package org.uiop.easyplacefix.until;

import com.tick_ins.tick.RunnableWithLast;
import com.tick_ins.tick.TickThread;
import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacementManager;
import fi.dy.masa.litematica.util.EntityUtils;
import fi.dy.masa.litematica.util.RayTraceUtils;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CoralFanBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.IClientPlayerInteractionManager;
import org.uiop.easyplacefix.data.LoosenModeData;
import org.uiop.easyplacefix.data.RelativeBlockHitResult;

import java.util.HashSet;
import java.util.List;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import static fi.dy.masa.litematica.util.WorldUtils.getValidBlockRange;
import static fi.dy.masa.litematica.util.WorldUtils.isPositionWithinRangeOfSchematicRegions;
import static org.uiop.easyplacefix.EasyPlaceFix.findBlockInInventory;
import static org.uiop.easyplacefix.EasyPlaceFix.LOGGER;
import static org.uiop.easyplacefix.config.easyPlacefixConfig.*;
import static org.uiop.easyplacefix.data.LoosenModeData.items;
import static org.uiop.easyplacefix.until.PlacementDiagnostics.report;
import static org.uiop.easyplacefix.until.PlacementInventory.pickItem;
import static org.uiop.easyplacefix.until.PlayerBlockAction.useItemOnAction.*;

public class doEasyPlace {
    private static final Method PLACEMENT_PART_GET_BOX = findMethod(
            SchematicPlacementManager.PlacementPart.class, "getBox");
    private static final Method BOX_CONTAINS = findMethod(
            PLACEMENT_PART_GET_BOX.getReturnType(), "contains", BlockPos.class);

    private static Method findMethod(Class<?> owner, String name, Class<?>... parameterTypes) {
        try {
            return owner.getMethod(name, parameterTypes);
        } catch (NoSuchMethodException exception) {
            throw new IllegalStateException("Unsupported Litematica/MaLiLib API: " + owner.getName() + "#" + name,
                    exception);
        }
    }

    private static boolean placementContains(SchematicPlacementManager.PlacementPart placementPart, BlockPos pos) {
        try {
            Object box = PLACEMENT_PART_GET_BOX.invoke(placementPart);
            return box != null && (boolean) BOX_CONTAINS.invoke(box, pos);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Could not inspect a Litematica placement bounding box", exception);
        }
    }

    public static boolean shouldAllowVanillaInteraction(Minecraft mc, RayTraceUtils.RayTraceWrapper traceWrapper) {
        if (!Allow_Interaction.getBooleanValue() || mc.level == null) {
            return false;
        }

        BlockHitResult trace = traceWrapper.getBlockHitResult();
        Level schematicWorld = SchematicWorldHandler.getSchematicWorld();
        if (trace == null || schematicWorld == null) {
            return false;
        }

        BlockPos pos = trace.getBlockPos();
        BlockState stateClient = mc.level.getBlockState(pos);
        BlockState stateSchematic = schematicWorld.getBlockState(pos);
        return ((IBlock) stateClient.getBlock()).isWorldTermination(pos, stateSchematic, stateClient)
                == InteractionResult.PASS;
    }

    // Whether the position belongs to any schematic area
    public static boolean isSchematicBlock(BlockPos pos) {
        SchematicPlacementManager schematicPlacementManager = DataManager.getSchematicPlacementManager();
        //Get loaded schematic placements touching this chunk position
        List<SchematicPlacementManager.PlacementPart> allPlacementsTouchingChunk
                = schematicPlacementManager.getAllPlacementsTouchingChunk(pos);
        //Check whether any placement part contains this position
        for (SchematicPlacementManager.PlacementPart placementPart : allPlacementsTouchingChunk) {
            if (placementContains(placementPart, pos)) {
                return true;
            }
        }
        return false;
    }

    public static ItemStack loosenMode2(HashSet<ItemStack> itemStackHashSet) {

        for (int i = 0; i < Minecraft.getInstance().player.getInventory().getContainerSize(); i++) {
            ItemStack stack = Minecraft.getInstance().player.getInventory().getItem(i);
            stack = stack.copy();
//                HashSet<Item> items =new HashSet<>();
//                for (ItemStack itemStack :itemStackHashSet){
//                    items.add(itemStack.getItem());
//                }
            if (!stack.isEmpty()) {
                if (items.contains(stack.getItem())) {
//                    InventoryUtils.setPickedItemToHand(i, stack.copy(), MinecraftClient.getInstance());
                    return stack; // Found a matching item stack and return it
                }


            }
        }

        return null;


    }

