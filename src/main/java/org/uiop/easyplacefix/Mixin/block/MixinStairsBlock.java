package org.uiop.easyplacefix.Mixin.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.StairsBlock;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.block.enums.BlockHalf;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.LookAt;
import org.uiop.easyplacefix.data.RelativeBlockHitResult;

import static org.uiop.easyplacefix.until.PlayerBlockAction.useItemOnAction.consumePlacementStateOverrideFor;

@Mixin(StairsBlock.class)
public class MixinStairsBlock implements IBlock {
    @ModifyReturnValue(method = "getPlacementState", at = @At("RETURN"))
    private BlockState easyplacefix$overridePlacementState(BlockState original, ItemPlacementContext context) {
        BlockState override = consumePlacementStateOverrideFor(StairsBlock.class, context.getBlockPos());
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
            case SOUTH -> new Pair<>(LookAt.South, LookAt.Horizontal);
            case WEST -> new Pair<>(LookAt.West, LookAt.Horizontal);
            case EAST -> new Pair<>(LookAt.East, LookAt.Horizontal);
            default -> new Pair<>(LookAt.North, LookAt.Horizontal);
        };
    }

    @Override
    public Pair<RelativeBlockHitResult, Integer> getHitResult(BlockState blockState, BlockPos blockPos, BlockState worldBlockState) {

        BlockHalf blockHalf = blockState.get(Properties.BLOCK_HALF);
        return switch (blockHalf) {
            case TOP -> new Pair<>(
                    new RelativeBlockHitResult(
                            new Vec3d(0.5, 0.0625, 0.5),
                            Direction.DOWN,
                            blockPos,
                            false
                    ), 1);
            case BOTTOM -> new Pair<>(
                    new RelativeBlockHitResult(
                            new Vec3d(0.5, 0.9375, 0.5),
                            Direction.UP,
                            blockPos,
                            false
                    ), 1);
        };

    }
}
