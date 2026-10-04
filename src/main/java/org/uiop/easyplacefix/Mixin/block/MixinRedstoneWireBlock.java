package org.uiop.easyplacefix.Mixin.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.block.enums.WireConnection;
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
import org.uiop.easyplacefix.data.RelativeBlockHitResult;

@Mixin(RedstoneWireBlock.class)
public abstract class MixinRedstoneWireBlock implements IBlock {
    @Shadow
    protected abstract boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos);

    @Override
    public Pair<RelativeBlockHitResult, Integer> getHitResult(BlockState blockState, BlockPos blockPos, BlockState worldBlockState) {
        return this.canPlaceAt(blockState, MinecraftClient.getInstance().world, blockPos)
                ? new Pair<>(
                new RelativeBlockHitResult(
                        new Vec3d(0.5, 0, 0.5),
                        Direction.UP,
                        blockPos.down(),
                        false
                ), (blockState.get(Properties.EAST_WIRE_CONNECTION) == WireConnection.NONE
                && blockState.get(Properties.WEST_WIRE_CONNECTION) == WireConnection.NONE
                && blockState.get(Properties.SOUTH_WIRE_CONNECTION) == WireConnection.NONE
                && blockState.get(Properties.NORTH_WIRE_CONNECTION) == WireConnection.NONE) ? 2 : 1
        ):null;
    }
}
