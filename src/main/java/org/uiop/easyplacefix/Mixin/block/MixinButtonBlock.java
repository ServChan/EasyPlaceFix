package org.uiop.easyplacefix.Mixin.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.ButtonBlock;
import net.minecraft.block.enums.Attachment;
import net.minecraft.block.enums.BlockFace;
import net.minecraft.client.MinecraftClient;
import net.minecraft.state.property.Properties;
import net.minecraft.util.hit.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.ICanUse;
import org.uiop.easyplacefix.until.PlayerInputAction;

@Mixin(ButtonBlock.class)
public class MixinButtonBlock implements IBlock {
    @Override
    public void afterAction(BlockState stateSchematic, BlockHitResult blockHitResult) {
        if (stateSchematic.get(Properties.BLOCK_FACE) == BlockFace.CEILING) {
            BlockState blockState = MinecraftClient.getInstance().world.getBlockState(blockHitResult.getBlockPos().up());
            if (blockState.getBlock() instanceof ICanUse) {
                PlayerInputAction.SetShift(false);
            }

        } else if (stateSchematic.get(Properties.BLOCK_FACE) == BlockFace.FLOOR) {
            BlockState blockState = MinecraftClient.getInstance().world.getBlockState(blockHitResult.getBlockPos().down());
            if (blockState.getBlock() instanceof ICanUse) {
                PlayerInputAction.SetShift(false);
            }
        } else {
            BlockState blockState = MinecraftClient.getInstance().world.getBlockState(blockHitResult.getBlockPos().offset(stateSchematic.get(Properties.HORIZONTAL_FACING).getOpposite()));
            if (blockState.getBlock() instanceof ICanUse) {
                PlayerInputAction.SetShift(false);
            }
        }

    }

    @Override
    public void firstAction(BlockState stateSchematic, BlockHitResult blockHitResult) {
        if (stateSchematic.get(Properties.BLOCK_FACE) == BlockFace.CEILING) {
            BlockState blockState = MinecraftClient.getInstance().world.getBlockState(blockHitResult.getBlockPos().up());
            if (blockState.getBlock() instanceof ICanUse) {
                PlayerInputAction.SetShift(true);
            }

        } else if (stateSchematic.get(Properties.BLOCK_FACE) == BlockFace.FLOOR) {
            BlockState blockState = MinecraftClient.getInstance().world.getBlockState(blockHitResult.getBlockPos().down());
            if (blockState.getBlock() instanceof ICanUse) {
                PlayerInputAction.SetShift(true);
            }
        } else {
            BlockState blockState = MinecraftClient.getInstance().world.getBlockState(blockHitResult.getBlockPos().offset(stateSchematic.get(Properties.HORIZONTAL_FACING).getOpposite()));
            if (blockState.getBlock() instanceof ICanUse) {
                PlayerInputAction.SetShift(true);
            }
        }

    }
}
