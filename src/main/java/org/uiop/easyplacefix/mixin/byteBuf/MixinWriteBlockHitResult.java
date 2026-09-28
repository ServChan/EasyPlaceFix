package org.uiop.easyplacefix.mixin.byteBuf;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.data.RelativeBlockHitResult;

@Mixin(targets = "net.minecraft.world.phys.BlockHitResult$1")
public abstract class MixinWriteBlockHitResult {

    @WrapMethod(method = "encode(Lio/netty/buffer/ByteBuf;Lnet/minecraft/world/phys/BlockHitResult;)V")
    public void w(ByteBuf buf, BlockHitResult hitResult, Operation<Void> original) {
        if (hitResult instanceof RelativeBlockHitResult) {
            BlockPos.STREAM_CODEC.encode(buf, hitResult.getBlockPos());
            Direction.STREAM_CODEC.encode(buf, hitResult.getDirection());
            Vec3 vec3d = hitResult.getLocation();
            buf.writeFloat((float) vec3d.x);
            buf.writeFloat((float) vec3d.y);
            buf.writeFloat((float) vec3d.z);
            buf.writeBoolean(hitResult.isInside());
            buf.writeBoolean(hitResult.isWorldBorderHit());
        } else {
            original.call(buf, hitResult);
        }

    }

}
