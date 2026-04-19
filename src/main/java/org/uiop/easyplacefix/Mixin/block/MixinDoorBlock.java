package org.uiop.easyplacefix.Mixin.block;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.ICanUse;
import org.uiop.easyplacefix.LookAt;
import org.uiop.easyplacefix.data.RelativeBlockHitResult;
import org.uiop.easyplacefix.until.PlayerBlockAction;
import org.uiop.easyplacefix.until.PlayerInputAction;

@Mixin(DoorBlock.class)
public abstract class MixinDoorBlock implements IBlock {
    @Shadow
    protected abstract boolean canSurvive(BlockState state, LevelReader world, BlockPos pos);

    @Shadow
    @Final
    private BlockSetType type;

    @Shadow
    @Final
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    @Override
    public Tuple<LookAt, LookAt> getYawAndPitch(BlockState blockState) {
        return switch (blockState.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
            case SOUTH -> new Tuple<>(LookAt.South, LookAt.PlayerPitch);
            case WEST -> new Tuple<>(LookAt.West, LookAt.PlayerPitch);
            case EAST -> new Tuple<>(LookAt.East, LookAt.PlayerPitch);
            default -> new Tuple<>(LookAt.North, LookAt.PlayerPitch);
        };
    }

    @Override
    public Tuple<RelativeBlockHitResult, Integer> getHitResult(BlockState blockState, BlockPos blockPos, BlockState worldBlockState) {
        Direction direction = blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);
        DoorHingeSide doorHinge = blockState.getValue(BlockStateProperties.DOOR_HINGE);
        if (blockState.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER) {
            blockPos = blockPos.below();
           blockState = blockState.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF,DoubleBlockHalf.LOWER);
        }
        return this.canSurvive(blockState, Minecraft.getInstance().level, blockPos) ?
                new Tuple<>(switch (direction) {
                    case SOUTH -> {
                        if (doorHinge == DoorHingeSide.LEFT)
                            yield new RelativeBlockHitResult(new Vec3(0.8, 1, 0), Direction.UP, blockPos.below(), false);
                        else
                            yield new RelativeBlockHitResult(new Vec3(0.2, 1, 0), Direction.UP, blockPos.below(), false);

                    }
                    case WEST -> {
                        if (doorHinge == DoorHingeSide.LEFT)
                            yield new RelativeBlockHitResult(new Vec3(0, 1, 0.8), Direction.UP, blockPos.below(), false);
                        else
                            yield new RelativeBlockHitResult(new Vec3(0, 1, 0.2), Direction.UP, blockPos.below(), false);

                    }
                    case EAST -> {
                        if (doorHinge == DoorHingeSide.LEFT)
                            yield new RelativeBlockHitResult(new Vec3(0, 1, 0.2), Direction.UP, blockPos.below(), false);
                        else
                            yield new RelativeBlockHitResult(new Vec3(0, 1, 0.8), Direction.UP, blockPos.below(), false);

                    }
                    default -> {
                        if (doorHinge == DoorHingeSide.LEFT)
                            yield new RelativeBlockHitResult(new Vec3(0.2, 0, 0), Direction.UP, blockPos, false);
                        else
                            yield new RelativeBlockHitResult(new Vec3(0.8, 0, 0), Direction.UP, blockPos, false);

                    }
                }, blockState.getValue(BlockStateProperties.OPEN) && this.type.canOpenByHand() ? 2 : 1) : null;
    }

    @Override
    public void afterAction(BlockState stateSchematic, BlockHitResult blockHitResult) {
        DoubleBlockHalf doorHinge = stateSchematic.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF);
        BlockState blockState;
        if (doorHinge==DoubleBlockHalf.LOWER){
            blockState = Minecraft.getInstance().level.getBlockState(blockHitResult.getBlockPos().below());
        }else {
            blockState = Minecraft.getInstance().level.getBlockState(blockHitResult.getBlockPos().below(2));
        }

        if (blockState.getBlock() instanceof ICanUse){
            PlayerInputAction.SetShift(false);
        }
    }

    @Override
    public void firstAction(BlockState stateSchematic, BlockHitResult blockHitResult) {
        DoubleBlockHalf doorHinge = stateSchematic.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF);
        BlockState blockState;
        if (doorHinge==DoubleBlockHalf.LOWER){
             blockState = Minecraft.getInstance().level.getBlockState(blockHitResult.getBlockPos().below());
        }else {
             blockState = Minecraft.getInstance().level.getBlockState(blockHitResult.getBlockPos().below(2));
        }

        if (blockState.getBlock() instanceof ICanUse){
            PlayerInputAction.SetShift(true);
        }
    }
}
