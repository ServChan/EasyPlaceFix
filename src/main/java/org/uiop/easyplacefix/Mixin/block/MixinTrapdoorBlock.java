package org.uiop.easyplacefix.Mixin.block;

import net.minecraft.block.BlockSetType;
import net.minecraft.block.BlockState;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.block.enums.BlockHalf;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.LookAt;
import org.uiop.easyplacefix.data.RelativeBlockHitResult;

import static org.uiop.easyplacefix.until.PlayerBlockAction.useItemOnAction.consumePlacementStateOverrideFor;

@Mixin(TrapdoorBlock.class)
public class MixinTrapdoorBlock implements IBlock {
    @Shadow
    @Final
    private BlockSetType blockSetType;

    @ModifyReturnValue(method = "getPlacementState", at = @At("RETURN"))
    private BlockState easyplacefix$overridePlacementState(BlockState original, ItemPlacementContext context) {
        BlockState override = consumePlacementStateOverrideFor(TrapdoorBlock.class, context.getBlockPos());
        if (override == null || original == null) {
            return original;
        }

        BlockState result = original;
        if (result.contains(Properties.HORIZONTAL_FACING) && override.contains(Properties.HORIZONTAL_FACING)) {
            result = result.with(Properties.HORIZONTAL_FACING, override.get(Properties.HORIZONTAL_FACING));
        }
        if (result.contains(Properties.BLOCK_HALF) && override.contains(Properties.BLOCK_HALF)) {
            result = result.with(Properties.BLOCK_HALF, override.get(Properties.BLOCK_HALF));
        }
        return result;
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
    public Pair<RelativeBlockHitResult, Integer> getHitResult(BlockState blockState, BlockPos blockPos, BlockState worldBlockState) {
        return new Pair<>(
                switch (blockState.get(Properties.BLOCK_HALF)) {
                    case TOP -> new RelativeBlockHitResult(new Vec3d(0.5, 0.9375, 0.5), Direction.DOWN, blockPos, false);
                    case BOTTOM -> new RelativeBlockHitResult(new Vec3d(0.5, 0.0625, 0.5), Direction.UP, blockPos, false);
                },
                blockState.get(Properties.OPEN) && this.blockSetType.canOpenByHand() ? 2 : 1
        );
    }
}
