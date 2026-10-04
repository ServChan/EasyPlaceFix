package org.uiop.easyplacefix.Mixin.byteBuf;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.uiop.easyplacefix.data.RelativeBlockHitResult;

@Mixin(PacketByteBuf.class)
public abstract class MixinWriteBlockHitResult {
    @Shadow
    public abstract PacketByteBuf writeBlockPos(BlockPos pos);

    @Shadow
    public abstract PacketByteBuf writeEnumConstant(Enum<?> instance);

    @Shadow
    public abstract PacketByteBuf writeFloat(float f);

    @Shadow
    public abstract PacketByteBuf writeBoolean(boolean bl);

    @WrapMethod(method = "writeBlockHitResult")
    public void w(BlockHitResult hitResult, Operation<Void> original) {
        if (hitResult instanceof RelativeBlockHitResult) {
            this.writeBlockPos(hitResult.getBlockPos());
            this.writeEnumConstant(hitResult.getSide());
            Vec3d vec3d = hitResult.getPos();
            this.writeFloat((float) vec3d.x);
            this.writeFloat((float) vec3d.y);
            this.writeFloat((float) vec3d.z);
            this.writeBoolean(hitResult.isInsideBlock());
            this.writeBoolean(hitResult.isAgainstWorldBorder());
        } else {
            original.call(hitResult);
        }

    }

}
