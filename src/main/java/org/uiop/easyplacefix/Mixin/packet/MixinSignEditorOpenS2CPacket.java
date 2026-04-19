package org.uiop.easyplacefix.Mixin.packet;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundOpenSignEditorPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.uiop.easyplacefix.until.PlayerBlockAction;

@Mixin(ClientboundOpenSignEditorPacket.class)
public class MixinSignEditorOpenS2CPacket {
    @WrapWithCondition(
            method = "handle(Lnet/minecraft/network/protocol/game/ClientGamePacketListener;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/protocol/game/ClientGamePacketListener;handleOpenSignEditor(Lnet/minecraft/network/protocol/game/ClientboundOpenSignEditorPacket;)V")
    )
    private boolean signEditorOpen(ClientGamePacketListener instance, ClientboundOpenSignEditorPacket signEditorOpenS2CPacket){
      return PlayerBlockAction.openSignEditorAction.run();
    }
}
