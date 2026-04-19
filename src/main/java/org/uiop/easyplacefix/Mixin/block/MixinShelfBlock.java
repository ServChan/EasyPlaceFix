package org.uiop.easyplacefix.Mixin.block;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.ShelfBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.LookAt;
import org.uiop.easyplacefix.until.PlayerInputAction;

import static org.uiop.easyplacefix.until.PlayerBlockAction.useItemOnAction.consumePlacementStateOverrideFor;

@Mixin(ShelfBlock.class)
public class MixinShelfBlock implements IBlock {
    @ModifyReturnValue(method = "getStateForPlacement", at = @At("RETURN"))
    private BlockState easyplacefix$overridePlacementState(BlockState original, BlockPlaceContext context) {
        BlockState override = consumePlacementStateOverrideFor(ShelfBlock.class, context.getClickedPos());
        if (override == null || original == null) {
            return original;
        }

        if (original.hasProperty(BlockStateProperties.HORIZONTAL_FACING) && override.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            return original.setValue(BlockStateProperties.HORIZONTAL_FACING, override.getValue(BlockStateProperties.HORIZONTAL_FACING));
        }
        return original;
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

    @Override
    public void firstAction(BlockState stateSchematic, BlockHitResult blockHitResult) {
        PlayerInputAction.SetShift(true);
    }

    @Override
    public void afterAction(BlockState stateSchematic, BlockHitResult blockHitResult) {
        PlayerInputAction.SetShift(false);
    }
}
