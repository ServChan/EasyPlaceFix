package org.uiop.easyplacefix.mixin.AccessorMixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {
    @Accessor("leftPos")
    int easyplacefix$getLeftPos();

    @Accessor("topPos")
    int easyplacefix$getTopPos();

    @Accessor("imageWidth")
    int easyplacefix$getImageWidth();
}
