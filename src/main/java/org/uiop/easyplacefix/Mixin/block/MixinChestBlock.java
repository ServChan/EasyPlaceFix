package org.uiop.easyplacefix.Mixin.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Pair;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.data.RelativeBlockHitResult;
import org.uiop.easyplacefix.until.PlayerInputAction;

@Mixin(ChestBlock.class)
public class MixinChestBlock implements IBlock {
    @Shadow
    @Final
    public static EnumProperty<ChestType> CHEST_TYPE;

    @Override
    public void firstAction(BlockState stateSchematic, BlockHitResult blockHitResult) {
        PlayerInputAction.SetShift(true);
    }

    @Override
    public void afterAction(BlockState stateSchematic, BlockHitResult blockHitResult) {

        PlayerInputAction.SetShift(false);
    }

    @Override
    public Pair<RelativeBlockHitResult, Integer> getHitResult(BlockState blockState, BlockPos blockPos, BlockState worldBlockState) {

        ChestType chestType = blockState.get(Properties.CHEST_TYPE);
        if (chestType == ChestType.SINGLE) {
            MinecraftClient.getInstance().getNetworkHandler().sendPacket(new PlayerInputC2SPacket(new PlayerInput(false, false, false, false, false, true, false)));

            return new Pair<>(new RelativeBlockHitResult(
                    new Vec3d(0.5, 0.5, 0.5),
                    Direction.UP,
                    blockPos, false
            ), 1);
        }
        Direction blockFace = blockState.get(Properties.HORIZONTAL_FACING);

        if (chestType == ChestType.LEFT) {
            blockFace = blockFace.rotateYCounterclockwise();
        } else {
            blockFace = blockFace.rotateYClockwise();
        }
        BlockPos offset = blockPos.offset(blockFace.getOpposite());

        return new Pair<>(new RelativeBlockHitResult(
                switch (blockFace) {
                    case EAST -> new Vec3d(0.9, 0.5, 0.5);
                    case SOUTH -> new Vec3d(0.5, 0.5, 0.9);
                    case WEST -> new Vec3d(0.1, 0.5, 0.5);
                    default -> new Vec3d(0.5, 0.5, 0.1);
                },
                blockFace,
                MinecraftClient.getInstance().world.
                        getBlockState(offset).
                        getBlock() == Blocks.AIR ? blockPos : offset
                , false
        ), 1);

    }
}
