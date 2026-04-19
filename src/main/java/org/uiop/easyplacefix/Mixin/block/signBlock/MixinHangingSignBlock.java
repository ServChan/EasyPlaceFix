package org.uiop.easyplacefix.Mixin.block.signBlock;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.LookAt;
import org.uiop.easyplacefix.data.RelativeBlockHitResult;
import org.uiop.easyplacefix.until.PlayerBlockAction;
import org.uiop.easyplacefix.until.PlayerInputAction;

@Mixin(CeilingHangingSignBlock.class)
public abstract class MixinHangingSignBlock implements IBlock {


    @Shadow
    protected abstract boolean canSurvive(BlockState state, LevelReader world, BlockPos pos);
    @Override
    public Tuple<Float, Float> getLimitYawAndPitch(BlockState blockState) {
        Tuple<LookAt, LookAt> lookAtPair = getYawAndPitch(blockState);
        return new Tuple<>(
                lookAtPair.getA().Value(),
                lookAtPair.getB().Value()
        );
    }
    @Override
    public Tuple<LookAt, LookAt> getYawAndPitch(BlockState blockState) {
        return new Tuple<>(LookAt.of(
                ((blockState.getValue(BlockStateProperties.ROTATION_16) * 22.5F) + 180) % 360
        ), LookAt.PlayerPitch);
    }
//@Override
//public Pair<LookAt, LookAt> getYawAndPitch(BlockState blockState) {
//    return new Pair<>(LookAt.of(
//            ((blockState.get(Properties.ROTATION) * 22.5F) + 180) % 360
//    ), LookAt.PlayerPitch);
//}

    @Override
    public void firstAction(BlockState stateSchematic, BlockHitResult blockHitResult) {
        PlayerBlockAction.openSignEditorAction.count++;
        if(stateSchematic.getValue(BlockStateProperties.ATTACHED)){
            PlayerInputAction.SetShift(true);
        }
    }

    @Override
    public void afterAction(BlockState stateSchematic, BlockHitResult blockHitResult) {
        if(stateSchematic.getValue(BlockStateProperties.ATTACHED)){
            PlayerInputAction.SetShift(false);
        }
    }

    @Override
    public Tuple<RelativeBlockHitResult, Integer> getHitResult(BlockState blockState, BlockPos blockPos, BlockState worldBlockState) {
        return this.canSurvive(blockState, Minecraft.getInstance().level, blockPos) ?
                new Tuple<>(
                        new RelativeBlockHitResult(new Vec3(0.5, 0, 0.5),
                                Direction.DOWN,
                                blockPos.above(),
                                false)
                        , 1) : null;
    }

}