    public static ItemStack loosenMode(ItemStack stack, BlockState stateSchema) {
        if (stack == null && LOOSEN_MODE.getBooleanValue()) {
            if (!EntityUtils.isCreativeMode(Minecraft.getInstance().player)) {
                Block ReplacedBlock = stateSchema.getBlock();//The schematic block expected at this position
                Predicate<Block> predicate = null;
                if (ReplacedBlock instanceof WallBlock)   //wall blocks
                    predicate = block -> block instanceof WallBlock;
                else if (ReplacedBlock instanceof FenceGateBlock)//fence gates
                    predicate = block -> block instanceof FenceGateBlock;
                else if (ReplacedBlock instanceof TrapDoorBlock)//trapdoors
                    predicate = block -> block instanceof TrapDoorBlock;
                else if (ReplacedBlock instanceof CoralFanBlock)//coral fans
                    predicate = block -> block instanceof CoralFanBlock;
                ItemStack stack1 = null;
                if (predicate != null) {
                    Inventory playerInventory = Minecraft.getInstance().player.getInventory();
                    stack1 = findBlockInInventory(playerInventory, predicate);
                }
                if (stack1 == null) {
                    HashSet<ItemStack> itemStackHashSet = LoosenModeData.loadFromFile();
                    return loosenMode2(itemStackHashSet);

                }
                return stack1;

            }


        }
        return stack;
    }

