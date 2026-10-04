package org.uiop.easyplacefix.Mixin.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.WallMountedBlock;
import net.minecraft.block.enums.BlockFace;
import net.minecraft.client.MinecraftClient;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.ICanUse;
import org.uiop.easyplacefix.LookAt;
import org.uiop.easyplacefix.data.RelativeBlockHitResult;
import org.uiop.easyplacefix.until.PlayerBlockAction;
import org.uiop.easyplacefix.until.PlayerInputAction;

@Mixin(WallMountedBlock.class)
public abstract class MixinWallMountedBlock implements IBlock {
    @Shadow
    protected abstract boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos);

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
        BlockFace blockFace = blockState.get(Properties.BLOCK_FACE);
        Direction direction = blockState.get(Properties.HORIZONTAL_FACING);
        return canPlaceAt(blockState, MinecraftClient.getInstance().world, blockPos) ?
                switch (blockFace) {
                    case FLOOR -> new Pair<>(
                            new RelativeBlockHitResult(new Vec3d(0.5, 1, 0.5),
                                    Direction.UP,
                                    blockPos.down(), false
                            ), 1);
                    case CEILING -> new Pair<>(
                            new RelativeBlockHitResult(new Vec3d(0.5, 0, 0.5),
                                    Direction.DOWN,
                                    blockPos.up(), false
                            ), 1);

                    case WALL -> new Pair<>(
                            new RelativeBlockHitResult(
                                    switch (direction) {
                                        case EAST -> new Vec3d(1, 0.5, 0.5);
                                        case SOUTH -> new Vec3d(0.5, 0.5, 1);
                                        case WEST -> new Vec3d(0, 0.5, 0.5);
                                        default -> new Vec3d(0.5, 0.5, 0);
                                    },
                                    direction,
                                    blockPos.offset(direction.getOpposite()),
                                    false
                            ), 1);
                } : null;
    }
}
