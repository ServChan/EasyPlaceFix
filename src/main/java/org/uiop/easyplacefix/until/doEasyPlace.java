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

public class doEasyPlace {//TODO Easy Place rewrite plan

    // Whether the position belongs to any schematic area
    public static boolean isSchematicBlock(BlockPos pos) {
        SchematicPlacementManager schematicPlacementManager = DataManager.getSchematicPlacementManager();
        //Get loaded schematic placements touching this chunk position
        List<SchematicPlacementManager.PlacementPart> allPlacementsTouchingChunk
                = schematicPlacementManager.getAllPlacementsTouchingChunk(pos);
        //Check whether any placement part contains this position
        for (SchematicPlacementManager.PlacementPart placementPart : allPlacementsTouchingChunk) {
            if (placementPart.getBox().containsPos(pos)) {
                return true;
            }
        }
        return false;
    }

    public static ItemStack loosenMode2(HashSet<ItemStack> itemStackHashSet) {

        for (int i = 0; i < MinecraftClient.getInstance().player.getInventory().size(); i++) {
            ItemStack stack = MinecraftClient.getInstance().player.getInventory().getStack(i);
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
            if (!EntityUtils.isCreativeMode(MinecraftClient.getInstance().player)) {
                Block ReplacedBlock = stateSchema.getBlock();//The schematic block expected at this position
                Predicate<Block> predicate = null;
                if (ReplacedBlock instanceof WallBlock)   //wall blocks
                    predicate = block -> block instanceof WallBlock;
                else if (ReplacedBlock instanceof FenceGateBlock)//fence gates
                    predicate = block -> block instanceof FenceGateBlock;
                else if (ReplacedBlock instanceof TrapdoorBlock)//trapdoors
                    predicate = block -> block instanceof TrapdoorBlock;
                else if (ReplacedBlock instanceof CoralFanBlock)//coral fans
                    predicate = block -> block instanceof CoralFanBlock;
                ItemStack stack1 = null;
                if (predicate != null) {
                    PlayerInventory playerInventory = MinecraftClient.getInstance().player.getInventory();
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

    public static ActionResult doEasyPlace2(MinecraftClient mc, RayTraceUtils.RayTraceWrapper traceWrapper) {
        BlockHitResult trace = traceWrapper.getBlockHitResult();//Ray-traced hit from schematic
        World schematicWorld = SchematicWorldHandler.getSchematicWorld();
        if (schematicWorld == null) {
            return ActionResult.PASS;
        }
        BlockPos pos = trace.getBlockPos();//Target position from schematic hit

        if (isPlacementCooling(pos)) return ActionResult.FAIL;// Placement cooldown check
        BlockState stateClient = mc.world.getBlockState(pos);//Current client world block state
        BlockState stateSchematic = schematicWorld.getBlockState(pos);
        ActionResult isTermination = ((IBlock) stateClient.getBlock()).isWorldTermination(pos, stateSchematic, stateClient);//termination check
        if (isTermination != null) return isTermination;
        // Two-phase termination checks
        isTermination = ((IBlock) stateSchematic.getBlock()).isSchemaTermination(pos, stateSchematic, stateClient);//termination check
        if (isTermination != null) return isTermination;


        //MISS happens when aiming at nothing, excluding schematic-only hits
        HitResult traceVanilla = RayTraceUtils.getRayTraceFromEntity(mc.world, mc.player, false, getValidBlockRange(mc));
        if (traceVanilla.getType() == HitResult.Type.ENTITY) {
            return ActionResult.PASS;
        }
        if (traceWrapper.getHitType() == RayTraceUtils.RayTraceWrapper.HitType.SCHEMATIC_BLOCK) {

            ItemStack stack = new ItemStack(((IBlock) stateSchematic.getBlock()).getItemForBlockState(stateSchematic));
            if (!stack.isEmpty()) {

                BlockState currentState = mc.world.getBlockState(pos);
                if (isPlacementStateSatisfied(stateSchematic, currentState))//compare states
                {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("EasyPlace skip at {} because world state already matches schematic", pos);
                    }
                    return ActionResult.FAIL;
                }
                //Removed old cache and speed checks
                if (!stateClient.canReplace(
                        new ItemPlacementContext(
                                MinecraftClient.getInstance().player,
                                Hand.MAIN_HAND,
                                stack,
                                trace
                        ))
                ) return ActionResult.FAIL;


                ClientPlayerInteractionManager interactionManager = MinecraftClient.getInstance().interactionManager;

                ItemStack itemStack2 = searchItem(mc, stack);
                itemStack2 = loosenMode(itemStack2, stateSchematic);
                if (itemStack2 == null) {//Cannot place when required item is missing
                    return ActionResult.FAIL;
                }

                Block block = stateSchematic.getBlock();//Block instance to operate on
                Pair<RelativeBlockHitResult, Integer> blockHitResultIntegerPair =
                        ((IBlock) block).getHitResult(
                                stateSchematic,
                                trace.getBlockPos(),
                                stateClient
                        );

                if (blockHitResultIntegerPair == null) return ActionResult.FAIL;
                RelativeBlockHitResult offsetBlockHitResult = blockHitResultIntegerPair.getLeft();//Placement hit result data
                if (stateSchematic.getBlock() instanceof PistonBlock) {//TODO Investigate interactBlock internals and improve this branch
                    pistonBlockState = stateSchematic;
                    modifyBoolean = true;
                }
                ItemStack finalStack = itemStack2;
//                concurrentMap.put(pos,0L);

                AtomicReference<Hand> hand = new AtomicReference<>();

//                Channel channel = ((ClientConnectionAccessor) MinecraftClient.getInstance().getNetworkHandler().getConnection()).getChannel();
//                Pair<Float, Float> lookAtPair = ((IBlock) block).getLimitYawAndPitch(stateSchematic);
                boolean hasSleep = ((IBlock) block).HasSleepTime(stateSchematic);
                var YawAndPitch = ((IBlock) block).getYawAndPitch(stateSchematic);
                boolean hasRotation = YawAndPitch != null;
                float rotationYaw = hasRotation ? YawAndPitch.getLeft().Value() : 0.0F;
                float rotationPitch = hasRotation ? YawAndPitch.getRight().Value() : 0.0F;
                if (hasRotation) {
                    yawLock = rotationYaw;
                    pitchLock = rotationPitch;
                }
                if (hasSleep) {
                    TickThread.addLastTask(
                            new RunnableWithLast.Builder()
                                    .setTask(() -> {
                                        if (hasRotation) {
                                            yawLock = rotationYaw;
                                            pitchLock = rotationPitch;
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
                                            yawLock = rotationYaw;
                                            pitchLock = rotationPitch;
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
                                            yawLock = rotationYaw;
                                            pitchLock = rotationPitch;
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
                                            yawLock = rotationYaw;
                                            pitchLock = rotationPitch;
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
                            return null;//shulker box path
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

        ;//Use crosshair target position
        //Target position should be near schematic regions
        //Placement restriction radius check
        return isPositionWithinRangeOfSchematicRegions(pos, 2);
    }

    private static boolean isPlacementStateSatisfied(BlockState schematic, BlockState world) {
        if (schematic.getBlock() != world.getBlock()) {
            return false;
        }

        if (schematic.getBlock() instanceof StairsBlock) {
            // For stairs we ignore SHAPE because it is neighbor-dependent and can lag behind on servers.
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

            // OPEN can be controlled by redstone on servers; don't force retries in powered state.
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

        if (block instanceof TrapdoorBlock) {
            // Delay trapdoor toggles to avoid neighbor placements during high-speed desync windows.
            for (int i = 1; i <= extraClicks; i++) {
                int delay = i;
                TickThread.addCountDownTask(new RunnableWithCountDown.Builder().setCount(delay).build(() -> {
                    if (mc.player == null || mc.world == null) {
                        return;
                    }
                    BlockState current = mc.world.getBlockState(targetPos);
                    if (!(current.getBlock() instanceof TrapdoorBlock)) {
                        return;
                    }
                    interactionManager.interactBlock(
                            mc.player,
                            usedHand,
                            hitResult
                    );
                    mc.player.swingHand(usedHand);
                }));
            }
            return;
        }

        int i = 1;
        while (i < totalClicks) {
            interactionManager.interactBlock(
                    mc.player,
                    usedHand,
                    hitResult
            );
            mc.player.swingHand(usedHand);
            i++;
        }
    }
}
