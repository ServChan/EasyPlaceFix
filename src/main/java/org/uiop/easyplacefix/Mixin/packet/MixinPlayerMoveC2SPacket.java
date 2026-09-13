package org.uiop.easyplacefix.Mixin.packet;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.tick_ins.tick.TickThread.isLookLocked;
import static com.tick_ins.tick.TickThread.pitchLock;
import static com.tick_ins.tick.TickThread.yawLock;

@Mixin(PlayerMoveC2SPacket.class)
public interface MixinPlayerMoveC2SPacket {
    @Mutable
    @Accessor
    void setYaw(float yaw);

    @Mutable
    @Accessor
    void setPitch(float pitch);

    @Mixin(PlayerMoveC2SPacket.Full.class)
    class Full {
        @Inject(method = "write", at = @At("HEAD"))
        private void lockLook(PacketByteBuf buf, CallbackInfo ci) {
            if (isLookLocked()) {
                ((MixinPlayerMoveC2SPacket) this).setYaw(yawLock);
                ((MixinPlayerMoveC2SPacket) this).setPitch(pitchLock);
            }
        }
    }

    @Mixin(PlayerMoveC2SPacket.LookAndOnGround.class)
    class LookAndOnGround {
        @Inject(method = "write", at = @At("HEAD"))
        private void lockLook(PacketByteBuf buf, CallbackInfo ci) {
            if (isLookLocked()) {
                ((MixinPlayerMoveC2SPacket) this).setYaw(yawLock);
                ((MixinPlayerMoveC2SPacket) this).setPitch(pitchLock);
            }
        }
    }
}
