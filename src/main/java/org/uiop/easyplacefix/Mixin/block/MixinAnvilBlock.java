package org.uiop.easyplacefix.Mixin.block;

import net.minecraft.block.AnvilBlock;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.LookAt;

@Mixin(AnvilBlock.class)
public class MixinAnvilBlock implements IBlock {
    @Override
    public Pair<LookAt, LookAt> getYawAndPitch(BlockState blockState) {

        return switch (blockState.get(Properties.HORIZONTAL_FACING)) {
            case SOUTH -> new Pair<>(LookAt.East, LookAt.PlayerPitch);
            case WEST -> new Pair<>(LookAt.South, LookAt.PlayerPitch);
            case EAST -> new Pair<>(LookAt.North, LookAt.PlayerPitch);
            default -> new Pair<>(LookAt.West, LookAt.PlayerPitch);
        };

    }
}
