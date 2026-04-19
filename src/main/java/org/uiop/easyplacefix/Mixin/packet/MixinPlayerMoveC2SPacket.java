package org.uiop.easyplacefix.Mixin.packet;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.tick_ins.tick.TickThread.notChangPlayerLook;
import static com.tick_ins.tick.TickThread.pitchLock;
import static com.tick_ins.tick.TickThread.yawLock;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

@Mixin(ServerboundMovePlayerPacket.class)
public interface MixinPlayerMoveC2SPacket {
    @Mutable
    @Accessor
    void setYRot(float yaw);

    @Mutable
    @Accessor
    void setXRot(float pitch);

    @Mixin(ServerboundMovePlayerPacket.PosRot.class)
    class Full {
        @Inject(method = "write", at = @At("HEAD"))
        private void lockLook(FriendlyByteBuf buf, CallbackInfo ci) {
            if (notChangPlayerLook) {
                ((MixinPlayerMoveC2SPacket) this).setYRot(yawLock);
                ((MixinPlayerMoveC2SPacket) this).setXRot(pitchLock);
            }
        }
    }

    @Mixin(ServerboundMovePlayerPacket.Rot.class)
    class LookAndOnGround {
        @Inject(method = "write", at = @At("HEAD"))
        private void lockLook(FriendlyByteBuf buf, CallbackInfo ci) {
            if (notChangPlayerLook) {
                ((MixinPlayerMoveC2SPacket) this).setYRot(yawLock);
                ((MixinPlayerMoveC2SPacket) this).setXRot(pitchLock);
            }
        }
    }
}
