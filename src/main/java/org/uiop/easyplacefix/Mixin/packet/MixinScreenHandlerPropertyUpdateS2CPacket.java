package org.uiop.easyplacefix.Mixin.packet;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundContainerSetDataPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.uiop.easyplacefix.until.PlayerBlockAction;

@Mixin(ClientboundContainerSetDataPacket.class)
public class MixinScreenHandlerPropertyUpdateS2CPacket {//Packet updates screen handler properties

    @WrapWithCondition(
            method = "handle(Lnet/minecraft/network/protocol/game/ClientGamePacketListener;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/network/protocol/game/ClientGamePacketListener;handleContainerSetData(Lnet/minecraft/network/protocol/game/ClientboundContainerSetDataPacket;)V"
            ))
    private boolean updateFail(ClientGamePacketListener instance, ClientboundContainerSetDataPacket screenHandlerPropertyUpdateS2CPacket) {
        return PlayerBlockAction.openScreenAction.run();
    }
}
