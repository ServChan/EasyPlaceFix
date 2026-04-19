package org.uiop.easyplacefix.Mixin.block;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BellAttachType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.ICanUse;
import org.uiop.easyplacefix.LookAt;
import org.uiop.easyplacefix.data.RelativeBlockHitResult;
import org.uiop.easyplacefix.until.PlayerInputAction;

@Mixin(BellBlock.class)
public abstract class MixinBellBlock implements IBlock {

    @Shadow
    protected abstract boolean canSurvive(BlockState state, LevelReader world, BlockPos pos);

    @Override
    public Tuple<LookAt, LookAt> getYawAndPitch(BlockState blockState) {
        BellAttachType attachment = blockState.getValue(BlockStateProperties.BELL_ATTACHMENT);
        if (attachment == BellAttachType.DOUBLE_WALL || attachment == BellAttachType.SINGLE_WALL) return null;
        return switch (blockState.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
            case SOUTH -> new Tuple<>(LookAt.South, LookAt.Horizontal);
            case WEST -> new Tuple<>(LookAt.West, LookAt.Horizontal);
            case EAST -> new Tuple<>(LookAt.East, LookAt.Horizontal);
            default -> new Tuple<>(LookAt.North, LookAt.Horizontal);
        };
    }

    @Override
    public void afterAction(BlockState stateSchematic, BlockHitResult blockHitResult) {
        BellAttachType attachment = stateSchematic.getValue(BlockStateProperties.BELL_ATTACHMENT);
        BlockPos pos = blockHitResult.getBlockPos();
        BlockState blockState = switch (attachment) {
            case CEILING -> Minecraft.getInstance().level.getBlockState(pos.above());
            case FLOOR -> Minecraft.getInstance().level.getBlockState(pos.below());
            default -> Minecraft.getInstance().level.getBlockState(
                    pos.relative(stateSchematic.getValue(BlockStateProperties.HORIZONTAL_FACING))
            );
        };

        if (blockState.getBlock() instanceof ICanUse) {
            PlayerInputAction.SetShift(false);
        }
    }

    @Override
    public void firstAction(BlockState stateSchematic, BlockHitResult blockHitResult) {
        BellAttachType attachment = stateSchematic.getValue(BlockStateProperties.BELL_ATTACHMENT);
        BlockPos pos = blockHitResult.getBlockPos();
        BlockState blockState = switch (attachment) {
            case CEILING -> Minecraft.getInstance().level.getBlockState(pos.above());
            case FLOOR -> Minecraft.getInstance().level.getBlockState(pos.below());
            default -> Minecraft.getInstance().level.getBlockState(
                    pos.relative(stateSchematic.getValue(BlockStateProperties.HORIZONTAL_FACING))
            );
        };

        if (blockState.getBlock() instanceof ICanUse) {
            PlayerInputAction.SetShift(true);
        }
    }

    @Override
    public Tuple<RelativeBlockHitResult, Integer> getHitResult(BlockState blockState, BlockPos blockPos, BlockState
            worldBlockState) {
        Direction facing = blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);
        return this.canSurvive(blockState, Minecraft.getInstance().level, blockPos) ?
                new Tuple<>(switch (blockState.getValue(BlockStateProperties.BELL_ATTACHMENT)) {
                    case CEILING -> new RelativeBlockHitResult(
                            new Vec3(0.5, 0, 0.5),
                            Direction.DOWN,
                            blockPos.above(), false
                    );
                    case FLOOR -> new RelativeBlockHitResult(
                            new Vec3(0.5, 1, 0.5),
                            Direction.UP,
                            blockPos.below(), false
                    );
                    case SINGLE_WALL, DOUBLE_WALL -> new RelativeBlockHitResult(
                            switch (facing) {
                                case EAST -> new Vec3(1, 0.5, 0.5);
                                case SOUTH -> new Vec3(0.5, 0.5, 1);
                                case WEST -> new Vec3(0, 0.5, 0.5);
                                default -> new Vec3(0.5, 0.5, 0);
                            },
                            facing.getOpposite(),
                            blockPos.relative(facing),
                            false);
                }, 1) : null;
    }
}
