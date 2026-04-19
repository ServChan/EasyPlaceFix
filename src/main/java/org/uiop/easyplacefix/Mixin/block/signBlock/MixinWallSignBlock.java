package org.uiop.easyplacefix.Mixin.block.signBlock;

import fi.dy.masa.litematica.world.SchematicWorldHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.ICanUse;
import org.uiop.easyplacefix.data.RelativeBlockHitResult;
import org.uiop.easyplacefix.until.PlayerBlockAction;
import org.uiop.easyplacefix.until.PlayerInputAction;


@Mixin(WallSignBlock.class)
public abstract class MixinWallSignBlock implements IBlock {

    @Shadow
    protected abstract boolean canSurvive(BlockState state, LevelReader world, BlockPos pos);
    @Override
    public void afterAction(BlockState stateSchematic, BlockHitResult blockHitResult) {
        BlockState blockState = Minecraft.getInstance().level.getBlockState(blockHitResult.getBlockPos().relative(stateSchematic.getValue(BlockStateProperties.HORIZONTAL_FACING).getOpposite()));
        if (blockState.getBlock() instanceof ICanUse){
            PlayerInputAction.SetShift(false);
        }
    }

    @Override
    public void firstAction(BlockState stateSchematic, BlockHitResult blockHitResult) {
        PlayerBlockAction.openSignEditorAction.count++;
        BlockState blockState = Minecraft.getInstance().level.getBlockState(blockHitResult.getBlockPos().relative(stateSchematic.getValue(BlockStateProperties.HORIZONTAL_FACING).getOpposite()));
        if (blockState.getBlock() instanceof ICanUse){
            PlayerInputAction.SetShift(true);
        }
    }
    @Override
    public Tuple<RelativeBlockHitResult, Integer> getHitResult(BlockState blockState, BlockPos blockPos, BlockState worldBlockState) {
        Direction direction = blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);
        return this.canSurvive(blockState, Minecraft.getInstance().level, blockPos) ?
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
}
