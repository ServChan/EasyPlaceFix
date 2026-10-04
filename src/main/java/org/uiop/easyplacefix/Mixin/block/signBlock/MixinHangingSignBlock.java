package org.uiop.easyplacefix.Mixin.block.signBlock;

import net.minecraft.block.BlockState;
import net.minecraft.block.HangingSignBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.LookAt;
import org.uiop.easyplacefix.data.RelativeBlockHitResult;
import org.uiop.easyplacefix.until.PlayerBlockAction;
import org.uiop.easyplacefix.until.PlayerInputAction;

@Mixin(HangingSignBlock.class)
public abstract class MixinHangingSignBlock implements IBlock {

    @Shadow
    protected abstract boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos);
    @Override
    public Pair<Float, Float> getLimitYawAndPitch(BlockState blockState) {
        Pair<LookAt, LookAt> lookAtPair = getYawAndPitch(blockState);
        return new Pair<>(
                lookAtPair.getLeft().Value(),
                lookAtPair.getRight().Value()
        );
    }
    @Override
    public Pair<LookAt, LookAt> getYawAndPitch(BlockState blockState) {
        return new Pair<>(LookAt.of(
                ((blockState.get(Properties.ROTATION) * 22.5F) + 180) % 360
        ), LookAt.PlayerPitch);
    }

    @Override
    public void firstAction(BlockState stateSchematic, BlockHitResult blockHitResult) {
        PlayerBlockAction.openSignEditorAction.count++;
        if(stateSchematic.get(Properties.ATTACHED)){
            PlayerInputAction.SetShift(true);
        }
    }

    @Override
    public void afterAction(BlockState stateSchematic, BlockHitResult blockHitResult) {
        if(stateSchematic.get(Properties.ATTACHED)){
            PlayerInputAction.SetShift(false);
        }
    }

    @Override
    public Pair<RelativeBlockHitResult, Integer> getHitResult(BlockState blockState, BlockPos blockPos, BlockState worldBlockState) {
        return this.canPlaceAt(blockState, MinecraftClient.getInstance().world, blockPos) ?
                new Pair<>(
                        new RelativeBlockHitResult(new Vec3d(0.5, 0, 0.5),
                                Direction.DOWN,
                                blockPos.up(),
                                false)
                        , 1) : null;
    }

}