    public static InteractionResult doEasyPlace2(Minecraft mc, RayTraceUtils.RayTraceWrapper traceWrapper) {
        BlockHitResult trace = traceWrapper.getBlockHitResult();//Ray-traced hit from schematic
        Level schematicWorld = SchematicWorldHandler.getSchematicWorld();
        if (schematicWorld == null) {
            report("easyplacefix.diagnostic.no_schematic_world");
            return InteractionResult.PASS;
        }
        BlockPos pos = trace.getBlockPos();//Target position from schematic hit

        if (isGlobalPlacementCooling()) {
            report("easyplacefix.diagnostic.global_cooldown", getEffectivePlacementDelayTicks());
            return InteractionResult.FAIL;
        }// Global rate limit (anti-cheat)
        if (isPlacementCooling(pos)) {
            report("easyplacefix.diagnostic.position_cooldown", pos.toShortString());
            return InteractionResult.FAIL;
        }// Per-position cooldown check
        BlockState stateClient = mc.level.getBlockState(pos);//Current client world block state
        BlockState stateSchematic = schematicWorld.getBlockState(pos);
        InteractionResult isTermination = ((IBlock) stateClient.getBlock()).isWorldTermination(pos, stateSchematic, stateClient);//termination check
        if (isTermination != null) {
            report("easyplacefix.diagnostic.world_termination", pos.toShortString());
            return isTermination;
        }
        // Two-phase termination checks
        isTermination = ((IBlock) stateSchematic.getBlock()).isSchemaTermination(pos, stateSchematic, stateClient);//termination check
        if (isTermination != null) {
            report("easyplacefix.diagnostic.schema_termination", pos.toShortString());
            return isTermination;
        }


        //MISS happens when aiming at nothing, excluding schematic-only hits
        HitResult traceVanilla = RayTraceUtils.getRayTraceFromEntity(mc.level, mc.player, false, getValidBlockRange(mc));
        if (traceVanilla.getType() == HitResult.Type.ENTITY) {
            report("easyplacefix.diagnostic.entity_in_crosshair");
            return InteractionResult.PASS;
        }
        if (traceWrapper.getHitType() == RayTraceUtils.RayTraceWrapper.HitType.SCHEMATIC_BLOCK) {

            ItemStack stack = PlacementItemResolver.getPlacementStack(stateSchematic, pos, schematicWorld);
            if (!stack.isEmpty()) {

                BlockState currentState = mc.level.getBlockState(pos);
                if (PlacementStateMatcher.isSatisfied(stateSchematic, currentState))//compare states
                {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("EasyPlace skip at {} because world state already matches schematic", pos);
                    }
                    report("easyplacefix.diagnostic.already_correct", pos.toShortString());
                    return InteractionResult.FAIL;
                }
                //Removed old cache and speed checks
                if (!stateClient.canBeReplaced(
                        new BlockPlaceContext(
                                Minecraft.getInstance().player,
                                InteractionHand.MAIN_HAND,
                                stack,
                                trace
                        ))
                ) {
                    report("easyplacefix.diagnostic.not_replaceable", pos.toShortString());
                    return InteractionResult.FAIL;
                }


                MultiPlayerGameMode interactionManager = Minecraft.getInstance().gameMode;

                ItemStack itemStack2 = PlacementInventory.searchItem(mc, stack);
                itemStack2 = loosenMode(itemStack2, stateSchematic);
                if (itemStack2 == null) {//Cannot place when required item is missing
                    report("easyplacefix.diagnostic.missing_item", stack.getHoverName());
                    return InteractionResult.FAIL;
                }

                Block block = stateSchematic.getBlock();//Block instance to operate on
                Tuple<RelativeBlockHitResult, Integer> blockHitResultIntegerPair =
                        ((IBlock) block).getHitResult(
                                stateSchematic,
                                trace.getBlockPos(),
                                stateClient
                        );

                if (blockHitResultIntegerPair == null) {
                    report("easyplacefix.diagnostic.no_hit_result", stateSchematic.getBlock().getName());
                    return InteractionResult.FAIL;
                }
                RelativeBlockHitResult offsetBlockHitResult = blockHitResultIntegerPair.getA();//Placement hit result data
                if (stateSchematic.getBlock() instanceof PistonBaseBlock) {//TODO Investigate interactBlock internals and improve this branch
                    pistonBlockState = stateSchematic;
                    modifyBoolean = true;
                }
                ItemStack finalStack = itemStack2;
//                concurrentMap.put(pos,0L);

                AtomicReference<InteractionHand> hand = new AtomicReference<>();

//                Channel channel = ((ClientConnectionAccessor) MinecraftClient.getInstance().getNetworkHandler().getConnection()).getChannel();
//                Pair<Float, Float> lookAtPair = ((IBlock) block).getLimitYawAndPitch(stateSchematic);
                boolean hasSleep = ((IBlock) block).HasSleepTime(stateSchematic);
                var YawAndPitch = ((IBlock) block).getYawAndPitch(stateSchematic);
                boolean hasRotation = YawAndPitch != null;
                float rotationYaw = hasRotation ? YawAndPitch.getA().Value() : 0.0F;
                float rotationPitch = hasRotation ? YawAndPitch.getB().Value() : 0.0F;
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
                                        InteractionHand usedHand = hand.get();
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
                                        if (PlacementStateMatcher.shouldUsePlacementOverride(stateSchematic)) {
                                            armPlacementStateOverride(trace.getBlockPos(), stateSchematic, offsetBlockHitResult.getDirection());
                                        }
                                        interactionManager.useItemOn(
                                                mc.player,
                                                usedHand,
                                                offsetBlockHitResult
                                        );
                                        mc.player.swing(usedHand);
                                        ExtraInteractionRunner.run(
                                                mc,
                                                interactionManager,
                                                usedHand,
                                                offsetBlockHitResult,
                                                blockHitResultIntegerPair.getB(),
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
                                        InteractionHand usedHand = hand.get();
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
                                        if (PlacementStateMatcher.shouldUsePlacementOverride(stateSchematic)) {
                                            armPlacementStateOverride(trace.getBlockPos(), stateSchematic, offsetBlockHitResult.getDirection());
                                        }
                                        interactionManager.useItemOn(
                                                mc.player,
                                                usedHand,
                                                offsetBlockHitResult
                                        );
                                        mc.player.swing(usedHand);
                                        ExtraInteractionRunner.run(
                                                mc,
                                                interactionManager,
                                                usedHand,
                                                offsetBlockHitResult,
                                                blockHitResultIntegerPair.getB(),
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


                report("easyplacefix.diagnostic.placing", stack.getHoverName(), pos.toShortString());
            } else {
                report("easyplacefix.diagnostic.no_block_item", stateSchematic.getBlock().getName());
            }


            return InteractionResult.SUCCESS;

        }
        if (placementRestrictionInEffect(pos)) {
            report("easyplacefix.diagnostic.restricted_area", pos.toShortString());
            return InteractionResult.FAIL;
        }
        report("easyplacefix.diagnostic.no_schematic_hit");
        return InteractionResult.PASS;
    }

    public static ItemStack searchItem(Minecraft mc, ItemStack stack) {
        return PlacementInventory.searchItem(mc, stack);
    }

    public static int getSlotWithStackWithOutNbt(ItemStack stack, Inventory inv) {
        return PlacementInventory.getSlotWithStackWithoutNbt(stack, inv);
    }

    public static int getSlotWithStack(ItemStack stack, Inventory inv) {
        return PlacementInventory.getSlotWithStack(stack, inv);
    }

    public static void pickItem(Minecraft mc, ItemStack stack) {
        PlacementInventory.pickItem(mc, stack);
    }

    private static boolean placementRestrictionInEffect(BlockPos pos) {

        ;//Use crosshair target position
        //Target position should be near schematic regions
        //Placement restriction radius check
        return isPositionWithinRangeOfSchematicRegions(pos, 2);
    }

}
