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

/**
 * Auto-tunes note blocks to the pitch stored in the schematic.
 * <p>
 * Placement itself needs a single click (top face); the schematic note is then
 * matched by {@link NoteBlockHelper}, which rate-limits the tuning clicks so a
 * wall of note blocks does not trip server timer anti-cheat.
 * <p>
 * Backported from EasyPlaceFix 0.6.5 (Minecraft 26.x) to Minecraft 1.21.11.
 */
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
