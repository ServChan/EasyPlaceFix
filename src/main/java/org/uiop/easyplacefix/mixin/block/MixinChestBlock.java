package org.uiop.easyplacefix.mixin.block;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.data.RelativeBlockHitResult;
import org.uiop.easyplacefix.util.PlayerInputAction;

@Mixin(ChestBlock.class)
public class MixinChestBlock implements IBlock {
    @Shadow
    @Final
    public static EnumProperty<ChestType> TYPE;

    @Override
    public void firstAction(BlockState stateSchematic, BlockHitResult blockHitResult) {
        PlayerInputAction.SetShift(true);
    }

    @Override
    public void afterAction(BlockState stateSchematic, BlockHitResult blockHitResult) {

        PlayerInputAction.SetShift(false);
    }

    @Override
    public Tuple<RelativeBlockHitResult, Integer> getHitResult(BlockState blockState, BlockPos blockPos, BlockState worldBlockState) {

        ChestType chestType = blockState.getValue(BlockStateProperties.CHEST_TYPE);
        if (chestType == ChestType.SINGLE) {
            return new Tuple<>(new RelativeBlockHitResult(
                    new Vec3(0.5, 0.5, 0.5),
                    Direction.UP,
                    blockPos, false
            ), 1);
        }
        Direction blockFace = blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);

        if (chestType == ChestType.LEFT) {
            blockFace = blockFace.getCounterClockWise();
        } else {
            blockFace = blockFace.getClockWise();
        }
        BlockPos offset = blockPos.relative(blockFace.getOpposite());

        return new Tuple<>(new RelativeBlockHitResult(
                switch (blockFace) {
                    case EAST -> new Vec3(0.9, 0.5, 0.5);
                    case SOUTH -> new Vec3(0.5, 0.5, 0.9);
                    case WEST -> new Vec3(0.1, 0.5, 0.5);
                    default -> new Vec3(0.5, 0.5, 0.1);
                },
                blockFace,
                Minecraft.getInstance().level.getBlockState(offset).canBeReplaced() ? blockPos : offset
                , false
        ), 1);

    }
}
