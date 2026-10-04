package org.uiop.easyplacefix.Mixin.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.GrindstoneBlock;
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
import org.uiop.easyplacefix.until.PlayerInputAction;

@Mixin(GrindstoneBlock.class)
public abstract class MixinGrindstoneBlock implements IBlock {
    @Override
    public boolean HasSleepTime(BlockState blockState) {
        return blockState.get(Properties.BLOCK_FACE) == BlockFace.WALL;

    }

    @Override
    public Pair<LookAt, LookAt> getYawAndPitch(BlockState blockState) {
        if (blockState.get(Properties.BLOCK_FACE) != BlockFace.WALL)
            return switch (blockState.get(Properties.HORIZONTAL_FACING)) {
                case SOUTH -> new Pair<>(LookAt.South, LookAt.Horizontal);
                case WEST -> new Pair<>(LookAt.West, LookAt.Horizontal);
                case EAST -> new Pair<>(LookAt.East, LookAt.Horizontal);
                default -> new Pair<>(LookAt.North, LookAt.Horizontal);
            };
        return switch (blockState.get(Properties.HORIZONTAL_FACING)) {
            case SOUTH -> new Pair<>(LookAt.North, LookAt.Horizontal);
            case WEST -> new Pair<>(LookAt.East, LookAt.Horizontal);
            case EAST -> new Pair<>(LookAt.West, LookAt.Horizontal);
            default -> new Pair<>(LookAt.South, LookAt.Horizontal);
        };
    }
    @Shadow
    protected abstract boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos);
    @Override
    public Pair<RelativeBlockHitResult, Integer> getHitResult(BlockState blockState, BlockPos blockPos, BlockState worldBlockState) {
        BlockFace blockFace = blockState.get(Properties.BLOCK_FACE);
        Direction direction = blockState.get(Properties.HORIZONTAL_FACING);
        return
                switch (blockFace) {
                    case FLOOR ->
                            canPlaceAt(blockState,MinecraftClient.getInstance().world, blockPos) ? new Pair<>(
                                    new RelativeBlockHitResult(new Vec3d(0.5, 1, 0.5),
                                            Direction.UP,
                                            blockPos.down(), false
                                    ), 1) : null;
                    case CEILING ->
                            canPlaceAt(blockState,MinecraftClient.getInstance().world, blockPos) ? new Pair<>(
                                    new RelativeBlockHitResult(new Vec3d(0.5, 0, 0.5),
                                            Direction.DOWN,
                                            blockPos.up(), false
                                    ), 1) : null;

                    case WALL -> new Pair<>(new RelativeBlockHitResult(
                            new Vec3d(0.5, 0.5, 0.5),
                            direction,
                            blockPos,
                            false), 1);
                };

    }

    @Override
    public void afterAction(BlockState stateSchematic, BlockHitResult blockHitResult) {
        if (stateSchematic.get(Properties.BLOCK_FACE) == BlockFace.CEILING){
            BlockState blockState = MinecraftClient.getInstance().world.getBlockState(blockHitResult.getBlockPos().up());
            if (blockState.getBlock() instanceof ICanUse){
                PlayerInputAction.SetShift(false);
            }

        }else if (stateSchematic.get(Properties.BLOCK_FACE) == BlockFace.FLOOR){
            BlockState blockState = MinecraftClient.getInstance().world.getBlockState(blockHitResult.getBlockPos().down());
            if (blockState.getBlock() instanceof ICanUse){
                PlayerInputAction.SetShift(false);
            }
        }
    }

    @Override
    public void firstAction(BlockState stateSchematic, BlockHitResult blockHitResult) {
        if (stateSchematic.get(Properties.BLOCK_FACE) == BlockFace.CEILING){
            BlockState blockState = MinecraftClient.getInstance().world.getBlockState(blockHitResult.getBlockPos().up());
            if (blockState.getBlock() instanceof ICanUse){
                PlayerInputAction.SetShift(true);
            }

        }else if (stateSchematic.get(Properties.BLOCK_FACE) == BlockFace.FLOOR){
            BlockState blockState = MinecraftClient.getInstance().world.getBlockState(blockHitResult.getBlockPos().down());
            if (blockState.getBlock() instanceof ICanUse){
                PlayerInputAction.SetShift(true);
            }
        }
    }
}
