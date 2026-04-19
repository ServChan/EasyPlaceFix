package org.uiop.easyplacefix.Mixin.block;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.LookAt;
import org.uiop.easyplacefix.data.RelativeBlockHitResult;

import static org.uiop.easyplacefix.until.PlayerBlockAction.useItemOnAction.consumePlacementStateOverrideFor;

@Mixin(StairBlock.class)
public class MixinStairsBlock implements IBlock {
    @ModifyReturnValue(method = "getStateForPlacement", at = @At("RETURN"))
    private BlockState easyplacefix$overridePlacementState(BlockState original, BlockPlaceContext context) {
        BlockState override = consumePlacementStateOverrideFor(StairBlock.class, context.getClickedPos());
        if (override == null || original == null) {
            return original;
        }

        BlockState result = original;
        if (result.hasProperty(BlockStateProperties.HORIZONTAL_FACING) && override.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            result = result.setValue(BlockStateProperties.HORIZONTAL_FACING, override.getValue(BlockStateProperties.HORIZONTAL_FACING));
        }
        if (result.hasProperty(BlockStateProperties.HALF) && override.hasProperty(BlockStateProperties.HALF)) {
            result = result.setValue(BlockStateProperties.HALF, override.getValue(BlockStateProperties.HALF));
        }
        return result;
    }

    @Override
    public Tuple<LookAt, LookAt> getYawAndPitch(BlockState blockState) {
        return switch (blockState.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
            case SOUTH -> new Tuple<>(LookAt.South, LookAt.Horizontal);
            case WEST -> new Tuple<>(LookAt.West, LookAt.Horizontal);
            case EAST -> new Tuple<>(LookAt.East, LookAt.Horizontal);
            default -> new Tuple<>(LookAt.North, LookAt.Horizontal);
        };
    }

    @Override
    public Tuple<RelativeBlockHitResult, Integer> getHitResult(BlockState blockState, BlockPos blockPos, BlockState worldBlockState) {

        Half blockHalf = blockState.getValue(BlockStateProperties.HALF);
        return switch (blockHalf) {
            case TOP -> new Tuple<>(
                    new RelativeBlockHitResult(
                            new Vec3(0.5, 0.0625, 0.5),
                            Direction.DOWN,
                            blockPos,
                            false
                    ), 1);
            case BOTTOM -> new Tuple<>(
                    new RelativeBlockHitResult(
                            new Vec3(0.5, 0.9375, 0.5),
                            Direction.UP,
                            blockPos,
                            false
                    ), 1);
        };

    }
}
