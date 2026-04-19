package org.uiop.easyplacefix.Mixin.block;

import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.data.RelativeBlockHitResult;

import static net.minecraft.core.Direction.Axis;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

@Mixin(RotatedPillarBlock.class)
public class MixinPillarBlock implements IBlock {
    @Override
    public Tuple<RelativeBlockHitResult, Integer> getHitResult(BlockState blockState, BlockPos blockPos, BlockState worldBlockState) {
        Axis axis = blockState.getValue(BlockStateProperties.AXIS);

        return new Tuple<>(new RelativeBlockHitResult(
                new Vec3(0.5, 0.5, 0.5),
                switch (axis) {
                    case X -> Direction.EAST;//x
                    case Y -> Direction.DOWN;//y
                    case Z -> Direction.NORTH;//z
                },
                blockPos, false
        ), 1);
    }
}
