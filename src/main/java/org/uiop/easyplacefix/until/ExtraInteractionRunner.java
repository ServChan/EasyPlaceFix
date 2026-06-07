package org.uiop.easyplacefix.until;

import com.tick_ins.tick.RunnableWithCountDown;
import com.tick_ins.tick.TickThread;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.uiop.easyplacefix.data.RelativeBlockHitResult;

public final class ExtraInteractionRunner {
    private ExtraInteractionRunner() {
    }

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

        if (block instanceof TrapDoorBlock) {
            delayTrapdoorToggles(mc, interactionManager, usedHand, hitResult, extraClicks, targetPos);
            return;
        }

        for (int i = 1; i < totalClicks; i++) {
            interactionManager.useItemOn(mc.player, usedHand, hitResult);
            mc.player.swing(usedHand);
        }
    }

    private static void delayTrapdoorToggles(
            Minecraft mc,
            MultiPlayerGameMode interactionManager,
            InteractionHand usedHand,
            RelativeBlockHitResult hitResult,
            int extraClicks,
            BlockPos targetPos
    ) {
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

                interactionManager.useItemOn(mc.player, usedHand, hitResult);
                mc.player.swing(usedHand);
            }));
        }
    }
}
