package org.uiop.easyplacefix.mixin;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.uiop.easyplacefix.util.ExcavationGuard;
import org.uiop.easyplacefix.util.ToolSwitcher;

@Mixin(MultiPlayerGameMode.class)
public abstract class MixinExcavationGuard {

    @Inject(method = "destroyBlock", at = @At("HEAD"), cancellable = true)
    private void easyplacefix$excavationGuardOnDestroyBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (ExcavationGuard.shouldBlockBreak(pos)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "startDestroyBlock", at = @At("HEAD"), cancellable = true)
    private void easyplacefix$excavationGuardOnStartDestroyBlock(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (ExcavationGuard.shouldBlockBreak(pos)) {
            cir.setReturnValue(false);
            return;
        }
        ToolSwitcher.trySwitchTool(pos);
    }

    @Inject(method = "continueDestroyBlock", at = @At("HEAD"), cancellable = true)
    private void easyplacefix$excavationGuardOnContinueDestroyBlock(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (ExcavationGuard.shouldBlockBreak(pos)) {
            cir.setReturnValue(false);
        }
    }
}
