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
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.DecoratedPotBlock;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CoralFanBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.ShelfBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
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
            return InteractionResult.PASS;
        }
        BlockPos pos = trace.getBlockPos();//Target position from schematic hit

        if (isGlobalPlacementCooling()) return InteractionResult.FAIL;// Global rate limit (anti-cheat)
        if (isPlacementCooling(pos)) return InteractionResult.FAIL;// Per-position cooldown check
        BlockState stateClient = mc.level.getBlockState(pos);//Current client world block state
        BlockState stateSchematic = schematicWorld.getBlockState(pos);
        InteractionResult isTermination = ((IBlock) stateClient.getBlock()).isWorldTermination(pos, stateSchematic, stateClient);//termination check
        if (isTermination != null) return isTermination;
        // Two-phase termination checks
        isTermination = ((IBlock) stateSchematic.getBlock()).isSchemaTermination(pos, stateSchematic, stateClient);//termination check
        if (isTermination != null) return isTermination;


        //MISS happens when aiming at nothing, excluding schematic-only hits
        HitResult traceVanilla = RayTraceUtils.getRayTraceFromEntity(mc.level, mc.player, false, getValidBlockRange(mc));
        if (traceVanilla.getType() == HitResult.Type.ENTITY) {
            return InteractionResult.PASS;
        }
        if (traceWrapper.getHitType() == RayTraceUtils.RayTraceWrapper.HitType.SCHEMATIC_BLOCK) {

            ItemStack stack = getPlacementStack(stateSchematic, pos, schematicWorld);
            if (!stack.isEmpty()) {

                BlockState currentState = mc.level.getBlockState(pos);
                if (isPlacementStateSatisfied(stateSchematic, currentState))//compare states
                {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("EasyPlace skip at {} because world state already matches schematic", pos);
                    }
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
                ) return InteractionResult.FAIL;


                MultiPlayerGameMode interactionManager = Minecraft.getInstance().gameMode;

                ItemStack itemStack2 = searchItem(mc, stack);
                itemStack2 = loosenMode(itemStack2, stateSchematic);
                if (itemStack2 == null) {//Cannot place when required item is missing
                    return InteractionResult.FAIL;
                }

                Block block = stateSchematic.getBlock();//Block instance to operate on
                Tuple<RelativeBlockHitResult, Integer> blockHitResultIntegerPair =
                        ((IBlock) block).getHitResult(
                                stateSchematic,
                                trace.getBlockPos(),
                                stateClient
                        );

                if (blockHitResultIntegerPair == null) return InteractionResult.FAIL;
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
                                        if (usePlacementStateOverride(stateSchematic)) {
                                            armPlacementStateOverride(trace.getBlockPos(), stateSchematic, offsetBlockHitResult.getDirection());
                                        }
                                        interactionManager.useItemOn(
                                                mc.player,
                                                usedHand,
                                                offsetBlockHitResult
                                        );
                                        mc.player.swing(usedHand);
                                        runExtraInteractions(
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
                                        if (usePlacementStateOverride(stateSchematic)) {
                                            armPlacementStateOverride(trace.getBlockPos(), stateSchematic, offsetBlockHitResult.getDirection());
                                        }
                                        interactionManager.useItemOn(
                                                mc.player,
                                                usedHand,
                                                offsetBlockHitResult
                                        );
                                        mc.player.swing(usedHand);
                                        runExtraInteractions(
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


            }


            return InteractionResult.SUCCESS;

        }
        if (placementRestrictionInEffect(pos)) return InteractionResult.FAIL;
        return InteractionResult.PASS;
    }

    public static ItemStack searchItem(Minecraft mc, ItemStack stack) {
        if (mc.player != null && mc.gameMode != null && mc.level != null) {
            if (!stack.isEmpty()) {
                Inventory inv = mc.player.getInventory();
                stack = stack.copy();
                if (EntityUtils.isCreativeMode(mc.player)) {
                    return stack;
                } else {
                    int slot;
                    if (IGNORE_NBT.getBooleanValue()) {
                        slot = getSlotWithStackWithOutNbt(stack, inv);
                    } else {
                        slot = getSlotWithStack(stack, inv);
                    }

                    if (slot != -1) {
                        return inv.getItem(slot);
                    } else if (slot == -1 && Configs.Generic.PICK_BLOCK_SHULKERS.getBooleanValue()) {
                        slot = findSlotWithBoxWithItem(mc.player.inventoryMenu, stack, false);
                        if (slot != -1) {
                            pickItem(mc, mc.player.inventoryMenu.slots.get(slot).getItem());
                            return null;//shulker box path
                        }
                    }
                }
            }

        }
        return null;

    }

    public static int getSlotWithStackWithOutNbt(ItemStack stack, Inventory inv) {
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            if (!inv.getItem(i).isEmpty() && ItemStack.isSameItem(stack, inv.getItem(i))) {
                return i;
            }
        }

        return -1;
    }

    public static int getSlotWithStack(ItemStack stack, Inventory inv) {
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            if (!inv.getItem(i).isEmpty() && ItemStack.isSameItemSameComponents(stack, inv.getItem(i))) {
                return i;
            }
        }

        return -1;
    }

    private static ItemStack getPlacementStack(BlockState stateSchematic, BlockPos pos, Level schematicWorld) {
        if (stateSchematic.getBlock() instanceof DecoratedPotBlock) {
            BlockEntity blockEntity = schematicWorld.getBlockEntity(pos);
            if (blockEntity instanceof DecoratedPotBlockEntity decoratedPotBlockEntity) {
                return DecoratedPotBlockEntity.createDecoratedPotInstance(decoratedPotBlockEntity.getDecorations());
            }
        }

        return new ItemStack(((IBlock) stateSchematic.getBlock()).getItemForBlockState(stateSchematic));
    }

    public static void pickItem(Minecraft mc, ItemStack stack) {

        if (EntityUtils.isCreativeMode(mc.player)) {
            setPickedItemToHand(stack, mc);
            mc.gameMode.handleCreativeModeItemAdd(mc.player.getItemInHand(InteractionHand.MAIN_HAND), 36 + mc.player.getInventory().getSelectedSlot());
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

        if (schematic.getBlock() instanceof StairBlock) {
            // For stairs we ignore SHAPE because it is neighbor-dependent and can lag behind on servers.
            boolean sameFacing = schematic.getValue(BlockStateProperties.HORIZONTAL_FACING) == world.getValue(BlockStateProperties.HORIZONTAL_FACING);
            boolean sameHalf = schematic.getValue(BlockStateProperties.HALF) == world.getValue(BlockStateProperties.HALF);
            return sameFacing && sameHalf;
        }

        if (schematic.getBlock() instanceof TrapDoorBlock) {
            boolean sameFacing = schematic.getValue(BlockStateProperties.HORIZONTAL_FACING) == world.getValue(BlockStateProperties.HORIZONTAL_FACING);
            boolean sameHalf = schematic.getValue(BlockStateProperties.HALF) == world.getValue(BlockStateProperties.HALF);
            if (!sameFacing || !sameHalf) {
                return false;
            }

            // OPEN can be controlled by redstone on servers; don't force retries in powered state.
            boolean schematicPowered = schematic.hasProperty(BlockStateProperties.POWERED) && schematic.getValue(BlockStateProperties.POWERED);
            boolean worldPowered = world.hasProperty(BlockStateProperties.POWERED) && world.getValue(BlockStateProperties.POWERED);
            if (schematicPowered || worldPowered) {
                return true;
            }

            return schematic.getValue(BlockStateProperties.OPEN) == world.getValue(BlockStateProperties.OPEN);
        }

        if (schematic.getBlock() instanceof ShelfBlock || schematic.getBlock() instanceof LecternBlock) {
            return schematic.getValue(BlockStateProperties.HORIZONTAL_FACING) == world.getValue(BlockStateProperties.HORIZONTAL_FACING);
        }

        return schematic.equals(world);
    }

    private static boolean usePlacementStateOverride(BlockState blockState) {
        return blockState.getBlock() instanceof StairBlock
                || blockState.getBlock() instanceof TrapDoorBlock
                || blockState.getBlock() instanceof ShelfBlock
                || blockState.getBlock() instanceof LecternBlock;
    }

    private static void runExtraInteractions(
            Minecraft mc,
            MultiPlayerGameMode interactionManager,
            InteractionHand usedHand,
            RelativeBlockHitResult hitResult,
            int totalClicks,
            Block block,
            BlockPos targetPos
    ) {
        int extraClicks = Math.max(0, totalClicks - 1);
        if (extraClicks == 0) {
            return;
        }

        if (block instanceof TrapDoorBlock) {
            // Delay trapdoor toggles to avoid neighbor placements during high-speed desync windows.
            for (int i = 1; i <= extraClicks; i++) {
                int delay = i;
                TickThread.addCountDownTask(new RunnableWithCountDown.Builder().setCount(delay).build(() -> {
                    if (mc.player == null || mc.level == null) {
                        return;
                    }
                    BlockState current = mc.level.getBlockState(targetPos);
                    if (!(current.getBlock() instanceof TrapDoorBlock)) {
                        return;
                    }
                    interactionManager.useItemOn(
                            mc.player,
                            usedHand,
                            hitResult
                    );
                    mc.player.swing(usedHand);
                }));
            }
            return;
        }

        int i = 1;
        while (i < totalClicks) {
            interactionManager.useItemOn(
                    mc.player,
                    usedHand,
                    hitResult
            );
            mc.player.swing(usedHand);
            i++;
        }
    }
}
