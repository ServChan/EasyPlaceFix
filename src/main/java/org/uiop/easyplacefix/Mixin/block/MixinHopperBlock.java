package org.uiop.easyplacefix.Mixin.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.data.RelativeBlockHitResult;

@Mixin(HopperBlock.class)
public class MixinHopperBlock implements IBlock {
    @Override
    public Tuple<RelativeBlockHitResult, Integer> getHitResult(BlockState blockState, BlockPos blockPos, BlockState worldBlockState) {
        return new Tuple<>(new RelativeBlockHitResult(
                new Vec3(0.5, 0.5, 0.5),
                switch (blockState.getValue(BlockStateProperties.FACING_HOPPER)) {
                    case SOUTH -> Direction.NORTH;
                    case EAST -> Direction.WEST;
                    case WEST -> Direction.EAST;
                    case NORTH -> Direction.SOUTH;
                    default -> Direction.UP;
                },
                blockPos, false
        ), 1);
    }
}
