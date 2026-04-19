package org.uiop.easyplacefix.Mixin.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.uiop.easyplacefix.IisSimpleHitPos;

@Mixin(ServerboundUseItemOnPacket.class)
public class MixinPlayerInteractBlockC2SPacket implements IisSimpleHitPos {
    @Shadow
    @Final
    private InteractionHand hand;
    @Shadow
    @Final
    private BlockHitResult blockHit;
    @Shadow
    @Final
    private int sequence;
    @Unique
    boolean isSimpleHitPos;

    @Override
    public void setSimpleHitPos() {
        isSimpleHitPos = true;
    }

    @Inject(method = "write", at = @At(value = "HEAD"), cancellable = true)
    public void operationWrite(FriendlyByteBuf buf, CallbackInfo ci) {
        if (isSimpleHitPos) {
            buf.writeEnum(hand);
            BlockPos blockPos = blockHit.getBlockPos();
            buf.writeBlockPos(blockPos);
            buf.writeEnum(blockHit.getDirection());
            Vec3 vec3d = blockHit.getLocation();
            buf.writeFloat((float) vec3d.x);
            buf.writeFloat((float) vec3d.y);
            buf.writeFloat((float) vec3d.z);
            buf.writeBoolean(blockHit.isInside());
            buf.writeVarInt(sequence);
            ci.cancel();
        }
    }
}
