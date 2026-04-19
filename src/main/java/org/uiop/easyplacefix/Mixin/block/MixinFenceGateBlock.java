package org.uiop.easyplacefix.Mixin.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.LookAt;
import org.uiop.easyplacefix.data.RelativeBlockHitResult;

@Mixin(FenceGateBlock.class)
public class MixinFenceGateBlock implements IBlock {
    @Override
    public Tuple<LookAt, LookAt> getYawAndPitch(BlockState blockState) {
        return switch (blockState.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
            case SOUTH -> new Tuple<>(LookAt.South, LookAt.Up);
            case WEST -> new Tuple<>(LookAt.West, LookAt.Up);
            case EAST -> new Tuple<>(LookAt.East, LookAt.Up);
            default -> new Tuple<>(LookAt.North, LookAt.Up);
        };
    }

    @Override
    public Tuple<RelativeBlockHitResult, Integer> getHitResult(BlockState blockState, BlockPos blockPos, BlockState worldBlockState) {
        Direction direction = blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);
        return new Tuple<>(new RelativeBlockHitResult(
                new Vec3(0.5, 0.5, 0.5),
                direction, blockPos, false
        ), blockState.getValue(BlockStateProperties.OPEN) ? 2 : 1);
    }
}
