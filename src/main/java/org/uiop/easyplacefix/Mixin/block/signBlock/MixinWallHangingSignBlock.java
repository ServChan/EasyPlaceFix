package org.uiop.easyplacefix.Mixin.block.signBlock;

import fi.dy.masa.litematica.world.SchematicWorldHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.WallHangingSignBlock;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.ICanUse;
import org.uiop.easyplacefix.LookAt;
import org.uiop.easyplacefix.data.RelativeBlockHitResult;
import org.uiop.easyplacefix.until.PlayerBlockAction;
import org.uiop.easyplacefix.until.PlayerInputAction;

@Mixin(WallHangingSignBlock.class)
public abstract class MixinWallHangingSignBlock implements IBlock {
    @Shadow
    public abstract boolean canAttachAt(BlockState state, WorldView world, BlockPos pos);

    @Override
    public boolean HasSleepTime(BlockState blockState) {
        return true;
    }

    @Override
    public void firstAction(BlockState stateSchematic, BlockHitResult blockHitResult) {
        PlayerBlockAction.openSignEditorAction.count++;
}

@Override
public Pair<LookAt, LookAt> getYawAndPitch(BlockState blockState) {
    return switch (blockState.get(Properties.HORIZONTAL_FACING)) {
        case SOUTH -> new Pair<>(LookAt.South, LookAt.PlayerPitch);
        case WEST -> new Pair<>(LookAt.West, LookAt.PlayerPitch);
        case EAST -> new Pair<>(LookAt.East, LookAt.PlayerPitch);
        default -> new Pair<>(LookAt.North, LookAt.PlayerPitch);
    };
}

    @Override
    public Pair<RelativeBlockHitResult, Integer> getHitResult(BlockState blockState, BlockPos blockPos, BlockState worldBlockState) {
        var direction = blockState.get(Properties.HORIZONTAL_FACING);
        return canAttachAt(blockState, MinecraftClient.getInstance().world, blockPos) ?
                new Pair<>(
                        new RelativeBlockHitResult(new Vec3d(0.5, 0.5, 0.5),
                                direction,
                                blockPos.offset(direction.getOpposite()),
                                false
                        ), 1) : null;
    }
}
