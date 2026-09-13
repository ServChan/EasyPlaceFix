package org.uiop.easyplacefix.Mixin;

import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.uiop.easyplacefix.until.ExcavationGuard;
import org.uiop.easyplacefix.until.ToolSwitcher;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class MixinExcavationGuard {
    @Inject(method = "breakBlock", at = @At("HEAD"), cancellable = true)
    private void easyplacefix$guardBreak(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (ExcavationGuard.shouldBlockBreak(pos)) cir.setReturnValue(false);
    }

    @Inject(method = "attackBlock", at = @At("HEAD"), cancellable = true)
    private void easyplacefix$guardAttack(BlockPos pos, Direction direction,
                                          CallbackInfoReturnable<Boolean> cir) {
        if (ExcavationGuard.shouldBlockBreak(pos)) {
            cir.setReturnValue(false);
            return;
        }
        ToolSwitcher.trySwitchTool(pos);
    }

    @Inject(method = "updateBlockBreakingProgress", at = @At("HEAD"), cancellable = true)
    private void easyplacefix$guardContinue(BlockPos pos, Direction direction,
                                            CallbackInfoReturnable<Boolean> cir) {
        if (ExcavationGuard.shouldBlockBreak(pos)) cir.setReturnValue(false);
    }
}
