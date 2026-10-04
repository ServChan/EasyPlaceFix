package org.uiop.easyplacefix.Mixin.block.signBlock;

import com.tick_ins.tick.RunnableWithCountDown;
import com.tick_ins.tick.TickThread;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.SignBlock;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.Tick;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.ICanUse;
import org.uiop.easyplacefix.LookAt;
import org.uiop.easyplacefix.data.RelativeBlockHitResult;
import org.uiop.easyplacefix.until.PlayerBlockAction;
import org.uiop.easyplacefix.until.PlayerInputAction;

@Mixin(SignBlock.class)
public abstract class MixinSignBlock implements IBlock {
    @Shadow
    protected abstract boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos);

    @Override
    public void afterAction(BlockState stateSchematic, BlockHitResult blockHitResult) {
        BlockState blockState = MinecraftClient.getInstance().world.getBlockState(blockHitResult.getBlockPos().down());
        if (blockState.getBlock() instanceof ICanUse){
            PlayerInputAction.SetShift(false);
        }
    }

    @Override
    public void firstAction(BlockState stateSchematic, BlockHitResult blockHitResult) {
        PlayerBlockAction.openSignEditorAction.count++;
       BlockState blockState = MinecraftClient.getInstance().world.getBlockState(blockHitResult.getBlockPos().down());
        if (blockState.getBlock() instanceof ICanUse){
            PlayerInputAction.SetShift(true);
        }
    }

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
                ((blockState.get(Properties.ROTATION) * 22.5F)+180) % 360
        ), LookAt.PlayerPitch);
    }

    @Override
    public Pair<RelativeBlockHitResult, Integer> getHitResult(BlockState blockState, BlockPos blockPos, BlockState worldBlockState) {
        return this.canPlaceAt(blockState, MinecraftClient.getInstance().world, blockPos) ? new Pair<>(
                new RelativeBlockHitResult(
                        new Vec3d(0.5, 1, 0.5),
                        Direction.UP,
                        blockPos.down(),
                        false
                ), 1) : null;
    }
}
