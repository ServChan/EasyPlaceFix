package org.uiop.easyplacefix.Mixin.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.MultifaceBlock;
import net.minecraft.block.enums.BlockFace;
import net.minecraft.client.MinecraftClient;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.data.RelativeBlockHitResult;

@Mixin(MultifaceBlock.class)
public abstract class MixinMultifaceBlock implements IBlock {
    @Shadow
    protected abstract boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos);

    private static Vec3d getFaceCenter(Direction direction) {
        return switch (direction) {
            case EAST -> new Vec3d(1, 0.5, 0.5);
            case SOUTH -> new Vec3d(0.5, 0.5, 1);
            case WEST -> new Vec3d(0, 0.5, 0.5);
            case NORTH -> new Vec3d(0.5, 0.5, 0);
            case UP -> new Vec3d(0.5, 1, 0.5);
            case DOWN -> new Vec3d(0.5, 0, 0.5);
        };
    }

    private static Direction findAttachedFace(BlockState blockState) {
        for (Direction direction : Direction.values()) {
            BooleanProperty faceProperty = MultifaceBlock.getProperty(direction);
            if (blockState.contains(faceProperty) && blockState.get(faceProperty)) {
                return direction;
            }
        }
        return null;
    }

    @Override
    public Pair<RelativeBlockHitResult, Integer> getHitResult(BlockState blockState, BlockPos blockPos, BlockState worldBlockState) {
        if (!canPlaceAt(blockState, MinecraftClient.getInstance().world, blockPos)) {
            return null;
        }

        // Wall-mounted style multiface blocks
        if (blockState.contains(Properties.BLOCK_FACE) && blockState.contains(Properties.HORIZONTAL_FACING)) {
        BlockFace blockFace = blockState.get(Properties.BLOCK_FACE);
        Direction direction = blockState.get(Properties.HORIZONTAL_FACING);
            int clicks = blockState.contains(Properties.POWERED) && blockState.get(Properties.POWERED) ? 2 : 1;
            return switch (blockFace) {//TODO TODO replace null with chained placement flow using position-aware easy place
                    case FLOOR -> new Pair<>(
                            new RelativeBlockHitResult(new Vec3d(0.5, 1, 0.5),
                                    Direction.UP,
                                    blockPos.down(), false
                            ), clicks);
                    case CEILING -> new Pair<>(
                            new RelativeBlockHitResult(new Vec3d(0.5, 0, 0.5),
                                    Direction.DOWN,
                                    blockPos.up(), false
                            ), clicks);

                    case WALL -> new Pair<>(
                            new RelativeBlockHitResult(
                                    getFaceCenter(direction),
                                    direction,
                                    blockPos.offset(direction.getOpposite()),
                                    false
                            ), clicks);
                };
        }

        // Generic multiface blocks (for example glow lichen): use one active face.
        Direction attachedFace = findAttachedFace(blockState);
        if (attachedFace == null) {
            return new Pair<>(
                    new RelativeBlockHitResult(new Vec3d(0.5, 0.5, 0.5), Direction.UP, blockPos, false),
                    1
            );
        }

        return new Pair<>(
                new RelativeBlockHitResult(
                        getFaceCenter(attachedFace),
                        attachedFace,
                        blockPos.offset(attachedFace.getOpposite()),
                        false
                ),
                1
        );
    }

}
