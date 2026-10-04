package org.uiop.easyplacefix.Mixin.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.LeverBlock;
import net.minecraft.block.enums.BlockFace;
import net.minecraft.client.MinecraftClient;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
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
    public Pair<RelativeBlockHitResult, Integer> getHitResult(BlockState blockState, BlockPos blockPos, BlockState worldBlockState) {
        BlockFace blockFace = blockState.get(Properties.BLOCK_FACE);
        Direction direction = blockState.get(Properties.HORIZONTAL_FACING);
        return canPlaceAt(blockState, MinecraftClient.getInstance().world, blockPos) ?
                switch (blockFace) {
                    case FLOOR -> new Pair<>(
                            new RelativeBlockHitResult(new Vec3d(0.5, 1, 0.5),
                                    Direction.UP,
                                    blockPos.down(), false
                            ), blockState.get(Properties.POWERED) ? 2 : 1);
                    case CEILING -> new Pair<>(
                            new RelativeBlockHitResult(new Vec3d(0.5, 0, 0.5),
                                    Direction.DOWN,
                                    blockPos.up(), false
                            ), blockState.get(Properties.POWERED) ? 2 : 1);

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
                            ), blockState.get(Properties.POWERED) ? 2 : 1);
                } : null;
    }

    @Override
    public void afterAction(BlockState stateSchematic, BlockHitResult blockHitResult) {
        if (stateSchematic.get(Properties.BLOCK_FACE) == BlockFace.CEILING) {
            BlockState blockState = MinecraftClient.getInstance().world.getBlockState(blockHitResult.getBlockPos().up());
            if (blockState.getBlock() instanceof ICanUse) {
                PlayerInputAction.SetShift(false);
            }

        } else if (stateSchematic.get(Properties.BLOCK_FACE) == BlockFace.FLOOR) {
            BlockState blockState = MinecraftClient.getInstance().world.getBlockState(blockHitResult.getBlockPos().down());
            if (blockState.getBlock() instanceof ICanUse) {
                PlayerInputAction.SetShift(false);
            }
        } else {
            BlockState blockState = MinecraftClient.getInstance().world.getBlockState(blockHitResult.getBlockPos().offset(stateSchematic.get(Properties.HORIZONTAL_FACING).getOpposite()));
            if (blockState.getBlock() instanceof ICanUse) {
                PlayerInputAction.SetShift(false);
            }
        }

    }

    @Override
    public void firstAction(BlockState stateSchematic, BlockHitResult blockHitResult) {
        if (stateSchematic.get(Properties.BLOCK_FACE) == BlockFace.CEILING) {
            BlockState blockState = MinecraftClient.getInstance().world.getBlockState(blockHitResult.getBlockPos().up());
            if (blockState.getBlock() instanceof ICanUse) {
                PlayerInputAction.SetShift(true);
            }

        } else if (stateSchematic.get(Properties.BLOCK_FACE) == BlockFace.FLOOR) {
            BlockState blockState = MinecraftClient.getInstance().world.getBlockState(blockHitResult.getBlockPos().down());
            if (blockState.getBlock() instanceof ICanUse) {
                PlayerInputAction.SetShift(true);
            }
        } else {
            BlockState blockState = MinecraftClient.getInstance().world.getBlockState(blockHitResult.getBlockPos().offset(stateSchematic.get(Properties.HORIZONTAL_FACING).getOpposite()));
            if (blockState.getBlock() instanceof ICanUse) {
                PlayerInputAction.SetShift(true);
            }
        }

    }

}
