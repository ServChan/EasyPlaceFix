package org.uiop.easyplacefix.Mixin.block;

import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.ICanUse;
import org.uiop.easyplacefix.LookAt;
import org.uiop.easyplacefix.data.RelativeBlockHitResult;
import org.uiop.easyplacefix.until.PlayerInputAction;

import static net.minecraft.world.level.block.WallTorchBlock.canSurvive;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.block.WallSkullBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

@Mixin(WallSkullBlock.class)
public class MixinWallSkullBlock implements IBlock {
    @Override
    public Tuple<LookAt, LookAt> getYawAndPitch(BlockState blockState) {
        return switch (blockState.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
            case SOUTH -> new Tuple<>(LookAt.North, LookAt.Horizontal);
            case WEST -> new Tuple<>(LookAt.East, LookAt.Horizontal);
            case EAST -> new Tuple<>(LookAt.West, LookAt.Horizontal);
            default -> new Tuple<>(LookAt.South, LookAt.Horizontal);
        };
    }

    @Override//TODO TODO verify wall skull placement without adjacent support
    public Tuple<RelativeBlockHitResult, Integer> getHitResult(BlockState blockState, BlockPos blockPos, BlockState worldBlockState) {
        Direction direction = blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);
        return
                canSurvive(Minecraft.getInstance().level, blockPos, direction) ?
                        new Tuple<>(
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
                                ), 1
                        ) : null;
    }
    @Override
    public void afterAction(BlockState stateSchematic, BlockHitResult blockHitResult) {

        BlockState blockState = Minecraft.getInstance().level.getBlockState(blockHitResult.getBlockPos().relative(stateSchematic.getValue(BlockStateProperties.HORIZONTAL_FACING).getOpposite()));
        if (blockState.getBlock() instanceof ICanUse) {
            PlayerInputAction.SetShift(false);
        }


    }

    @Override
    public void firstAction(BlockState stateSchematic, BlockHitResult blockHitResult) {

        BlockState blockState = Minecraft.getInstance().level.getBlockState(blockHitResult.getBlockPos().relative(stateSchematic.getValue(BlockStateProperties.HORIZONTAL_FACING).getOpposite()));
        if (blockState.getBlock() instanceof ICanUse) {
            PlayerInputAction.SetShift(true);
        }


    }
}
