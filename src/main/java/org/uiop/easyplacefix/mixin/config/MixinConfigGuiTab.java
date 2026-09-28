package org.uiop.easyplacefix.mixin.config;

import fi.dy.masa.litematica.gui.GuiConfigs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.uiop.easyplacefix.EasyPlaceFix;

@Mixin(GuiConfigs.ConfigGuiTab.class)
public class MixinConfigGuiTab {

    @Inject(method = "values", at = @At("RETURN"), cancellable = true, remap = false)
    private static void values(CallbackInfoReturnable<GuiConfigs.ConfigGuiTab[]> cir) {
        GuiConfigs.ConfigGuiTab[] returnValue = cir.getReturnValue();
        for (GuiConfigs.ConfigGuiTab tab : returnValue) {
            if (EasyPlaceFix.EASY_FIX.equals(tab)) {
                return;
            }
        }
        GuiConfigs.ConfigGuiTab[] arr = new GuiConfigs.ConfigGuiTab[returnValue.length + 1];
        System.arraycopy(returnValue, 0, arr, 0, returnValue.length);
        arr[arr.length - 1] = EasyPlaceFix.EASY_FIX;
        cir.setReturnValue(arr);
    }
}
