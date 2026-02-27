package org.uiop.easyplacefix.Mixin.block;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.block.BlockState;
import net.minecraft.block.LecternBlock;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.LookAt;
import org.uiop.easyplacefix.until.PlayerInputAction;

import static org.uiop.easyplacefix.until.PlayerBlockAction.useItemOnAction.consumePlacementStateOverrideFor;

@Mixin(LecternBlock.class)
public class MixinLecternBlock implements IBlock {
    @ModifyReturnValue(method = "getPlacementState", at = @At("RETURN"))
    private BlockState easyplacefix$overridePlacementState(BlockState original, ItemPlacementContext context) {
        BlockState override = consumePlacementStateOverrideFor(LecternBlock.class, context.getBlockPos());
        if (override == null || original == null) {
            return original;
        }

        if (original.contains(Properties.HORIZONTAL_FACING) && override.contains(Properties.HORIZONTAL_FACING)) {
            return original.with(Properties.HORIZONTAL_FACING, override.get(Properties.HORIZONTAL_FACING));
        }
        return original;
    }

    @Override
    public Pair<LookAt, LookAt> getYawAndPitch(BlockState blockState) {
        return switch (blockState.get(Properties.HORIZONTAL_FACING)) {
            case SOUTH -> new Pair<>(LookAt.North, LookAt.Horizontal);
            case WEST -> new Pair<>(LookAt.East, LookAt.Horizontal);
            case EAST -> new Pair<>(LookAt.West, LookAt.Horizontal);
            default -> new Pair<>(LookAt.South, LookAt.Horizontal);
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

