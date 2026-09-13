package org.uiop.easyplacefix.until;

import com.tick_ins.tick.RunnableWithCountDown;
import com.tick_ins.tick.TickThread;
import fi.dy.masa.litematica.config.Configs;
import fi.dy.masa.litematica.util.RayTraceUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.uiop.easyplacefix.config.easyPlacefixConfig.TERRAIN_AUTO_REPLACE;

public final class TerrainAutoReplace {
    private static final Set<Block> ELIGIBLE = Set.of(
            Blocks.DIRT, Blocks.GRASS_BLOCK, Blocks.COARSE_DIRT, Blocks.DIRT_PATH);
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

    public static boolean tryClearThenRetry(MinecraftClient mc,
                                             RayTraceUtils.RayTraceWrapper traceWrapper,
                                             BlockPos pos,
                                             Direction direction) {
        BlockPos key = pos.toImmutable();
        if (!CLEARING.add(key)) return true;
        mc.interactionManager.attackBlock(key, direction);
        step(mc, traceWrapper, key, direction, MAX_TICKS);
        return true;
    }

    private static void step(MinecraftClient mc, RayTraceUtils.RayTraceWrapper traceWrapper,
                             BlockPos pos, Direction direction, int ticksLeft) {
        if (mc.world == null || mc.player == null || mc.interactionManager == null) {
            CLEARING.remove(pos);
            return;
        }

        BlockState current = mc.world.getBlockState(pos);
        if (!ELIGIBLE.contains(current.getBlock())) {
            CLEARING.remove(pos);
            doEasyPlace.doEasyPlace2(mc, traceWrapper);
            return;
        }

        if (ticksLeft <= 0) {
            mc.interactionManager.cancelBlockBreaking();
            CLEARING.remove(pos);
            return;
        }

        mc.interactionManager.updateBlockBreakingProgress(pos, direction);
        mc.player.swingHand(Hand.MAIN_HAND);
        TickThread.addCountDownTask(new RunnableWithCountDown.Builder()
                .setCount(1)
                .build(() -> step(mc, traceWrapper, pos, direction, ticksLeft - 1)));
    }

    public static void clear() {
        CLEARING.clear();
    }
}
