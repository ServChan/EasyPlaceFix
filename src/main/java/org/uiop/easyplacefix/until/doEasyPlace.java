package org.uiop.easyplacefix.until;

import com.tick_ins.tick.RunnableWithLast;
import com.tick_ins.tick.TickThread;
import com.tick_ins.tick.RunnableWithCountDown;
import fi.dy.masa.litematica.config.Configs;
import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacementManager;
import fi.dy.masa.litematica.util.EntityUtils;
import fi.dy.masa.litematica.util.RayTraceUtils;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.state.property.Properties;
import net.minecraft.world.World;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.IClientPlayerInteractionManager;
import org.uiop.easyplacefix.data.LoosenModeData;
import org.uiop.easyplacefix.data.RelativeBlockHitResult;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import static fi.dy.masa.litematica.util.InventoryUtils.findSlotWithBoxWithItem;
import static fi.dy.masa.litematica.util.InventoryUtils.setPickedItemToHand;
import static fi.dy.masa.litematica.util.WorldUtils.getValidBlockRange;
import static fi.dy.masa.litematica.util.WorldUtils.isPositionWithinRangeOfSchematicRegions;
import static org.uiop.easyplacefix.EasyPlaceFix.findBlockInInventory;
import static org.uiop.easyplacefix.EasyPlaceFix.LOGGER;
import static org.uiop.easyplacefix.config.easyPlacefixConfig.*;
import static org.uiop.easyplacefix.data.LoosenModeData.items;
import static org.uiop.easyplacefix.until.PlayerBlockAction.useItemOnAction.*;

public class doEasyPlace {

    public static boolean shouldAllowVanillaInteraction(MinecraftClient mc,
                                                         RayTraceUtils.RayTraceWrapper traceWrapper) {
        if (!Allow_Interaction.getBooleanValue() || mc.world == null || traceWrapper == null) {
            return false;
        }

        BlockHitResult trace = traceWrapper.getBlockHitResult();
        World schematicWorld = SchematicWorldHandler.getSchematicWorld();
        if (trace == null || schematicWorld == null) {
            return false;
        }

        BlockPos pos = trace.getBlockPos();
        BlockState stateClient = mc.world.getBlockState(pos);
        BlockState stateSchematic = schematicWorld.getBlockState(pos);
        return ((IBlock) stateClient.getBlock()).isWorldTermination(pos, stateSchematic, stateClient)
                == ActionResult.PASS;
    }

    public static boolean isSchematicBlock(BlockPos pos) {
        SchematicPlacementManager schematicPlacementManager = DataManager.getSchematicPlacementManager();

        List<SchematicPlacementManager.PlacementPart> allPlacementsTouchingChunk
                = schematicPlacementManager.getAllPlacementsTouchingChunk(pos);

        for (SchematicPlacementManager.PlacementPart placementPart : allPlacementsTouchingChunk) {
            if (placementPart.getBox().containsPos(pos)) {
                return true;
            }
        }
        return false;
    }

    public static ItemStack loosenMode2() {

        for (int i = 0; i < MinecraftClient.getInstance().player.getInventory().size(); i++) {
            ItemStack stack = MinecraftClient.getInstance().player.getInventory().getStack(i);
            stack = stack.copy();

            if (!stack.isEmpty()) {
                if (items.contains(stack.getItem())) {

                    return stack;
                }

            }
        }

        return null;

    }

    public static ItemStack loosenMode(ItemStack stack, BlockState stateSchema) {
        if (stack == null && LOOSEN_MODE.getBooleanValue()) {
            if (!EntityUtils.isCreativeMode(MinecraftClient.getInstance().player)) {
                Block ReplacedBlock = stateSchema.getBlock();
                Predicate<Block> predicate = null;
                if (ReplacedBlock instanceof WallBlock)
                    predicate = block -> block instanceof WallBlock;
                else if (ReplacedBlock instanceof FenceGateBlock)
                    predicate = block -> block instanceof FenceGateBlock;
                else if (ReplacedBlock instanceof TrapdoorBlock)
                    predicate = block -> block instanceof TrapdoorBlock;
                else if (ReplacedBlock instanceof CoralFanBlock)
                    predicate = block -> block instanceof CoralFanBlock;
                ItemStack stack1 = null;
                if (predicate != null) {
                    PlayerInventory playerInventory = MinecraftClient.getInstance().player.getInventory();
                    stack1 = findBlockInInventory(playerInventory, predicate);
                }
                if (stack1 == null) {
                    return loosenMode2();

                }
                return stack1;

            }

        }
        return stack;
    }

