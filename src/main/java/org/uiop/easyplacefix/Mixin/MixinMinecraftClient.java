package org.uiop.easyplacefix.Mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.tick_ins.tick.TickThread;
import net.minecraft.client.Minecraft;
import org.uiop.easyplacefix.EasyPlaceFix;
import org.uiop.easyplacefix.until.PlayerBlockAction;

@Mixin(Minecraft.class)
public class MixinMinecraftClient {
    @Inject(method = "clearDownloadedResourcePacks()V",
            at = @At(value = "HEAD"),
            require = 0)
    private void disconnect(CallbackInfo ci) {
        EasyPlaceFix.screenId = 1;
        EasyPlaceFix.crafterOperation = false;
        PlayerBlockAction.openScreenAction.count = 0;
        PlayerBlockAction.openSignEditorAction.count = 0;
        PlayerBlockAction.useItemOnAction.modifyBoolean = false;
        PlayerBlockAction.useItemOnAction.clearPlacementStateOverride();
        PlayerBlockAction.useItemOnAction.lastPlacementTimeMap.clear();
        PlayerBlockAction.useItemOnAction.pistonBlockState = null;
        TickThread.onClientDisconnected();
    }

    @Inject(method = "destroy()V", at = @At("HEAD"), require = 0)
    private void easyplacefix$stopClient(CallbackInfo ci) {
        TickThread.onClientShutdown();
    }
}
