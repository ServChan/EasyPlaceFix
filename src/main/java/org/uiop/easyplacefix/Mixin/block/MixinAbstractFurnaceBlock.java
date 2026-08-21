package org.uiop.easyplacefix.mixin.block;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.LookAt;

import static org.uiop.easyplacefix.config.easyPlacefixConfig.Allow_Interaction;

@Mixin(AbstractFurnaceBlock.class)
public class MixinAbstractFurnaceBlock implements IBlock {
    @Override
    public InteractionResult isWorldTermination(BlockPos pos, BlockState blockState, BlockState worldBlockstate) {
        if (Allow_Interaction.getBooleanValue()) return InteractionResult.PASS;

        return null;
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
}