    public static ActionResult doEasyPlace2(MinecraftClient mc, RayTraceUtils.RayTraceWrapper traceWrapper) {
        BlockHitResult trace = traceWrapper.getBlockHitResult();
        World schematicWorld = SchematicWorldHandler.getSchematicWorld();
        if (schematicWorld == null) {
            return ActionResult.PASS;
        }
        BlockPos pos = trace.getBlockPos();

        if (isGlobalPlacementCooling()) return ActionResult.FAIL;
        if (isPlacementCooling(pos)) return ActionResult.FAIL;
        BlockState stateClient = mc.world.getBlockState(pos);
        BlockState stateSchematic = schematicWorld.getBlockState(pos);
        ActionResult isTermination = ((IBlock) stateClient.getBlock()).isWorldTermination(pos, stateSchematic, stateClient);
        if (isTermination != null) return isTermination;

        isTermination = ((IBlock) stateSchematic.getBlock()).isSchemaTermination(pos, stateSchematic, stateClient);
        if (isTermination != null) return isTermination;

        HitResult traceVanilla = RayTraceUtils.getRayTraceFromEntity(mc.world, mc.player, false, getValidBlockRange(mc));
        if (traceVanilla.getType() == HitResult.Type.ENTITY) {
            return ActionResult.PASS;
        }
        if (traceWrapper.getHitType() == RayTraceUtils.RayTraceWrapper.HitType.SCHEMATIC_BLOCK) {

            ItemStack stack = new ItemStack(((IBlock) stateSchematic.getBlock()).getItemForBlockState(stateSchematic));
            if (!stack.isEmpty()) {

                BlockState currentState = mc.world.getBlockState(pos);
                if (isPlacementStateSatisfied(stateSchematic, currentState))
                {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("EasyPlace skip at {} because world state already matches schematic", pos);
                    }
                    return ActionResult.FAIL;
                }

                if (!stateClient.canReplace(
                        new ItemPlacementContext(
                                MinecraftClient.getInstance().player,
                                Hand.MAIN_HAND,
                                stack,
                                trace
                        ))
                ) {
                    if (TerrainAutoReplace.isEligible(stateClient, stateSchematic)) {
                        TerrainAutoReplace.tryClearThenRetry(mc, traceWrapper, pos, trace.getSide());
                        return ActionResult.SUCCESS;
                    }
                    return ActionResult.FAIL;
                }
                if (stateSchematic.getBlock() instanceof NoteBlock
                        && currentState.getBlock() instanceof NoteBlock) {
                    int targetNote = stateSchematic.get(Properties.NOTE);
                    int currentNote = currentState.get(Properties.NOTE);
                    if (currentNote != targetNote) {
                        if (!NoteBlockHelper.isTuning(pos)) {
                            NoteBlockHelper.tune(mc, pos, targetNote);
                        }
                        return ActionResult.SUCCESS;
                    }
                }

                ClientPlayerInteractionManager interactionManager = MinecraftClient.getInstance().interactionManager;

                ItemStack itemStack2 = searchItem(mc, stack);
                itemStack2 = loosenMode(itemStack2, stateSchematic);
                if (itemStack2 == null) {
                    return ActionResult.FAIL;
                }

                Block block = stateSchematic.getBlock();
                Pair<RelativeBlockHitResult, Integer> blockHitResultIntegerPair =
                        ((IBlock) block).getHitResult(
                                stateSchematic,
                                trace.getBlockPos(),
                                stateClient
                        );

                if (blockHitResultIntegerPair == null) return ActionResult.FAIL;
                RelativeBlockHitResult offsetBlockHitResult = blockHitResultIntegerPair.getLeft();
                if (stateSchematic.getBlock() instanceof PistonBlock) {
                    pistonBlockState = stateSchematic;
                    modifyBoolean = true;
                }
                ItemStack finalStack = itemStack2;

                AtomicReference<Hand> hand = new AtomicReference<>();

                boolean hasSleep = ((IBlock) block).HasSleepTime(stateSchematic);
                var YawAndPitch = ((IBlock) block).getYawAndPitch(stateSchematic);
                boolean hasRotation = YawAndPitch != null;
                float rotationYaw = hasRotation ? YawAndPitch.getLeft().Value() : 0.0F;
                float rotationPitch = hasRotation ? YawAndPitch.getRight().Value() : 0.0F;
                markGlobalPlacement();
                if (hasSleep) {
                    TickThread.addLastTask(
                            new RunnableWithLast.Builder()
                                    .setTask(() -> {
                                        if (hasRotation) {
                                            PlayerRotationAction.setServerBoundPlayerRotation(
                                                    rotationYaw,
                                                    rotationPitch,
                                                    mc.player.horizontalCollision
                                            );
                                        }
                                        pickItem(mc, finalStack);
                                        hand.set(EntityUtils.getUsedHandForItem(mc.player, finalStack));
                                        ((IClientPlayerInteractionManager) interactionManager).syn();
                                    })
                                    .setYawAndPitch(hasRotation ? new oshi.util.tuples.Pair<>(rotationYaw, rotationPitch) : null)
                                    .cache(() -> {
                                        pickItem(mc, finalStack);
                                        hand.set(EntityUtils.getUsedHandForItem(mc.player, finalStack));
                                        ((IClientPlayerInteractionManager) interactionManager).syn();
                                        Hand usedHand = hand.get();
                                        if (usedHand == null) {
                                            return;
                                        }
                                        if (hasRotation) {
                                            PlayerRotationAction.setServerBoundPlayerRotation(
                                                    rotationYaw,
                                                    rotationPitch,
                                                    mc.player.horizontalCollision
                                            );
                                        }
                                        ((IBlock) block).firstAction(stateSchematic, trace);
                                        if (usePlacementStateOverride(stateSchematic)) {
                                            armPlacementStateOverride(trace.getBlockPos(), stateSchematic, offsetBlockHitResult.getSide());
                                        }
                                        interactionManager.interactBlock(
                                                mc.player,
                                                usedHand,
                                                offsetBlockHitResult
                                        );
                                        mc.player.swingHand(usedHand);
                                        runExtraInteractions(
                                                mc,
                                                interactionManager,
                                                usedHand,
                                                offsetBlockHitResult,
                                                blockHitResultIntegerPair.getRight(),
                                                block,
                                                trace.getBlockPos()
                                        );
                                        ((IBlock) block).afterAction(stateSchematic, trace);
                                        ((IBlock) block).BlockAction(stateSchematic, trace);
                                        if (CLIENT_ROTATION_REVERT.getBooleanValue()) {
                                            PlayerRotationAction.restRotation();
                                        }
                                    })
                                    .build()
                    );

                } else {
                    TickThread.addTask(new RunnableWithLast.Builder()
                                    .setTask(() -> {
                                        if (hasRotation) {
                                            PlayerRotationAction.setServerBoundPlayerRotation(
                                                    rotationYaw,
                                                    rotationPitch,
                                                    mc.player.horizontalCollision
                                            );
                                        }

                                        pickItem(mc, finalStack);
                                        hand.set(EntityUtils.getUsedHandForItem(mc.player, finalStack));
                                        ((IClientPlayerInteractionManager) interactionManager).syn();
                                    })
                                    .setYawAndPitch(hasRotation ? new oshi.util.tuples.Pair<>(rotationYaw, rotationPitch) : null)
                                    .build()
                            ,
                            new RunnableWithLast.Builder()
                                    .setTask(() -> {
                                        pickItem(mc, finalStack);
                                        hand.set(EntityUtils.getUsedHandForItem(mc.player, finalStack));
                                        ((IClientPlayerInteractionManager) interactionManager).syn();
                                        Hand usedHand = hand.get();
                                        if (usedHand == null) {
                                            return;
                                        }
                                        if (hasRotation) {
                                            PlayerRotationAction.setServerBoundPlayerRotation(
                                                    rotationYaw,
                                                    rotationPitch,
                                                    mc.player.horizontalCollision
                                            );
                                        }
                                        ((IBlock) block).firstAction(stateSchematic, trace);
                                        if (usePlacementStateOverride(stateSchematic)) {
                                            armPlacementStateOverride(trace.getBlockPos(), stateSchematic, offsetBlockHitResult.getSide());
                                        }
                                        interactionManager.interactBlock(
                                                mc.player,
                                                usedHand,
                                                offsetBlockHitResult
                                        );
                                        mc.player.swingHand(usedHand);
                                        runExtraInteractions(
                                                mc,
                                                interactionManager,
                                                usedHand,
                                                offsetBlockHitResult,
                                                blockHitResultIntegerPair.getRight(),
                                                block,
                                                trace.getBlockPos()
                                        );
                                        ((IBlock) block).afterAction(stateSchematic, trace);
                                        ((IBlock) block).BlockAction(stateSchematic, trace);
                                        if (CLIENT_ROTATION_REVERT.getBooleanValue()){
                                            PlayerRotationAction.restRotation();
                                        }
                                    })
                                    .build()
                    );

                }

            }

            return ActionResult.SUCCESS;

        }
        if (placementRestrictionInEffect(pos)) return ActionResult.FAIL;
        return ActionResult.PASS;
    }

    public static ItemStack searchItem(MinecraftClient mc, ItemStack stack) {
        if (mc.player != null && mc.interactionManager != null && mc.world != null) {
            if (!stack.isEmpty()) {
                PlayerInventory inv = mc.player.getInventory();
                stack = stack.copy();
                if (EntityUtils.isCreativeMode(mc.player)) {
                    return stack;
                } else {
                    int slot;
                    if (IGNORE_NBT.getBooleanValue()) {
                        slot = getSlotWithStackWithOutNbt(stack, inv);
                    } else {
                        slot = inv.getSlotWithStack(stack);
                    }

                    if (slot != -1) {
                        return inv.getStack(slot);
                    } else if (slot == -1 && Configs.Generic.PICK_BLOCK_SHULKERS.getBooleanValue()) {
                        slot = findSlotWithBoxWithItem(mc.player.playerScreenHandler, stack, false);
                        if (slot != -1) {
                            pickItem(mc, mc.player.playerScreenHandler.slots.get(slot).getStack());
                            return null;
                        }
                    }
                }
            }

        }
        return null;

    }

    public static int getSlotWithStackWithOutNbt(ItemStack stack, PlayerInventory inv) {
        for (int i = 0; i < inv.size(); ++i) {
            if (!inv.getStack(i).isEmpty() && ItemStack.areItemsEqual(stack, inv.getStack(i))) {
                return i;
            }
        }

        return -1;
    }

    public static void pickItem(MinecraftClient mc, ItemStack stack) {

        if (EntityUtils.isCreativeMode(mc.player)) {
            setPickedItemToHand(stack, mc);
            mc.interactionManager.clickCreativeStack(mc.player.getStackInHand(Hand.MAIN_HAND), 36 + mc.player.getInventory().getSelectedSlot());
        } else {
            setPickedItemToHand(stack, mc);
        }
    }

    private static boolean placementRestrictionInEffect(BlockPos pos) {

        ;

        return isPositionWithinRangeOfSchematicRegions(pos, 2);
    }

    private static boolean isPlacementStateSatisfied(BlockState schematic, BlockState world) {
        if (schematic.getBlock() != world.getBlock()) {
            return false;
        }

        if (schematic.getBlock() instanceof StairsBlock) {

            boolean sameFacing = schematic.get(Properties.HORIZONTAL_FACING) == world.get(Properties.HORIZONTAL_FACING);
            boolean sameHalf = schematic.get(Properties.BLOCK_HALF) == world.get(Properties.BLOCK_HALF);
            return sameFacing && sameHalf;
        }

        if (schematic.getBlock() instanceof TrapdoorBlock) {
            boolean sameFacing = schematic.get(Properties.HORIZONTAL_FACING) == world.get(Properties.HORIZONTAL_FACING);
            boolean sameHalf = schematic.get(Properties.BLOCK_HALF) == world.get(Properties.BLOCK_HALF);
            if (!sameFacing || !sameHalf) {
                return false;
            }

            boolean schematicPowered = schematic.contains(Properties.POWERED) && schematic.get(Properties.POWERED);
            boolean worldPowered = world.contains(Properties.POWERED) && world.get(Properties.POWERED);
            if (schematicPowered || worldPowered) {
                return true;
            }

            return schematic.get(Properties.OPEN) == world.get(Properties.OPEN);
        }

        if (schematic.getBlock() instanceof ShelfBlock || schematic.getBlock() instanceof LecternBlock) {
            return schematic.get(Properties.HORIZONTAL_FACING) == world.get(Properties.HORIZONTAL_FACING);
        }

        return schematic.equals(world);
    }

    private static boolean usePlacementStateOverride(BlockState blockState) {
        return blockState.getBlock() instanceof StairsBlock
                || blockState.getBlock() instanceof TrapdoorBlock
                || blockState.getBlock() instanceof ShelfBlock
                || blockState.getBlock() instanceof LecternBlock;
    }

    private static void runExtraInteractions(
            MinecraftClient mc,
            ClientPlayerInteractionManager interactionManager,
            Hand usedHand,
            RelativeBlockHitResult hitResult,
            int totalClicks,
            Block block,
            BlockPos targetPos
    ) {
        int extraClicks = Math.max(0, totalClicks - 1);
        if (extraClicks == 0) {
            return;
        }

        for (int i = 1; i <= extraClicks; i++) {
            TickThread.addCountDownTask(new RunnableWithCountDown.Builder().setCount(i * 2).build(() -> {
                if (mc.player == null || mc.world == null) return;
                if (mc.world.getBlockState(targetPos).getBlock() != block) return;
                interactionManager.interactBlock(mc.player, usedHand, hitResult);
                mc.player.swingHand(usedHand);
            }));
        }
    }
}
