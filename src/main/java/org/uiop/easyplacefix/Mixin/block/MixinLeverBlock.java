package org.uiop.easyplacefix.Mixin.block;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.ICanUse;
import org.uiop.easyplacefix.data.RelativeBlockHitResult;
import org.uiop.easyplacefix.until.PlayerInputAction;

@Mixin(LeverBlock.class)
public abstract class MixinLeverBlock extends MixinWallMountedBlock implements IBlock {
    @Shadow
    @Final
    public static BooleanProperty POWERED;


    @Override
    public Tuple<RelativeBlockHitResult, Integer> getHitResult(BlockState blockState, BlockPos blockPos, BlockState worldBlockState) {
        AttachFace blockFace = blockState.getValue(BlockStateProperties.ATTACH_FACE);
        Direction direction = blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);
        return blockState.canSurvive(Minecraft.getInstance().level, blockPos) ?
                switch (blockFace) {//TODO TODO replace null with chained placement flow using position-aware easy place
                    case FLOOR -> new Tuple<>(
                            new RelativeBlockHitResult(new Vec3(0.5, 1, 0.5),
                                    Direction.UP,
                                    blockPos.below(), false
                            ), blockState.getValue(BlockStateProperties.POWERED) ? 2 : 1);
                    case CEILING -> new Tuple<>(
                            new RelativeBlockHitResult(new Vec3(0.5, 0, 0.5),
                                    Direction.DOWN,
                                    blockPos.above(), false
                            ), blockState.getValue(BlockStateProperties.POWERED) ? 2 : 1);

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
                            ), blockState.getValue(BlockStateProperties.POWERED) ? 2 : 1);
                } : null;
    }


    @Override
    public void afterAction(BlockState stateSchematic, BlockHitResult blockHitResult) {
        if (stateSchematic.getValue(BlockStateProperties.ATTACH_FACE) == AttachFace.CEILING) {
            BlockState blockState = Minecraft.getInstance().level.getBlockState(blockHitResult.getBlockPos().above());
            if (blockState.getBlock() instanceof ICanUse) {
                PlayerInputAction.SetShift(false);
            }

        } else if (stateSchematic.getValue(BlockStateProperties.ATTACH_FACE) == AttachFace.FLOOR) {
            BlockState blockState = Minecraft.getInstance().level.getBlockState(blockHitResult.getBlockPos().below());
            if (blockState.getBlock() instanceof ICanUse) {
                PlayerInputAction.SetShift(false);
            }
        } else {
            BlockState blockState = Minecraft.getInstance().level.getBlockState(blockHitResult.getBlockPos().relative(stateSchematic.getValue(BlockStateProperties.HORIZONTAL_FACING).getOpposite()));
            if (blockState.getBlock() instanceof ICanUse) {
                PlayerInputAction.SetShift(false);
            }
        }

    }

    @Override
    public void firstAction(BlockState stateSchematic, BlockHitResult blockHitResult) {
        if (stateSchematic.getValue(BlockStateProperties.ATTACH_FACE) == AttachFace.CEILING) {
            BlockState blockState = Minecraft.getInstance().level.getBlockState(blockHitResult.getBlockPos().above());
            if (blockState.getBlock() instanceof ICanUse) {
                PlayerInputAction.SetShift(true);
            }

        } else if (stateSchematic.getValue(BlockStateProperties.ATTACH_FACE) == AttachFace.FLOOR) {
            BlockState blockState = Minecraft.getInstance().level.getBlockState(blockHitResult.getBlockPos().below());
            if (blockState.getBlock() instanceof ICanUse) {
                PlayerInputAction.SetShift(true);
            }
        } else {
            BlockState blockState = Minecraft.getInstance().level.getBlockState(blockHitResult.getBlockPos().relative(stateSchematic.getValue(BlockStateProperties.HORIZONTAL_FACING).getOpposite()));
            if (blockState.getBlock() instanceof ICanUse) {
                PlayerInputAction.SetShift(true);
            }
        }


    }

}
