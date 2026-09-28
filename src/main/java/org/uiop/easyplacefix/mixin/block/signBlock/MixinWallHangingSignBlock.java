package org.uiop.easyplacefix.mixin.block.signBlock;

import fi.dy.masa.litematica.world.SchematicWorldHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.ICanUse;
import org.uiop.easyplacefix.LookAt;
import org.uiop.easyplacefix.data.RelativeBlockHitResult;
import org.uiop.easyplacefix.util.PlayerBlockAction;
import org.uiop.easyplacefix.util.PlayerInputAction;

@Mixin(WallHangingSignBlock.class)
public abstract class MixinWallHangingSignBlock implements IBlock {
    @Shadow
    public abstract boolean canPlace(BlockState state, LevelReader world, BlockPos pos);

    @Override
    public boolean HasSleepTime(BlockState blockState) {
        return true;
    }

    @Override
    public void firstAction(BlockState stateSchematic, BlockHitResult blockHitResult) {
        PlayerBlockAction.openSignEditorAction.arm();
    }

    @Override
    public Tuple<LookAt, LookAt> getYawAndPitch(BlockState blockState) {
        return switch (blockState.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
            case SOUTH -> new Tuple<>(LookAt.North, LookAt.Horizontal);
            case WEST -> new Tuple<>(LookAt.East, LookAt.Horizontal);
            case EAST -> new Tuple<>(LookAt.West, LookAt.Horizontal);
            default -> new Tuple<>(LookAt.South, LookAt.Horizontal);
        };
    }

    @Override
    public Tuple<RelativeBlockHitResult, Integer> getHitResult(BlockState blockState, BlockPos blockPos, BlockState worldBlockState) {
        Direction facing = blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);
        if (!canPlace(blockState, Minecraft.getInstance().level, blockPos)) {
            return null;
        }
        for (Direction side : new Direction[]{facing.getClockWise(), facing.getCounterClockWise()}) {
            BlockPos supportPos = blockPos.relative(side);
            if (Minecraft.getInstance().level.getBlockState(supportPos).canBeReplaced()) {
                continue;
            }
            Direction clickedFace = side.getOpposite();
            return new Tuple<>(
                    new RelativeBlockHitResult(
                            new Vec3(0.5 + 0.5 * clickedFace.getStepX(), 0.5, 0.5 + 0.5 * clickedFace.getStepZ()),
                            clickedFace,
                            supportPos,
                            false
                    ), 1);
        }
        return null;
    }
}
