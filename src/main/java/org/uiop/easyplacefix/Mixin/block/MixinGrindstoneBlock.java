package org.uiop.easyplacefix.Mixin.block;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.GrindstoneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
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

@Mixin(GrindstoneBlock.class)
public abstract class MixinGrindstoneBlock implements IBlock {
    @Override
    public boolean HasSleepTime(BlockState blockState) {
        return blockState.getValue(BlockStateProperties.ATTACH_FACE) == AttachFace.WALL;

    }

    @Override
    public Tuple<LookAt, LookAt> getYawAndPitch(BlockState blockState) {
        if (blockState.getValue(BlockStateProperties.ATTACH_FACE) != AttachFace.WALL)
            return switch (blockState.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
                case SOUTH -> new Tuple<>(LookAt.South, LookAt.Horizontal);
                case WEST -> new Tuple<>(LookAt.West, LookAt.Horizontal);
                case EAST -> new Tuple<>(LookAt.East, LookAt.Horizontal);
                default -> new Tuple<>(LookAt.North, LookAt.Horizontal);
            };
        return switch (blockState.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
            case SOUTH -> new Tuple<>(LookAt.North, LookAt.Horizontal);
            case WEST -> new Tuple<>(LookAt.East, LookAt.Horizontal);
            case EAST -> new Tuple<>(LookAt.West, LookAt.Horizontal);
            default -> new Tuple<>(LookAt.South, LookAt.Horizontal);
        };
    }
    @Shadow
    protected abstract boolean canSurvive(BlockState state, LevelReader world, BlockPos pos);
    @Override
    public Tuple<RelativeBlockHitResult, Integer> getHitResult(BlockState blockState, BlockPos blockPos, BlockState worldBlockState) {
        AttachFace blockFace = blockState.getValue(BlockStateProperties.ATTACH_FACE);
        Direction direction = blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);
        return
                switch (blockFace) {//TODO TODO replace null with chained placement flow using position-aware easy place
                    case FLOOR ->
                            canSurvive(blockState,Minecraft.getInstance().level, blockPos) ? new Tuple<>(
                                    new RelativeBlockHitResult(new Vec3(0.5, 1, 0.5),
                                            Direction.UP,
                                            blockPos.below(), false
                                    ), 1) : null;
                    case CEILING ->
                            canSurvive(blockState,Minecraft.getInstance().level, blockPos) ? new Tuple<>(
                                    new RelativeBlockHitResult(new Vec3(0.5, 0, 0.5),
                                            Direction.DOWN,
                                            blockPos.above(), false
                                    ), 1) : null;

                    case WALL -> new Tuple<>(new RelativeBlockHitResult(
                            new Vec3(0.5, 0.5, 0.5),
                            direction,
                            blockPos,
                            false), 1);
                };

    }

    @Override
    public void afterAction(BlockState stateSchematic, BlockHitResult blockHitResult) {
        if (stateSchematic.getValue(BlockStateProperties.ATTACH_FACE) == AttachFace.CEILING){
            BlockState blockState = Minecraft.getInstance().level.getBlockState(blockHitResult.getBlockPos().above());
            if (blockState.getBlock() instanceof ICanUse){
                PlayerInputAction.SetShift(false);
            }

        }else if (stateSchematic.getValue(BlockStateProperties.ATTACH_FACE) == AttachFace.FLOOR){
            BlockState blockState = Minecraft.getInstance().level.getBlockState(blockHitResult.getBlockPos().below());
            if (blockState.getBlock() instanceof ICanUse){
                PlayerInputAction.SetShift(false);
            }
        }
    }

    @Override
    public void firstAction(BlockState stateSchematic, BlockHitResult blockHitResult) {
        if (stateSchematic.getValue(BlockStateProperties.ATTACH_FACE) == AttachFace.CEILING){
            BlockState blockState = Minecraft.getInstance().level.getBlockState(blockHitResult.getBlockPos().above());
            if (blockState.getBlock() instanceof ICanUse){
                PlayerInputAction.SetShift(true);
            }

        }else if (stateSchematic.getValue(BlockStateProperties.ATTACH_FACE) == AttachFace.FLOOR){
            BlockState blockState = Minecraft.getInstance().level.getBlockState(blockHitResult.getBlockPos().below());
            if (blockState.getBlock() instanceof ICanUse){
                PlayerInputAction.SetShift(true);
            }
        }
    }
}
