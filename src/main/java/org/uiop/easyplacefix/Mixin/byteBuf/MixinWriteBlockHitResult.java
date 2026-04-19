package org.uiop.easyplacefix.Mixin.byteBuf;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.uiop.easyplacefix.data.RelativeBlockHitResult;

@Mixin(FriendlyByteBuf.class)
public abstract class MixinWriteBlockHitResult {
    @Shadow
    public abstract FriendlyByteBuf writeBlockPos(BlockPos pos);

    @Shadow
    public abstract FriendlyByteBuf writeEnum(Enum<?> instance);

    @Shadow
    public abstract FriendlyByteBuf writeFloat(float f);

    @Shadow
    public abstract FriendlyByteBuf writeBoolean(boolean bl);

    @WrapMethod(method = "writeBlockHitResult")//TODO Custom packet encoding path
    public void w(BlockHitResult hitResult, Operation<Void> original) {
        if (hitResult instanceof RelativeBlockHitResult) {
            this.writeBlockPos(hitResult.getBlockPos());
            this.writeEnum(hitResult.getDirection());
            Vec3 vec3d = hitResult.getLocation();
            this.writeFloat((float) vec3d.x);
            this.writeFloat((float) vec3d.y);
            this.writeFloat((float) vec3d.z);
            this.writeBoolean(hitResult.isInside());
            this.writeBoolean(hitResult.isWorldBorderHit());
        } else {
            original.call(hitResult);
        }


    }


}
