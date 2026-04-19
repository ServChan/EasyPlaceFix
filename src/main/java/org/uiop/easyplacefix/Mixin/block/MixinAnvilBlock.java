package org.uiop.easyplacefix.Mixin.block;

import net.minecraft.util.Tuple;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.LookAt;

@Mixin(AnvilBlock.class)
public class MixinAnvilBlock implements IBlock {
    @Override
    public Tuple<LookAt, LookAt> getYawAndPitch(BlockState blockState) {

        return switch (blockState.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
            case SOUTH -> new Tuple<>(LookAt.East, LookAt.PlayerPitch);
            case WEST -> new Tuple<>(LookAt.South, LookAt.PlayerPitch);
            case EAST -> new Tuple<>(LookAt.North, LookAt.PlayerPitch);
            default -> new Tuple<>(LookAt.West, LookAt.PlayerPitch);
        };
        //Anvil orientation is perpendicular to piston-like facing
        // IDE settings note
    }
}
