package org.uiop.easyplacefix.mixin.config;

import fi.dy.masa.litematica.gui.GuiConfigs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.uiop.easyplacefix.EasyPlaceFix;

@Mixin(value = GuiConfigs.ConfigGuiTab.class, remap = false)
public interface ConfigGuiTabAccessor {
    @Invoker("<init>")
    static GuiConfigs.ConfigGuiTab init(String name, int ordinal, String translationKey) {
        throw new AssertionError();
    }
}
