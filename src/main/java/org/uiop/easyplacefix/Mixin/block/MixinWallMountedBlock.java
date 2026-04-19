package org.uiop.easyplacefix.Mixin.block;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.ICanUse;
import org.uiop.easyplacefix.LookAt;
import org.uiop.easyplacefix.data.RelativeBlockHitResult;
import org.uiop.easyplacefix.until.PlayerBlockAction;
import org.uiop.easyplacefix.until.PlayerInputAction;

@Mixin(FaceAttachedHorizontalDirectionalBlock.class)//Lever,button
public abstract class MixinWallMountedBlock implements IBlock {
    @Shadow
    protected abstract boolean canSurvive(BlockState state, LevelReader world, BlockPos pos);
//    @Override
//    public void afterAction(BlockState stateSchematic, BlockHitResult blockHitResult) {
//        BlockState blockState = MinecraftClient.getInstance().world.getBlockState(blockHitResult.getBlockPos().offset(stateSchematic.get(Properties.FACING).getOpposite()));
//        if (blockState.getBlock() instanceof ICanUse){
//            PlayerInputAction.SetShift(false);
//        }
//    }
//
//    @Override
//    public void firstAction(BlockState stateSchematic, BlockHitResult blockHitResult) {
//        BlockState blockState = MinecraftClient.getInstance().world.getBlockState(blockHitResult.getBlockPos().offset(stateSchematic.get(Properties.FACING).getOpposite()));
//        if (blockState.getBlock() instanceof ICanUse){
//            PlayerInputAction.SetShift(true);
//        }
//    }
    @Override
    public Tuple<LookAt, LookAt> getYawAndPitch(BlockState blockState) {
        return switch (blockState.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
            case SOUTH -> new Tuple<>(LookAt.South, LookAt.Horizontal);
            case WEST -> new Tuple<>(LookAt.West, LookAt.Horizontal);
            case EAST -> new Tuple<>(LookAt.East, LookAt.Horizontal);
            default -> new Tuple<>(LookAt.North, LookAt.Horizontal);
        };
    }

    @Override
    public Tuple<RelativeBlockHitResult, Integer> getHitResult(BlockState blockState, BlockPos blockPos, BlockState worldBlockState) {
        AttachFace blockFace = blockState.getValue(BlockStateProperties.ATTACH_FACE);
        Direction direction = blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);
        return canSurvive(blockState, Minecraft.getInstance().level, blockPos) ?
                switch (blockFace) {//TODO TODO replace null with chained placement flow using position-aware easy place
                    case FLOOR -> new Tuple<>(
                            new RelativeBlockHitResult(new Vec3(0.5, 1, 0.5),
                                    Direction.UP,
                                    blockPos.below(), false
                            ), 1);
                    case CEILING -> new Tuple<>(
                            new RelativeBlockHitResult(new Vec3(0.5, 0, 0.5),
                                    Direction.DOWN,
                                    blockPos.above(), false
                            ), 1);

                    case WALL -> new Tuple<>(
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
                            ), 1);
                } : null;
    }
}
