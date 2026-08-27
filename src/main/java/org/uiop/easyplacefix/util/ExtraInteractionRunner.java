package org.uiop.easyplacefix.util;

import com.tick_ins.tick.RunnableWithCountDown;
import com.tick_ins.tick.TickThread;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.uiop.easyplacefix.data.RelativeBlockHitResult;

public final class ExtraInteractionRunner {
    private ExtraInteractionRunner() {
    }

    /**
     * Some blocks need more than one right-click to reach the schematic state
     * (repeater delay, trapdoor toggles, ...). Sending all of those clicks in the
     * same tick produces a burst of interaction packets that server "timer"
     * anti-cheat flags. Spread every extra click across consecutive client ticks
     * so the client never sends more than one interaction per tick.
     */
    public static void run(
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

        // Two ticks per extra click: keeps this at most one interaction per tick even
        // when the player sweeps across a row of multi-click blocks (repeater rows).
        for (int i = 1; i <= extraClicks; i++) {
            TickThread.addCountDownTask(new RunnableWithCountDown.Builder().setCount(i * 2).build(() -> {
                if (mc.player == null || mc.level == null) {
                    return;
                }

                BlockState current = mc.level.getBlockState(targetPos);
                if (current.getBlock() != block) {
                    // The block was removed or replaced before we finished cycling it.
                    return;
                }

                interactionManager.useItemOn(mc.player, usedHand, hitResult);
                mc.player.swing(usedHand);
            }));
        }
    }
}
