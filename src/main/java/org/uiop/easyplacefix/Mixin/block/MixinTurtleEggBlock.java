package org.uiop.easyplacefix.Mixin.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.TurtleEggBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.data.RelativeBlockHitResult;

@Mixin(TurtleEggBlock.class)
public abstract class MixinTurtleEggBlock implements IBlock {
    @Shadow public abstract void afterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool);

    @Override
    public Pair<RelativeBlockHitResult, Integer> getHitResult(BlockState blockState, BlockPos blockPos, BlockState worldBlockState) {

        int count;
        if (worldBlockState.getBlock()==blockState.getBlock()){
            count = blockState.get(Properties.EGGS)-worldBlockState.get(Properties.EGGS);
           if (count<1)return null;
        }else {
            count = blockState.get(Properties.EGGS);
        }

        return new Pair<>(new RelativeBlockHitResult(
                new Vec3d(0.5, 0.5, 0.5),
                Direction.UP,
                blockPos, false
        ),count);
    }
}
