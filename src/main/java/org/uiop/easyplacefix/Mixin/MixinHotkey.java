package org.uiop.easyplacefix.mixin;

import fi.dy.masa.litematica.config.Hotkeys;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import static org.uiop.easyplacefix.config.easyPlaceFixHotkeys.getExtraHotkeys;

@Mixin(value = Hotkeys.class, remap = false)

public class MixinHotkey {
    @ModifyArg(method = "<clinit>", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableList;of(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;"), remap = false)
    private static Object[] modifyHotkeys(Object[] hotkeys) {
        return ArrayUtils.addAll(hotkeys, (Object[]) getExtraHotkeys());
    }
}

