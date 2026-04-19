package org.uiop.easyplacefix.Mixin.block;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.data.RelativeBlockHitResult;

@Mixin(CocoaBlock.class)
public abstract class MixinCocoaBlock implements IBlock {
    @Shadow
    protected abstract boolean canSurvive(BlockState state, LevelReader world, BlockPos pos);

    @Override
    public Tuple<RelativeBlockHitResult, Integer> getHitResult(BlockState blockState, BlockPos blockPos, BlockState worldBlockState) {
        Direction direction = blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);
        return this.canSurvive(blockState, Minecraft.getInstance().level, blockPos) ?
                new Tuple<>(
                        new RelativeBlockHitResult(
                                switch (direction) {
                                    case EAST -> new Vec3(1, 0.5, 0.5);
                                    case SOUTH -> new Vec3(0.5, 0.5, 1);
                                    case WEST -> new Vec3(0, 0.5, 0.5);
                                    default -> new Vec3(0.5, 0.5, 0);
                                },
                                direction,
                                blockPos.relative(direction.getOpposite()),
                                false
                        ), 1
                ) : null;
    }
}
