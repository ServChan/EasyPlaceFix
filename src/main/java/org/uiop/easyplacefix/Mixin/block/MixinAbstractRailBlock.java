package org.uiop.easyplacefix.Mixin.block;

import net.minecraft.util.Tuple;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.RailShape;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.LookAt;

@Mixin(BaseRailBlock.class)
public class MixinAbstractRailBlock implements IBlock {
    @Override
    public Tuple<LookAt, LookAt> getYawAndPitch(BlockState blockState) {
        RailShape railShape = blockState.getValue(BlockStateProperties.RAIL_SHAPE_STRAIGHT);
        if (railShape == RailShape.NORTH_SOUTH) {
            return new Tuple<>(LookAt.North, LookAt.PlayerPitch);
        } else {
            return new Tuple<>(LookAt.East, LookAt.PlayerPitch);
        }
    }
}
