package org.uiop.easyplacefix.Mixin;

import com.llamalad7.mixinextras.sugar.Local;
import fi.dy.masa.litematica.config.Configs;
import fi.dy.masa.litematica.util.EasyPlaceProtocol;
import fi.dy.masa.litematica.util.PlacementHandler;
import fi.dy.masa.litematica.util.RayTraceUtils;
import fi.dy.masa.litematica.util.WorldUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.uiop.easyplacefix.config.easyPlacefixConfig;

import static fi.dy.masa.litematica.util.WorldUtils.getValidBlockRange;
import static org.uiop.easyplacefix.until.doEasyPlace.doEasyPlace2;
import static org.uiop.easyplacefix.until.doEasyPlace.shouldAllowVanillaInteraction;

@Mixin(WorldUtils.class)
public abstract class MixinWorldUtils {

    @Inject(method = "doEasyPlaceAction", at = @At(value = "INVOKE", target = "Lfi/dy/masa/litematica/util/RayTraceUtils$RayTraceWrapper;getHitType()Lfi/dy/masa/litematica/util/RayTraceUtils$RayTraceWrapper$HitType;",ordinal = 0), cancellable = true,remap = false)
    private static void t1(MinecraftClient mc, CallbackInfoReturnable<ActionResult> cir, @Local RayTraceUtils.RayTraceWrapper traceWrapper){
        if (!easyPlacefixConfig.ENABLE_FIX.getBooleanValue()) {
            return;
        }

        if (shouldAllowVanillaInteraction(mc, traceWrapper)) {
            cir.setReturnValue(ActionResult.PASS);
            return;
        }

        if (PlacementHandler.getEffectiveProtocolVersion() != EasyPlaceProtocol.SLAB_ONLY) {
            return;
        }

        cir.setReturnValue(doEasyPlace2(mc, traceWrapper));
    }

}

