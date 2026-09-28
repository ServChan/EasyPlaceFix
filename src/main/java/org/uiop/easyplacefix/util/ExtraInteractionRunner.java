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

import static org.uiop.easyplacefix.config.easyPlacefixConfig.CLIENT_ROTATION_REVERT;

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
            BlockPos targetPos,
            Float yaw,
            Float pitch
    ) {
        int extraClicks = Math.max(0, totalClicks - 1);
        if (extraClicks == 0) {
            return;
        }

        RelativeBlockHitResult followUpHit = hitResult.getBlockPos().equals(targetPos)
                ? hitResult
                : new RelativeBlockHitResult(hitResult.getLocation(), hitResult.getDirection(), targetPos, false);

        for (int i = 1; i <= extraClicks; i++) {
            TickThread.addCountDownTask(new RunnableWithCountDown.Builder().setCount(i * 2).build(() -> {
                if (mc.player == null || mc.level == null || mc.player.isSecondaryUseActive()) {
                    return;
                }

                BlockState current = mc.level.getBlockState(targetPos);
                if (current.getBlock() != block) {
                    return;
                }

                boolean spoofRotation = yaw != null && pitch != null;
                if (spoofRotation) {
                    PlayerRotationAction.setServerBoundPlayerRotation(yaw, pitch, mc.player.horizontalCollision);
                }
                interactionManager.useItemOn(mc.player, usedHand, followUpHit);
                mc.player.swing(usedHand, mc.player.getItemInHand(usedHand).getInteractAnimation(), false);
                if (spoofRotation && CLIENT_ROTATION_REVERT.getBooleanValue()) {
                    PlayerRotationAction.restRotation();
                }
            }));
        }
    }
}
