package org.uiop.easyplacefix.Mixin.block;

import net.minecraft.block.AbstractRedstoneGateBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.ComparatorBlock;
import net.minecraft.block.enums.ComparatorMode;
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

@Mixin(ComparatorBlock.class)
public abstract class MixinComparatorBlock extends AbstractRedstoneGateBlock implements IBlock {

    protected MixinComparatorBlock(Settings settings) {
        super(settings);
    }

    @Override
    public Pair<RelativeBlockHitResult, Integer> getHitResult(BlockState blockState, BlockPos blockPos, BlockState worldBlockState) {
        return this.canPlaceAt(blockState, MinecraftClient.getInstance().world, blockPos) ?
                new Pair<>(new RelativeBlockHitResult(new Vec3d(0.5, 0.5, 0.5),
                        Direction.UP,
                        blockPos,
                        false),
                        (blockState.get(Properties.COMPARATOR_MODE) == ComparatorMode.SUBTRACT) ? 2 : 1) : null;
    }
}
