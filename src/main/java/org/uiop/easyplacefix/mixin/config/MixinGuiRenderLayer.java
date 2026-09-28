package org.uiop.easyplacefix.mixin.config;

import fi.dy.masa.litematica.gui.GuiConfigs;
import fi.dy.masa.litematica.gui.GuiRenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.uiop.easyplacefix.EasyPlaceFix;

@Mixin(value = GuiRenderLayer.class, remap = false)
public abstract class MixinGuiRenderLayer {

    @Shadow
    protected abstract int createTabButton(int x, int y, int width, GuiConfigs.ConfigGuiTab tab);

    @Inject(method = "createTabButton", at = @At("RETURN"), cancellable = true)
    private void appendEasyFixTabButton(int x, int y, int width, GuiConfigs.ConfigGuiTab tab, CallbackInfoReturnable<Integer> cir) {
        if (tab == GuiConfigs.ConfigGuiTab.RENDER_LAYERS) {
            int nextX = x + cir.getReturnValue();
            int newX = this.createTabButton(nextX, y, width, EasyPlaceFix.EASY_FIX);
            cir.setReturnValue(newX);
        }
    }
}
