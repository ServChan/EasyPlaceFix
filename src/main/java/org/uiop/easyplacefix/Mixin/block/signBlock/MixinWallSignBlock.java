package org.uiop.easyplacefix.Mixin.block.signBlock;

import fi.dy.masa.litematica.world.SchematicWorldHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.WallSignBlock;
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
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.ICanUse;
import org.uiop.easyplacefix.data.RelativeBlockHitResult;
import org.uiop.easyplacefix.until.PlayerBlockAction;
import org.uiop.easyplacefix.until.PlayerInputAction;

@Mixin(WallSignBlock.class)
public abstract class MixinWallSignBlock implements IBlock {

    @Shadow
    protected abstract boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos);
    @Override
    public void afterAction(BlockState stateSchematic, BlockHitResult blockHitResult) {
        BlockState blockState = MinecraftClient.getInstance().world.getBlockState(blockHitResult.getBlockPos().offset(stateSchematic.get(Properties.HORIZONTAL_FACING).getOpposite()));
        if (blockState.getBlock() instanceof ICanUse){
            PlayerInputAction.SetShift(false);
        }
    }

    @Override
    public void firstAction(BlockState stateSchematic, BlockHitResult blockHitResult) {
        PlayerBlockAction.openSignEditorAction.count++;
        BlockState blockState = MinecraftClient.getInstance().world.getBlockState(blockHitResult.getBlockPos().offset(stateSchematic.get(Properties.HORIZONTAL_FACING).getOpposite()));
        if (blockState.getBlock() instanceof ICanUse){
            PlayerInputAction.SetShift(true);
        }
    }
    @Override
    public Pair<RelativeBlockHitResult, Integer> getHitResult(BlockState blockState, BlockPos blockPos, BlockState worldBlockState) {
        Direction direction = blockState.get(Properties.HORIZONTAL_FACING);
        return this.canPlaceAt(blockState, MinecraftClient.getInstance().world, blockPos) ?
                new Pair<>(
                        new RelativeBlockHitResult(
                                switch (direction) {
                                    case EAST -> new Vec3d(1, 0.5, 0.5);
                                    case SOUTH -> new Vec3d(0.5, 0.5, 1);
                                    case WEST -> new Vec3d(0, 0.5, 0.5);
                                    default -> new Vec3d(0.5, 0.5, 0);
                                },
                                direction,
                                blockPos.offset(direction.getOpposite()),
                                false
                        ), 1
                ) : null;
    }
}
