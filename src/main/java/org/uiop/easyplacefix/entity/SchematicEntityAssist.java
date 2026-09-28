package org.uiop.easyplacefix.entity;

import com.tick_ins.packet.Ping2Server;
import com.tick_ins.tick.RunnableWithCountDown;
import com.tick_ins.tick.RunnableWithLast;
import com.tick_ins.tick.TickThread;
import fi.dy.masa.litematica.util.EntityUtils;
import fi.dy.masa.litematica.util.RayTraceUtils;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.litematica.world.WorldSchematic;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.GlowItemFrame;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.decoration.painting.Painting;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.uiop.easyplacefix.IClientPlayerInteractionManager;
import org.uiop.easyplacefix.data.RelativeBlockHitResult;
import org.uiop.easyplacefix.util.PlacementInventory;
import org.uiop.easyplacefix.util.PlayerBlockAction;
import org.uiop.easyplacefix.util.PlayerRotationAction;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.uiop.easyplacefix.config.easyPlacefixConfig.CLIENT_ROTATION_REVERT;
import static org.uiop.easyplacefix.config.easyPlacefixConfig.ENTITY_PLACEMENT;
import static org.uiop.easyplacefix.util.PlacementDiagnostics.report;

public final class SchematicEntityAssist {
    private static final double POSITION_TOLERANCE = 0.5;
    private static final float ROTATION_TOLERANCE = 1.0F;
    private static final long BUSY_GRACE_MS = 400L;
    private static final List<EquipmentSlot> ARMOR_SLOTS =
            List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);

    private static final Map<String, Long> BUSY_UNTIL = new ConcurrentHashMap<>();

    private SchematicEntityAssist() {
    }

    public static void clear() {
        BUSY_UNTIL.clear();
    }

    public static InteractionResult tryHandle(Minecraft mc, RayTraceUtils.RayTraceWrapper traceWrapper) {
        if (!ENTITY_PLACEMENT.getBooleanValue() || mc.player == null || mc.level == null || mc.gameMode == null) {
            return null;
        }
        WorldSchematic schematicWorld = SchematicWorldHandler.getSchematicWorld();
        if (schematicWorld == null) {
            return null;
        }

        LocalPlayer player = mc.player;
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getViewVector(1.0F).scale(player.blockInteractionRange()));
        Entity target = null;
        double targetDistance = Double.MAX_VALUE;
        for (Entity entity : schematicWorld.getEntities((Entity) null, new AABB(eye, end).inflate(1.0), SchematicEntityAssist::isSupported)) {
            Optional<Vec3> hit = entity.getBoundingBox().inflate(0.1).clip(eye, end);
            if (hit.isPresent()) {
                double distance = eye.distanceToSqr(hit.get());
                if (distance < targetDistance) {
                    targetDistance = distance;
                    target = entity;
                }
            }
        }
        if (target == null) {
            return null;
        }

        BlockHitResult blockHit = traceWrapper == null ? null : traceWrapper.getBlockHitResult();
        if (blockHit != null && blockHit.getType() != HitResult.Type.MISS
                && eye.distanceToSqr(blockHit.getLocation()) + 0.01 < targetDistance
                && !isSupportOf(target, blockHit.getBlockPos())) {
            return null;
        }

        String busyKey = busyKey(target);
        Long busyUntil = BUSY_UNTIL.get(busyKey);
        if (busyUntil != null && System.currentTimeMillis() < busyUntil) {
            return InteractionResult.SUCCESS;
        }
        if (PlayerBlockAction.useItemOnAction.isGlobalPlacementCooling()) {
            return InteractionResult.FAIL;
        }

        if (target instanceof ItemFrame frame) {
            return handleItemFrame(mc, frame, busyKey);
        }
        if (target instanceof Painting painting) {
            return handlePainting(mc, painting, busyKey);
        }
        if (target instanceof ArmorStand armorStand) {
            return handleArmorStand(mc, armorStand, busyKey);
        }
        return null;
    }

    private static boolean isSupported(Entity entity) {
        return entity instanceof ItemFrame
                || entity instanceof Painting
                || (entity instanceof ArmorStand armorStand && !armorStand.isMarker());
    }

    private static boolean isSupportOf(Entity entity, BlockPos pos) {
        if (entity instanceof ItemFrame frame) {
            return frame.getPos().relative(frame.getDirection().getOpposite()).equals(pos);
        }
        if (entity instanceof Painting painting) {
            return painting.getPos().relative(painting.getDirection().getOpposite()).equals(pos);
        }
        return false;
    }

    private static String busyKey(Entity entity) {
        return entity.getType() + "@" + BlockPos.containing(entity.position()).toShortString();
    }

    private static void markBusy(String key, int ticks) {
        BUSY_UNTIL.put(key, System.currentTimeMillis() + ticks * 50L + Ping2Server.getRtt() + BUSY_GRACE_MS);
        if (BUSY_UNTIL.size() > 256) {
            long now = System.currentTimeMillis();
            BUSY_UNTIL.entrySet().removeIf(entry -> entry.getValue() < now);
        }
    }

    private static InteractionResult handleItemFrame(Minecraft mc, ItemFrame schematicFrame, String busyKey) {
        ClientLevel level = mc.level;
        BlockPos pos = schematicFrame.getPos();
        Direction direction = schematicFrame.getDirection();
        boolean glow = schematicFrame instanceof GlowItemFrame;

        ItemFrame worldFrame = level.getEntitiesOfClass(ItemFrame.class, new AABB(pos).inflate(0.1),
                        frame -> frame.getPos().equals(pos) && frame.getDirection() == direction
                                && (frame instanceof GlowItemFrame) == glow)
                .stream().findFirst().orElse(null);

        if (worldFrame == null) {
            BlockPos support = pos.relative(direction.getOpposite());
            if (level.getBlockState(support).canBeReplaced()) {
                report("easyplacefix.diagnostic.entity_no_support", pos.toShortString());
                return InteractionResult.FAIL;
            }
            ItemStack frameStack = findStack(mc, new ItemStack(glow ? Items.GLOW_ITEM_FRAME : Items.ITEM_FRAME));
            if (frameStack == null) {
                return InteractionResult.FAIL;
            }
            placeOnFace(mc, frameStack, support, direction, null);
            markBusy(busyKey, 2);
            report("easyplacefix.diagnostic.entity_placing", frameStack.getHoverName(), pos.toShortString());
            return InteractionResult.SUCCESS;
        }

        ItemStack wanted = schematicFrame.getItem();
        if (wanted.isEmpty()) {
            return null;
        }
        ItemStack present = worldFrame.getItem();
        if (present.isEmpty()) {
            ItemStack stack = findStack(mc, wanted.copyWithCount(1));
            if (stack == null) {
                return InteractionResult.FAIL;
            }
            interactWith(mc, worldFrame, stack);
            markBusy(busyKey, 2);
            report("easyplacefix.diagnostic.entity_filling_frame", stack.getHoverName(), pos.toShortString());
            return InteractionResult.SUCCESS;
        }
        if (!ItemStack.isSameItem(present, wanted)) {
            report("easyplacefix.diagnostic.entity_frame_wrong_item", pos.toShortString(), present.getHoverName(), wanted.getHoverName());
            return InteractionResult.FAIL;
        }

        int clicks = Math.floorMod(schematicFrame.getRotation() - worldFrame.getRotation(), 8);
        if (clicks == 0) {
            return null;
        }
        for (int i = 0; i < clicks; i++) {
            TickThread.addCountDownTask(new RunnableWithCountDown.Builder().setCount(i * 2 + 1).build(() -> {
                if (mc.player != null && mc.gameMode != null && worldFrame.isAlive()) {
                    mc.gameMode.interact(mc.player, worldFrame, hitAtCenter(worldFrame), InteractionHand.MAIN_HAND);
                    mc.player.swing(InteractionHand.MAIN_HAND, mc.player.getItemInHand(InteractionHand.MAIN_HAND).getInteractAnimation(), false);
                }
            }));
        }
        PlayerBlockAction.useItemOnAction.markGlobalPlacement();
        markBusy(busyKey, clicks * 2 + 1);
        report("easyplacefix.diagnostic.entity_rotating_frame", pos.toShortString(), clicks);
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult handlePainting(Minecraft mc, Painting schematicPainting, String busyKey) {
        ClientLevel level = mc.level;
        BlockPos pos = schematicPainting.getPos();
        Direction direction = schematicPainting.getDirection();
        Painting worldPainting = level.getEntitiesOfClass(Painting.class, schematicPainting.getBoundingBox().inflate(0.1),
                        painting -> painting.getDirection() == direction)
                .stream().findFirst().orElse(null);

        if (worldPainting != null) {
            if (!worldPainting.getVariant().equals(schematicPainting.getVariant())) {
                report("easyplacefix.diagnostic.entity_painting_variant", pos.toShortString());
                return InteractionResult.FAIL;
            }
            return null;
        }

        BlockPos support = pos.relative(direction.getOpposite());
        if (level.getBlockState(support).canBeReplaced()) {
            report("easyplacefix.diagnostic.entity_no_support", pos.toShortString());
            return InteractionResult.FAIL;
        }
        ItemStack exact = schematicPainting.getPickResult();
        ItemStack stack = exact == null || exact.isEmpty() ? null : PlacementInventory.searchItem(mc, exact);
        boolean exactVariant = stack != null;
        if (stack == null) {
            stack = findStack(mc, new ItemStack(Items.PAINTING));
            if (stack == null) {
                return InteractionResult.FAIL;
            }
        }
        placeOnFace(mc, stack, support, direction, null);
        markBusy(busyKey, 2);
        report(exactVariant ? "easyplacefix.diagnostic.entity_placing" : "easyplacefix.diagnostic.entity_painting_random",
                stack.getHoverName(), pos.toShortString());
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult handleArmorStand(Minecraft mc, ArmorStand schematicStand, String busyKey) {
        ClientLevel level = mc.level;
        Vec3 position = schematicStand.position();
        ArmorStand worldStand = level.getEntitiesOfClass(ArmorStand.class,
                        new AABB(position, position).inflate(POSITION_TOLERANCE),
                        stand -> !stand.isMarker() && stand.position().distanceTo(position) <= POSITION_TOLERANCE)
                .stream().findFirst().orElse(null);

        if (worldStand == null) {
            BlockPos cell = BlockPos.containing(position);
            if (!level.getBlockState(cell).canBeReplaced() || !level.getBlockState(cell.above()).canBeReplaced()) {
                report("easyplacefix.diagnostic.entity_no_space", cell.toShortString());
                return InteractionResult.FAIL;
            }
            ItemStack stack = findStack(mc, new ItemStack(Items.ARMOR_STAND));
            if (stack == null) {
                return InteractionResult.FAIL;
            }
            BlockPos below = cell.below();
            float yaw = Mth.wrapDegrees(schematicStand.getYRot() + 180.0F);
            if (level.getBlockState(below).canBeReplaced()) {
                placeOnFace(mc, stack, cell, Direction.UP, yaw, new Vec3(0.5, 0.0, 0.5));
            } else {
                placeOnFace(mc, stack, below, Direction.UP, yaw);
            }
            markBusy(busyKey, 2);
            report("easyplacefix.diagnostic.entity_placing", stack.getHoverName(), cell.toShortString());
            return InteractionResult.SUCCESS;
        }

        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack wanted = schematicStand.getItemBySlot(slot);
            if (wanted.isEmpty() || !worldStand.getItemBySlot(slot).isEmpty()) {
                continue;
            }
            if (worldStand.getEquipmentSlotForItem(wanted) != slot) {
                continue;
            }
            ItemStack stack = findStack(mc, wanted.copyWithCount(1));
            if (stack == null) {
                return InteractionResult.FAIL;
            }
            interactWith(mc, worldStand, stack);
            markBusy(busyKey, 2);
            report("easyplacefix.diagnostic.entity_equipping", stack.getHoverName(), BlockPos.containing(position).toShortString());
            return InteractionResult.SUCCESS;
        }

        if (Math.abs(Mth.wrapDegrees(worldStand.getYRot() - schematicStand.getYRot())) > ROTATION_TOLERANCE) {
            report("easyplacefix.diagnostic.entity_stand_rotation", BlockPos.containing(position).toShortString());
            return InteractionResult.FAIL;
        }
        return null;
    }

    private static ItemStack findStack(Minecraft mc, ItemStack wanted) {
        ItemStack stack = PlacementInventory.searchItem(mc, wanted);
        if (stack == null) {
            report("easyplacefix.diagnostic.missing_item", wanted.getHoverName());
        }
        return stack;
    }

    private static EntityHitResult hitAtCenter(Entity entity) {
        return new EntityHitResult(entity, entity.getBoundingBox().getCenter());
    }

    private static void placeOnFace(Minecraft mc, ItemStack stack, BlockPos clickedPos, Direction face, Float yaw) {
        Vec3 faceCenter = new Vec3(0.5 + 0.5 * face.getStepX(), 0.5 + 0.5 * face.getStepY(), 0.5 + 0.5 * face.getStepZ());
        placeOnFace(mc, stack, clickedPos, face, yaw, faceCenter);
    }

    private static void placeOnFace(Minecraft mc, ItemStack stack, BlockPos clickedPos, Direction face, Float yaw,
                                    Vec3 relativeHit) {
        RelativeBlockHitResult hitResult = new RelativeBlockHitResult(relativeHit, face, clickedPos, false);
        float pitch = mc.player.getXRot();
        PlayerBlockAction.useItemOnAction.markGlobalPlacement();
        TickThread.addTask(
                new RunnableWithLast.Builder()
                        .setTask(() -> {
                            if (yaw != null) {
                                PlayerRotationAction.setServerBoundPlayerRotation(yaw, pitch, mc.player.horizontalCollision);
                            }
                            PlacementInventory.pickItem(mc, stack);
                            ((IClientPlayerInteractionManager) mc.gameMode).syn();
                        })
                        .setYawAndPitch(yaw == null ? null : new oshi.util.tuples.Pair<>(yaw, pitch))
                        .build(),
                new RunnableWithLast.Builder()
                        .setTask(() -> {
                            PlacementInventory.pickItem(mc, stack);
                            ((IClientPlayerInteractionManager) mc.gameMode).syn();
                            InteractionHand hand = EntityUtils.getUsedHandForItem(mc.player, stack);
                            if (hand == null) {
                                return;
                            }
                            if (yaw != null) {
                                PlayerRotationAction.setServerBoundPlayerRotation(yaw, pitch, mc.player.horizontalCollision);
                            }
                            mc.gameMode.useItemOn(mc.player, hand, hitResult);
                            mc.player.swing(hand, mc.player.getItemInHand(hand).getInteractAnimation(), false);
                            if (yaw != null && CLIENT_ROTATION_REVERT.getBooleanValue()) {
                                PlayerRotationAction.restRotation();
                            }
                        })
                        .build()
        );
    }

    private static void interactWith(Minecraft mc, Entity entity, ItemStack stack) {
        PlayerBlockAction.useItemOnAction.markGlobalPlacement();
        TickThread.addTask(
                new RunnableWithLast.Builder()
                        .setTask(() -> {
                            PlacementInventory.pickItem(mc, stack);
                            ((IClientPlayerInteractionManager) mc.gameMode).syn();
                        })
                        .build(),
                new RunnableWithLast.Builder()
                        .setTask(() -> {
                            PlacementInventory.pickItem(mc, stack);
                            ((IClientPlayerInteractionManager) mc.gameMode).syn();
                            InteractionHand hand = EntityUtils.getUsedHandForItem(mc.player, stack);
                            if (hand == null || !entity.isAlive()) {
                                return;
                            }
                            mc.gameMode.interact(mc.player, entity, hitAtCenter(entity), hand);
                            mc.player.swing(hand, mc.player.getItemInHand(hand).getInteractAnimation(), false);
                        })
                        .build()
        );
    }
}
