package org.uiop.easyplacefix.Mixin.block;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.ICanUse;
import org.uiop.easyplacefix.until.PlayerInputAction;

@Mixin(ButtonBlock.class)
public class MixinButtonBlock implements IBlock {
    @Override
    public void afterAction(BlockState stateSchematic, BlockHitResult blockHitResult) {
        if (stateSchematic.getValue(BlockStateProperties.ATTACH_FACE) == AttachFace.CEILING) {
            BlockState blockState = Minecraft.getInstance().level.getBlockState(blockHitResult.getBlockPos().above());
            if (blockState.getBlock() instanceof ICanUse) {
                PlayerInputAction.SetShift(false);
            }

        } else if (stateSchematic.getValue(BlockStateProperties.ATTACH_FACE) == AttachFace.FLOOR) {
            BlockState blockState = Minecraft.getInstance().level.getBlockState(blockHitResult.getBlockPos().below());
            if (blockState.getBlock() instanceof ICanUse) {
                PlayerInputAction.SetShift(false);
            }
        } else {
            BlockState blockState = Minecraft.getInstance().level.getBlockState(blockHitResult.getBlockPos().relative(stateSchematic.getValue(BlockStateProperties.HORIZONTAL_FACING).getOpposite()));
            if (blockState.getBlock() instanceof ICanUse) {
                PlayerInputAction.SetShift(false);
            }
        }

    }

    @Override
    public void firstAction(BlockState stateSchematic, BlockHitResult blockHitResult) {
        if (stateSchematic.getValue(BlockStateProperties.ATTACH_FACE) == AttachFace.CEILING) {
            BlockState blockState = Minecraft.getInstance().level.getBlockState(blockHitResult.getBlockPos().above());
            if (blockState.getBlock() instanceof ICanUse) {
                PlayerInputAction.SetShift(true);
            }

        } else if (stateSchematic.getValue(BlockStateProperties.ATTACH_FACE) == AttachFace.FLOOR) {
            BlockState blockState = Minecraft.getInstance().level.getBlockState(blockHitResult.getBlockPos().below());
            if (blockState.getBlock() instanceof ICanUse) {
                PlayerInputAction.SetShift(true);
            }
        } else {
            BlockState blockState = Minecraft.getInstance().level.getBlockState(blockHitResult.getBlockPos().relative(stateSchematic.getValue(BlockStateProperties.HORIZONTAL_FACING).getOpposite()));
            if (blockState.getBlock() instanceof ICanUse) {
                PlayerInputAction.SetShift(true);
            }
        }


    }
}
