package org.uiop.easyplacefix.Mixin.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.data.RelativeBlockHitResult;

@Mixin(SlabBlock.class)
public class MixinSlabBlock implements IBlock {
    @Override
    public Tuple<RelativeBlockHitResult, Integer> getHitResult(BlockState blockState, BlockPos blockPos, BlockState worldBlockState) {
        SlabType slabType = blockState.getValue(BlockStateProperties.SLAB_TYPE);

        if (blockState.getBlock().equals(worldBlockState.getBlock())){
            SlabType slabClientType = worldBlockState.getValue(BlockStateProperties.SLAB_TYPE);
            if (slabType==SlabType.DOUBLE){
                if (slabClientType==SlabType.TOP)
                    return new Tuple<>(new RelativeBlockHitResult(new Vec3(0.5, 0, 0.5), Direction.UP, blockPos, false), 1);
                else
                    return new Tuple<>(new RelativeBlockHitResult(new Vec3(0.5, 1, 0.5), Direction.DOWN, blockPos, false), 1);

            }
            else {
                return null;
            }

        }
        return switch (slabType) {
            case TOP -> new Tuple<>(new RelativeBlockHitResult(new Vec3(0.5, 1, 0.5), Direction.DOWN, blockPos, false), 1);
            case BOTTOM -> new Tuple<>(new RelativeBlockHitResult(new Vec3(0.5, 0, 0.5), Direction.UP, blockPos, false), 1);
            case DOUBLE -> new Tuple<>(new RelativeBlockHitResult(new Vec3(0.5, 0.5, 0.5), Direction.UP, blockPos, false), 2);
        };

    }
}
