package org.uiop.easyplacefix.mixin.config;


import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.gui.GuiConfigs;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.uiop.easyplacefix.EasyPlaceFix;

import java.util.Arrays;
import java.util.List;

import static org.uiop.easyplacefix.config.easyPlacefixConfig.getExtraGenericConfigs;

@Mixin(GuiConfigs.class)
public abstract class MixinGuiConfigs {

    @Inject(at =@At("HEAD"),method = "getConfigs", cancellable = true,remap = false)
    private void getConfigs(CallbackInfoReturnable<List<GuiConfigsBase.ConfigOptionWrapper>> cir) {
        GuiConfigs.ConfigGuiTab tab = DataManager.getConfigGuiTab();
        if (EasyPlaceFix.EASY_FIX.equals(tab)) {
            List<IConfigBase> list1 = Arrays.asList(getExtraGenericConfigs());
            cir.setReturnValue(GuiConfigsBase.ConfigOptionWrapper.createFor(list1));
        }
    }
}

