package org.uiop.easyplacefix.Mixin.packet;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.uiop.easyplacefix.until.PlayerBlockAction;


@Mixin(ClientboundContainerClosePacket.class)
public class MixinCloseScreenS2CPacket {//Packet forcibly closes current screen from server

    @WrapWithCondition(
            method = "handle(Lnet/minecraft/network/protocol/game/ClientGamePacketListener;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/network/protocol/game/ClientGamePacketListener;handleContainerClose(Lnet/minecraft/network/protocol/game/ClientboundContainerClosePacket;)V"
            ))
    private boolean closeScreenFail(ClientGamePacketListener instance, ClientboundContainerClosePacket closeScreenS2CPacket) {
        return PlayerBlockAction.openScreenAction.run();
    }
}
