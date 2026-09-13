package org.uiop.easyplacefix.util;

import com.tick_ins.tick.RunnableWithCountDown;
import com.tick_ins.tick.TickThread;
import fi.dy.masa.litematica.config.Configs;
import fi.dy.masa.litematica.util.RayTraceUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.uiop.easyplacefix.config.easyPlacefixConfig.TERRAIN_AUTO_REPLACE;
import static org.uiop.easyplacefix.util.PlacementDiagnostics.report;

/**
 * Optional companion to Easy Place: dirt, grass block (any snow state), and dirt path are cheap
 * terrain blocks with no meaningful drops to lose. When the schematic wants a different one of
 * these at a position that is already occupied by one of them, {@link #tryClearThenRetry} mines
 * the wrong one out - driving the same public {@link net.minecraft.client.multiplayer.MultiPlayerGameMode}
 * mining calls a held left-click would - and retries the placement once it is gone, instead of
 * Easy Place just failing with "not replaceable".
 */
public final class TerrainAutoReplace {
    private static final Set<Block> ELIGIBLE = Set.of(
            Blocks.DIRT, Blocks.GRASS_BLOCK, Blocks.COARSE_DIRT, Blocks.DIRT_PATH);
    // Safety cap so a rejected or stalled break (out of reach, protected, desync, ...)
    // cannot loop forever; any shovel clears these in a handful of ticks.
    private static final int MAX_TICKS = 40;

    private static final Set<BlockPos> CLEARING = ConcurrentHashMap.newKeySet();

    private TerrainAutoReplace() {
    }

    public static boolean isEligible(BlockState worldState, BlockState schematicState) {
        return TERRAIN_AUTO_REPLACE.getBooleanValue()
                && Configs.Generic.EASY_PLACE_MODE.getBooleanValue()
                && ELIGIBLE.contains(worldState.getBlock())
                && ELIGIBLE.contains(schematicState.getBlock());
    }

    public static void clear() {
        CLEARING.clear();
    }

    public static boolean tryClearThenRetry(Minecraft mc, RayTraceUtils.RayTraceWrapper traceWrapper,
                                             BlockPos pos, Direction direction) {
        BlockPos key = pos.immutable();
        if (!CLEARING.add(key)) {
            return true; // already clearing this position, do not start a second sequence
        }

        report("easyplacefix.diagnostic.terrain_clearing", key.toShortString());
        mc.gameMode.startDestroyBlock(key, direction);
        step(mc, traceWrapper, key, direction, MAX_TICKS);
        return true;
    }

    private static void step(Minecraft mc, RayTraceUtils.RayTraceWrapper traceWrapper,
                              BlockPos pos, Direction direction, int ticksLeft) {
        if (mc.level == null || mc.player == null || mc.gameMode == null) {
            CLEARING.remove(pos);
            return;
        }

        BlockState current = mc.level.getBlockState(pos);
        if (!ELIGIBLE.contains(current.getBlock())) {
            // Broken (or otherwise changed) - retry the placement now that the spot is clear.
            CLEARING.remove(pos);
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
        mc.player.swing(InteractionHand.MAIN_HAND);
        TickThread.addCountDownTask(new RunnableWithCountDown.Builder()
                .setCount(1)
                .build(() -> step(mc, traceWrapper, pos, direction, ticksLeft - 1)));
    }
}
