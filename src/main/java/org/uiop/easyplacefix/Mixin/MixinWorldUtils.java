package org.uiop.easyplacefix.Mixin;

import com.llamalad7.mixinextras.sugar.Local;
import fi.dy.masa.litematica.config.Configs;
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

import static fi.dy.masa.litematica.util.WorldUtils.getValidBlockRange;
import static org.uiop.easyplacefix.until.doEasyPlace.doEasyPlace2;

@Mixin(WorldUtils.class)
public abstract class MixinWorldUtils {
    //    @WrapMethod(method = "doEasyPlaceAction")
//    private static ActionResult fix(MinecraftClient mc, Operation<ActionResult> original) {//Main logic entry point
//        if (PlacementHandler.getEffectiveProtocolVersion() == EasyPlaceProtocol.SLAB_ONLY) {
//            RayTraceUtils.RayTraceWrapper traceWrapper;
//
//            double traceMaxRange = getValidBlockRange(mc);
//            HitResult traceVanilla = RayTraceUtils.getRayTraceFromEntity(mc.world, mc.player, false, traceMaxRange);
//            if (Configs.Generic.EASY_PLACE_FIRST.getBooleanValue()) {
//                // Temporary hack, using this same config here
//                boolean targetFluids = Configs.InfoOverlays.INFO_OVERLAYS_TARGET_FLUIDS.getBooleanValue();
//                traceWrapper = RayTraceUtils.getGenericTrace(mc.world, mc.player, traceMaxRange, true, targetFluids, false);
//            } else {
////            Configs.Generic.EASY_PLACE_FIRST.setBooleanValue(true); Temporary workaround
//                traceWrapper = RayTraceUtils.getFurthestSchematicWorldTraceBeforeVanilla(mc.world, mc.player, traceMaxRange);
//            }
//            if (traceWrapper == null) return ActionResult.PASS;
//            return doEasyPlace2(mc, traceVanilla, traceWrapper);
//        }
//        return original.call(mc);
//    }
    @Inject(method = "doEasyPlaceAction", at = @At(value = "INVOKE", target = "Lfi/dy/masa/litematica/util/RayTraceUtils$RayTraceWrapper;getHitType()Lfi/dy/masa/litematica/util/RayTraceUtils$RayTraceWrapper$HitType;",ordinal = 0), cancellable = true,remap = false)
    private static void t1(Minecraft mc, CallbackInfoReturnable<InteractionResult> cir, @Local RayTraceUtils.RayTraceWrapper traceWrapper){
        if (!easyPlacefixConfig.ENABLE_FIX.getBooleanValue()) {
            return;
        }

        if (PlacementHandler.getEffectiveProtocolVersion() != EasyPlaceProtocol.SLAB_ONLY) {
            return;
        }

        InteractionResult result = doEasyPlace2(mc, traceWrapper);
        // Consume both SUCCESS and FAIL so vanilla EasyPlace won't run as a fallback
        // and cause duplicate/contradicting interactions on servers.
        if (result != InteractionResult.PASS) {
            cir.setReturnValue(result);
        }
    }
//    @Inject(method = "doEasyPlaceAction", at = @At("HEAD"), cancellable = true)
//    private static void aa(MinecraftClient mc, CallbackInfoReturnable<ActionResult> cir) {
//        if (PlacementHandler.getEffectiveProtocolVersion() == EasyPlaceProtocol.SLAB_ONLY) {
//            RayTraceUtils.RayTraceWrapper traceWrapper;
//
//            double traceMaxRange = getValidBlockRange(mc);
//            if (Configs.Generic.EASY_PLACE_FIRST.getBooleanValue()) {
//                boolean targetFluids = Configs.InfoOverlays.INFO_OVERLAYS_TARGET_FLUIDS.getBooleanValue();
//                traceWrapper = RayTraceUtils.getGenericTrace(mc.world, mc.player, traceMaxRange, true, targetFluids, false);
//
//            } else {
////            Configs.Generic.EASY_PLACE_FIRST.setBooleanValue(true); Temporary workaround
//                traceWrapper = RayTraceUtils.getFurthestSchematicWorldTraceBeforeVanilla(mc.world, mc.player, traceMaxRange);
//            }
//            if (traceWrapper == null) {
//                cir.setReturnValue(ActionResult.PASS);
//                return;
//            }
//            cir.setReturnValue(doEasyPlace2(mc, traceWrapper));
//        }
//
//    }


//    @Inject(method = "doEasyPlaceAction",
//            at = @At(value = "INVOKE",
//                    target = "Lfi/dy/masa/litematica/util/RayTraceUtils$RayTraceWrapper;" +
//                            "getHitType()Lfi/dy/masa/litematica/util/RayTraceUtils$RayTraceWrapper$HitType;"),
//    locals = LocalCapture.CAPTURE_FAILHARD)
//    private static void getHitType(MinecraftClient mc, CallbackInfoReturnable<ActionResult> cir) {
//        if(PlacementHandler.getEffectiveProtocolVersion() == EasyPlaceProtocol.SLAB_ONLY){
//            doEasyPlace2(mc,traceWrapper);
//        }
//    }
//@Inject(
//        method = "doEasyPlaceAction",
//        at = @At(
//                value = "INVOKE",
//                target = "Lfi/dy/masa/litematica/util/RayTraceUtils$RayTraceWrapper;getHitType()Lfi/dy/masa/litematica/util/RayTraceUtils$RayTraceWrapper$HitType;"
//        ),
//        locals = LocalCapture.CAPTURE_FAILSOFT // Capture local variables
//)
//private static void onAfterGetHitType(MinecraftClient mc, CallbackInfoReturnable<ActionResult> cir, RayTraceUtils.RayTraceWrapper traceWrapper) {
//    if ( PlacementHandler.getEffectiveProtocolVersion() == EasyPlaceProtocol.SLAB_ONLY) {
//        // Inject logic here
//        doEasyPlace2(mc, traceWrapper);
//        // Override return value
//        cir.setReturnValue(ActionResult.SUCCESS); // Override return value
//    }
//}
}



