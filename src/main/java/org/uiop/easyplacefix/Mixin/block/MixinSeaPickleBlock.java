package org.uiop.easyplacefix.Mixin.block;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.data.RelativeBlockHitResult;

@Mixin(SeaPickleBlock.class)
public abstract class MixinSeaPickleBlock extends VegetationBlock implements IBlock {
    protected MixinSeaPickleBlock(Properties settings) {
        super(settings);
    }

    @Override
    public Tuple<RelativeBlockHitResult, Integer> getHitResult(BlockState blockState, BlockPos blockPos, BlockState worldBlockState) {
        int count;
        if (worldBlockState.getBlock()==blockState.getBlock()){//TODO!!Legacy behavior note
            count = blockState.getValue(BlockStateProperties.PICKLES)-worldBlockState.getValue(BlockStateProperties.PICKLES);
            if (count<1)return null;
        }else {
            count = blockState.getValue(BlockStateProperties.PICKLES);
        }


        return canSurvive(blockState, Minecraft.getInstance().level, blockPos) ? new Tuple<>(
                new RelativeBlockHitResult(
                        new Vec3(0.5, 0.5, 0.5),
                        Direction.UP,
                        blockPos, false
                ), count
        ) : null;
    }
}
