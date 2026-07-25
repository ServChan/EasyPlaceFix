package org.uiop.easyplacefix.mixin;


import fi.dy.masa.litematica.config.Configs;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import static org.uiop.easyplacefix.config.easyPlacefixConfig.getExtraGenericConfigs;

@Mixin(Configs.Generic.class)// Generic,
public class MixinConfigs {
    @ModifyArg(method = "<clinit>", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableList;of(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;"), remap = false)
    private static Object[] modifyConfigs(Object[] configs) {
        return ArrayUtils.addAll(configs, (Object[]) getExtraGenericConfigs());
    }
}
