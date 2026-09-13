package org.uiop.easyplacefix.Mixin.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.EnchantingTableBlock;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.IBlock;
import static org.uiop.easyplacefix.config.easyPlacefixConfig.Allow_Interaction;

@Mixin(EnchantingTableBlock.class)
public abstract class MixinEnchantingTableBlock implements IBlock {
    @Override public ActionResult isWorldTermination(BlockPos pos, BlockState schematic, BlockState world) {
        return Allow_Interaction.getBooleanValue() ? ActionResult.PASS : null;
    }
}
