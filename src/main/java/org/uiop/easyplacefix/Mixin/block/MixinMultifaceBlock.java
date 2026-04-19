package org.uiop.easyplacefix.Mixin.block;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.data.RelativeBlockHitResult;

@Mixin(MultifaceBlock.class)
public abstract class MixinMultifaceBlock implements IBlock {
    @Shadow
    protected abstract boolean canSurvive(BlockState state, LevelReader world, BlockPos pos);

    private static Vec3 getFaceCenter(Direction direction) {
        return switch (direction) {
            case EAST -> new Vec3(1, 0.5, 0.5);
            case SOUTH -> new Vec3(0.5, 0.5, 1);
            case WEST -> new Vec3(0, 0.5, 0.5);
            case NORTH -> new Vec3(0.5, 0.5, 0);
            case UP -> new Vec3(0.5, 1, 0.5);
            case DOWN -> new Vec3(0.5, 0, 0.5);
        };
    }

    private static Direction findAttachedFace(BlockState blockState) {
        for (Direction direction : Direction.values()) {
            BooleanProperty faceProperty = MultifaceBlock.getFaceProperty(direction);
            if (blockState.hasProperty(faceProperty) && blockState.getValue(faceProperty)) {
                return direction;
            }
        }
        return null;
    }

    @Override
    public Tuple<RelativeBlockHitResult, Integer> getHitResult(BlockState blockState, BlockPos blockPos, BlockState worldBlockState) {
        if (!canSurvive(blockState, Minecraft.getInstance().level, blockPos)) {
            return null;
        }

        // Wall-mounted style multiface blocks
        if (blockState.hasProperty(BlockStateProperties.ATTACH_FACE) && blockState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
        AttachFace blockFace = blockState.getValue(BlockStateProperties.ATTACH_FACE);
        Direction direction = blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);
            int clicks = blockState.hasProperty(BlockStateProperties.POWERED) && blockState.getValue(BlockStateProperties.POWERED) ? 2 : 1;
            return switch (blockFace) {//TODO TODO replace null with chained placement flow using position-aware easy place
                    case FLOOR -> new Tuple<>(
                            new RelativeBlockHitResult(new Vec3(0.5, 1, 0.5),
                                    Direction.UP,
                                    blockPos.below(), false
                            ), clicks);
                    case CEILING -> new Tuple<>(
                            new RelativeBlockHitResult(new Vec3(0.5, 0, 0.5),
                                    Direction.DOWN,
                                    blockPos.above(), false
                            ), clicks);

                    case WALL -> new Tuple<>(
                            new RelativeBlockHitResult(
                                    getFaceCenter(direction),
                                    direction,
                                    blockPos.relative(direction.getOpposite()),
                                    false
                            ), clicks);
                };
        }

        // Generic multiface blocks (for example glow lichen): use one active face.
        Direction attachedFace = findAttachedFace(blockState);
        if (attachedFace == null) {
            return new Tuple<>(
                    new RelativeBlockHitResult(new Vec3(0.5, 0.5, 0.5), Direction.UP, blockPos, false),
                    1
            );
        }

        return new Tuple<>(
                new RelativeBlockHitResult(
                        getFaceCenter(attachedFace),
                        attachedFace,
                        blockPos.relative(attachedFace.getOpposite()),
                        false
                ),
                1
        );
    }

}
