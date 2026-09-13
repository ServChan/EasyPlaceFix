package org.uiop.easyplacefix.Mixin.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.IBlock;
import static org.uiop.easyplacefix.config.easyPlacefixConfig.Allow_Interaction;

@Mixin(RespawnAnchorBlock.class)
public abstract class MixinRespawnAnchorBlock implements IBlock {
    @Override public ActionResult isWorldTermination(BlockPos pos, BlockState schematic, BlockState world) {
        return Allow_Interaction.getBooleanValue() ? ActionResult.PASS : null;
    }
}
