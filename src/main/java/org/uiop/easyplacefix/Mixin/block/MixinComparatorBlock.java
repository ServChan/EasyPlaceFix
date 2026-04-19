package org.uiop.easyplacefix.Mixin.block;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ComparatorMode;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.data.RelativeBlockHitResult;


@Mixin(ComparatorBlock.class)
public abstract class MixinComparatorBlock extends DiodeBlock implements IBlock {

    protected MixinComparatorBlock(Properties settings) {
        super(settings);
    }

    @Override
    public Tuple<RelativeBlockHitResult, Integer> getHitResult(BlockState blockState, BlockPos blockPos, BlockState worldBlockState) {
        return this.canSurvive(blockState, Minecraft.getInstance().level, blockPos) ?
                new Tuple<>(new RelativeBlockHitResult(new Vec3(0.5, 0.5, 0.5),
                        Direction.UP,
                        blockPos,
                        false),
                        (blockState.getValue(BlockStateProperties.MODE_COMPARATOR) == ComparatorMode.SUBTRACT) ? 2 : 1) : null;
    }
}
