package org.uiop.easyplacefix.Mixin.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.PlantBlock;
import net.minecraft.block.SeaPickleBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.data.RelativeBlockHitResult;

@Mixin(SeaPickleBlock.class)
public abstract class MixinSeaPickleBlock extends PlantBlock implements IBlock {
    protected MixinSeaPickleBlock(Settings settings) {
        super(settings);
    }

    @Override
    public Pair<RelativeBlockHitResult, Integer> getHitResult(BlockState blockState, BlockPos blockPos, BlockState worldBlockState) {
        int count;
        if (worldBlockState.getBlock()==blockState.getBlock()){
            count = blockState.get(Properties.PICKLES)-worldBlockState.get(Properties.PICKLES);
            if (count<1)return null;
        }else {
            count = blockState.get(Properties.PICKLES);
        }

        return canPlaceAt(blockState, MinecraftClient.getInstance().world, blockPos) ? new Pair<>(
                new RelativeBlockHitResult(
                        new Vec3d(0.5, 0.5, 0.5),
                        Direction.UP,
                        blockPos, false
                ), count
        ) : null;
    }
}
