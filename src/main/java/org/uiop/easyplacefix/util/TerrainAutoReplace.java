package org.uiop.easyplacefix.util;

import com.tick_ins.tick.RunnableWithCountDown;
import com.tick_ins.tick.TickThread;
import fi.dy.masa.litematica.config.Configs;
import fi.dy.masa.litematica.util.RayTraceUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.uiop.easyplacefix.config.easyPlacefixConfig.BREAK_WRONG_BLOCKS;
import static org.uiop.easyplacefix.config.easyPlacefixConfig.TERRAIN_AUTO_REPLACE;
import static org.uiop.easyplacefix.util.PlacementDiagnostics.report;

public final class TerrainAutoReplace {
    private static final Set<Block> TERRAIN = Set.of(
            Blocks.DIRT, Blocks.GRASS_BLOCK, Blocks.COARSE_DIRT, Blocks.DIRT_PATH);

    private static final int TERRAIN_MAX_TICKS = 40;
    private static final int GENERAL_MAX_TICKS = 600;
    private static final int GENERAL_GRACE_TICKS = 20;

    private static final Set<BlockPos> CLEARING = ConcurrentHashMap.newKeySet();

    private TerrainAutoReplace() {
    }

    public static boolean isEligible(BlockPos pos, BlockState worldState, BlockState schematicState) {
        if (!Configs.Generic.EASY_PLACE_MODE.getBooleanValue()) {
            return false;
        }
        return isTerrainSwap(worldState, schematicState) || isWrongBlock(pos, worldState, schematicState);
    }

    private static boolean isTerrainSwap(BlockState worldState, BlockState schematicState) {
        return TERRAIN_AUTO_REPLACE.getBooleanValue()
                && worldState.getBlock() != schematicState.getBlock()
                && TERRAIN.contains(worldState.getBlock())
                && TERRAIN.contains(schematicState.getBlock());
    }

    private static boolean isWrongBlock(BlockPos pos, BlockState worldState, BlockState schematicState) {
        if (!BREAK_WRONG_BLOCKS.getBooleanValue() || schematicState.isAir()) {
            return false;
        }
        if (worldState.isAir() || worldState.getBlock() instanceof LiquidBlock) {
            return false;
        }
        if (!PlacementStateMatcher.isStructurallyWrong(schematicState, worldState)) {
            return false;
        }
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return false;
        }
        if (pos != null) {
            if (worldState.getDestroySpeed(level, pos) < 0.0F) {
                return false;
            }
            if (level.getBlockEntity(pos) instanceof Container) {
                return false;
            }
        }
        return true;
    }

    public static void clear() {
        CLEARING.clear();
    }

    public static boolean tryClearThenRetry(Minecraft mc, RayTraceUtils.RayTraceWrapper traceWrapper,
                                            BlockPos pos, Direction direction) {
        BlockPos key = pos.immutable();
        if (!CLEARING.add(key)) {
            return true;
        }

        BlockState worldState = mc.level.getBlockState(key);
        boolean terrain = TERRAIN.contains(worldState.getBlock());
        report(terrain ? "easyplacefix.diagnostic.terrain_clearing" : "easyplacefix.diagnostic.wrong_block_clearing",
                key.toShortString());
        mc.gameMode.startDestroyBlock(key, direction);

        int maxTicks = TERRAIN_MAX_TICKS;
        if (!terrain) {
            float progress = mc.level.getBlockState(key).getDestroyProgress(mc.player, mc.level, key);
            maxTicks = progress <= 0.0F
                    ? GENERAL_MAX_TICKS
                    : Math.min(GENERAL_MAX_TICKS, (int) Math.ceil(1.0F / progress) + GENERAL_GRACE_TICKS);
        }
        step(mc, traceWrapper, key, direction, maxTicks);
        return true;
    }

    private static boolean isCleared(BlockState state) {
        return state.isAir() || state.canBeReplaced();
    }

    private static void step(Minecraft mc, RayTraceUtils.RayTraceWrapper traceWrapper,
                             BlockPos pos, Direction direction, int ticksLeft) {
        if (mc.level == null || mc.player == null || mc.gameMode == null) {
            CLEARING.remove(pos);
            return;
        }

        BlockState current = mc.level.getBlockState(pos);
        if (isCleared(current)) {
            if (PlayerBlockAction.useItemOnAction.isGlobalPlacementCooling() && ticksLeft > 0) {
                scheduleStep(mc, traceWrapper, pos, direction, ticksLeft - 1);
                return;
            }
            CLEARING.remove(pos);
            PlayerBlockAction.useItemOnAction.lastPlacementTimeMap.remove(pos);
            EasyPlaceHandler.doEasyPlace2(mc, traceWrapper);
            return;
        }

        if (ticksLeft <= 0) {
            mc.gameMode.stopDestroyBlock();
            CLEARING.remove(pos);
            report("easyplacefix.diagnostic.terrain_clear_timeout", pos.toShortString());
            return;
        }

        mc.gameMode.continueDestroyBlock(pos, direction);
        mc.player.swing(InteractionHand.MAIN_HAND, mc.player.getItemInHand(InteractionHand.MAIN_HAND).getInteractAnimation(), false);
        scheduleStep(mc, traceWrapper, pos, direction, ticksLeft - 1);
    }

    private static void scheduleStep(Minecraft mc, RayTraceUtils.RayTraceWrapper traceWrapper,
                                     BlockPos pos, Direction direction, int ticksLeft) {
        TickThread.addCountDownTask(new RunnableWithCountDown.Builder()
                .setCount(1)
                .build(() -> step(mc, traceWrapper, pos, direction, ticksLeft)));
    }
}
