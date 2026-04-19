package org.uiop.easyplacefix.Mixin.packet;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.uiop.easyplacefix.until.PlayerBlockAction;

@Mixin(ClientboundContainerSetContentPacket.class)
public class MixinInventoryS2CPacket {//Packet sends inventory slot contents

    @WrapWithCondition(
            method = "handle(Lnet/minecraft/network/protocol/game/ClientGamePacketListener;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/network/protocol/game/ClientGamePacketListener;handleContainerContent(Lnet/minecraft/network/protocol/game/ClientboundContainerSetContentPacket;)V"
            ))
    private boolean InventoryFail(ClientGamePacketListener instance, ClientboundContainerSetContentPacket inventoryS2CPacket) {
        return PlayerBlockAction.openScreenAction.run();
    }
}
