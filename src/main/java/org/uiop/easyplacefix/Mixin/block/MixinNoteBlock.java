package org.uiop.easyplacefix.Mixin.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.NoteBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.data.RelativeBlockHitResult;
import org.uiop.easyplacefix.until.NoteBlockHelper;

@Mixin(NoteBlock.class)
public abstract class MixinNoteBlock implements IBlock {
    @Override
    public Pair<RelativeBlockHitResult, Integer> getHitResult(BlockState blockState, BlockPos blockPos, BlockState worldBlockState) {
        return new Pair<>(new RelativeBlockHitResult(
                new Vec3d(0.5, 0.5, 0.5),
                Direction.UP,
                blockPos,
                false
        ), 1);
    }

    @Override
    public void afterAction(BlockState stateSchematic, BlockHitResult blockHitResult) {
        int targetNote = stateSchematic.get(Properties.NOTE);
        if (targetNote > 0) {
            NoteBlockHelper.tune(MinecraftClient.getInstance(), blockHitResult.getBlockPos(), targetNote);
        }
    }
}
