package org.uiop.easyplacefix.mixin.block;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.data.RelativeBlockHitResult;
import org.uiop.easyplacefix.util.SupportInteraction;
import net.minecraft.world.phys.BlockHitResult;

@Mixin(CakeBlock.class)
public abstract class MixinCakeBlock implements IBlock {
    @Shadow
    protected abstract boolean canSurvive(BlockState state, LevelReader world, BlockPos pos);

    @Override
    public Tuple<RelativeBlockHitResult, Integer> getHitResult(BlockState blockState, BlockPos blockPos, BlockState worldBlockState) {
        return canSurvive(blockState, Minecraft.getInstance().level, blockPos) ? new Tuple<>(
                new RelativeBlockHitResult(
                        new Vec3(0.5, 1, 0.5),
                        Direction.UP,
                        blockPos.below(), false
                ), blockState.getValue(BlockStateProperties.BITES) + 1
        ) : null;
    }

    @Override
    public void firstAction(BlockState stateSchematic, BlockHitResult blockHitResult) {
        SupportInteraction.sneakIfInteractive(blockHitResult.getBlockPos().below());
    }

    @Override
    public void afterAction(BlockState stateSchematic, BlockHitResult blockHitResult) {
        SupportInteraction.releaseIfInteractive(blockHitResult.getBlockPos().below());
    }
}
