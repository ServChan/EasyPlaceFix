package org.uiop.easyplacefix.Mixin.block;

import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.BedPart;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.LookAt;
import org.uiop.easyplacefix.data.RelativeBlockHitResult;

@Mixin(BedBlock.class)
public class MixinBedBlock implements IBlock {

    @Override
    public Pair<LookAt, LookAt> getYawAndPitch(BlockState blockState) {
        return switch (blockState.get(Properties.HORIZONTAL_FACING)) {
            case SOUTH -> new Pair<>(LookAt.South, LookAt.Horizontal);
            case WEST -> new Pair<>(LookAt.West, LookAt.Horizontal);
            case EAST -> new Pair<>(LookAt.East, LookAt.Horizontal);
            default -> new Pair<>(LookAt.North, LookAt.Horizontal);
        };
    }

    @Override
    public Pair<RelativeBlockHitResult, Integer> getHitResult(BlockState blockState, BlockPos blockPos, BlockState worldBlockState) {
        Direction direction = blockState.get(Properties.HORIZONTAL_FACING);
        if (blockState.get(Properties.BED_PART) == BedPart.HEAD) blockPos = blockPos.offset(direction.getOpposite());
        return new Pair<>(new RelativeBlockHitResult(
                new Vec3d(0.5, 0.5, 0.5),
                Direction.UP,
                blockPos, false
        ), 1);
    }
}
