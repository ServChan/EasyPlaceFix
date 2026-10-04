package org.uiop.easyplacefix.Mixin.packet;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.uiop.easyplacefix.until.PlayerBlockAction;

@Mixin(InventoryS2CPacket.class)
public class MixinInventoryS2CPacket {

    @WrapWithCondition(
            method = "apply(Lnet/minecraft/network/listener/ClientPlayPacketListener;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/network/listener/ClientPlayPacketListener;" +
                            "onInventory(Lnet/minecraft/network/packet/s2c/play/InventoryS2CPacket;)V"
            ))
    private boolean InventoryFail(ClientPlayPacketListener instance, InventoryS2CPacket inventoryS2CPacket) {
        return PlayerBlockAction.openScreenAction.run();
    }
}
