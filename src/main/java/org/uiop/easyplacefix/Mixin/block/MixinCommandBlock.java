package org.uiop.easyplacefix.Mixin.block;

import net.minecraft.util.Tuple;
import net.minecraft.world.level.block.CommandBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.LookAt;

@Mixin(CommandBlock.class)
public class MixinCommandBlock implements IBlock {
    @Override
    public Tuple<LookAt, LookAt> getYawAndPitch(BlockState blockState) {
        return switch (blockState.getValue(BlockStateProperties.FACING)) {
            case DOWN -> new Tuple<>(LookAt.PlayerYaw, LookAt.Up);
            case UP -> new Tuple<>(LookAt.PlayerYaw, LookAt.Down);
            case SOUTH -> new Tuple<>(LookAt.North, LookAt.Horizontal);
            case WEST -> new Tuple<>(LookAt.East, LookAt.Horizontal);
            case EAST -> new Tuple<>(LookAt.West, LookAt.Horizontal);
            case NORTH -> new Tuple<>(LookAt.South, LookAt.Horizontal);
        };
    }
}
