package org.uiop.easyplacefix.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import fi.dy.masa.litematica.util.EasyPlaceProtocol;
import fi.dy.masa.litematica.util.PlacementHandler;
import fi.dy.masa.litematica.util.RayTraceUtils;
import fi.dy.masa.litematica.util.WorldUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.uiop.easyplacefix.config.easyPlacefixConfig;

import static org.uiop.easyplacefix.util.EasyPlaceHandler.doEasyPlace2;
import static org.uiop.easyplacefix.util.EasyPlaceHandler.shouldAllowVanillaInteraction;

@Mixin(WorldUtils.class)
public abstract class MixinWorldUtils {
    @Inject(method = "doEasyPlaceAction", at = @At(value = "INVOKE", target = "Lfi/dy/masa/litematica/util/RayTraceUtils$RayTraceWrapper;getHitType()Lfi/dy/masa/litematica/util/RayTraceUtils$RayTraceWrapper$HitType;",ordinal = 0), cancellable = true,remap = false)
    private static void t1(Minecraft mc, CallbackInfoReturnable<InteractionResult> cir, @Local RayTraceUtils.RayTraceWrapper traceWrapper){
        if (!easyPlacefixConfig.ENABLE_FIX.getBooleanValue()) {
            return;
        }

        // Allow vanilla block interaction regardless of the active placement protocol.
        // In AUTO mode Servux selects V3, so this must run before the SLAB_ONLY guard.
        if (shouldAllowVanillaInteraction(mc, traceWrapper)) {
            cir.setReturnValue(InteractionResult.PASS);
            return;
        }

        if (PlacementHandler.getEffectiveProtocolVersion() != EasyPlaceProtocol.SLAB_ONLY) {
            return;
        }

        // EasyPlaceFix replaces the SLAB_ONLY path completely. Propagate PASS as well,
        // otherwise Litematica continues and can turn it into FAIL, blocking vanilla use.
        cir.setReturnValue(doEasyPlace2(mc, traceWrapper));
    }
}

