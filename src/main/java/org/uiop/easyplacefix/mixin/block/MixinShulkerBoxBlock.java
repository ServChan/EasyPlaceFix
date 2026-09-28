package org.uiop.easyplacefix.mixin.block;

import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.data.RelativeBlockHitResult;

import static org.uiop.easyplacefix.config.easyPlacefixConfig.Allow_Interaction;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

@Mixin(ShulkerBoxBlock.class)
public class MixinShulkerBoxBlock implements IBlock {
    @Override
    public Tuple<RelativeBlockHitResult, Integer> getHitResult(BlockState blockState, BlockPos blockPos, BlockState worldBlockState) {
        Direction facing = blockState.getValue(BlockStateProperties.FACING);
        return new Tuple<>(new RelativeBlockHitResult(
                new Vec3(0.5 + 0.5 * facing.getStepX(), 0.5 + 0.5 * facing.getStepY(), 0.5 + 0.5 * facing.getStepZ()),
                facing,
                blockPos,
                false
        ), 1);
    }

    @Override
    public InteractionResult isWorldTermination(BlockPos pos, BlockState blockState, BlockState worldBlockstate) {
        if (Allow_Interaction.getBooleanValue())return InteractionResult.PASS;

        return null;
    }
}
