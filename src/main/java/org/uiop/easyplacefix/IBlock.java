package org.uiop.easyplacefix;

import org.uiop.easyplacefix.data.RelativeBlockHitResult;

import static org.uiop.easyplacefix.until.PlayerRotationAction.limitYawRotation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public interface IBlock {
    default boolean hasYawPitch() {
        return false;
    }
    default boolean HasSleepTime(BlockState blockState){return false;}
    default Tuple<LookAt, LookAt> getYawAndPitch(BlockState blockState) {
        return null;
    }
    default Tuple<Float, Float> getLimitYawAndPitch(BlockState blockState) {
        Tuple<LookAt, LookAt> lookAtPair = getYawAndPitch(blockState);
        if (lookAtPair!=null){
            return new Tuple<>(limitYawRotation(Direction.fromYRot(lookAtPair.getA().Value())),lookAtPair.getB().Value());
        }
       return null;
    }

    default Direction getSide(BlockState blockState) {
        return null;
    }

    default Tuple<RelativeBlockHitResult, Integer> getHitResult(BlockState blockState, BlockPos blockPos, BlockState worldBlockState) {
        return new Tuple<>(new RelativeBlockHitResult(
                new Vec3(0.5, 0.5, 0.5),
                Direction.UP,
                blockPos, false
        ), 1);
    }

    default void BlockAction(BlockState blockState, BlockHitResult blockHitResult) {
    }
    default void firstAction(BlockState stateSchematic, BlockHitResult blockHitResult){}
    default void afterAction(BlockState stateSchematic, BlockHitResult blockHitResult){}
    default InteractionResult isSchemaTermination(BlockPos pos, BlockState blockState, BlockState worldBlockstate){return null;}
    default InteractionResult isWorldTermination(BlockPos pos, BlockState blockState,BlockState worldBlockstate){return null;}
    default Item getItemForBlockState(BlockState blockState){return null;}
}
