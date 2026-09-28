package org.uiop.easyplacefix.util;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import org.uiop.easyplacefix.ICanUse;

public final class SupportInteraction {
    private SupportInteraction() {
    }

    public static void sneakIfInteractive(BlockPos supportPos) {
        if (isInteractive(supportPos)) {
            PlayerInputAction.SetShift(true);
        }
    }

    public static void releaseIfInteractive(BlockPos supportPos) {
        if (isInteractive(supportPos)) {
            PlayerInputAction.SetShift(false);
        }
    }

    private static boolean isInteractive(BlockPos supportPos) {
        Minecraft mc = Minecraft.getInstance();
        return mc.level != null && mc.level.getBlockState(supportPos).getBlock() instanceof ICanUse;
    }
}
