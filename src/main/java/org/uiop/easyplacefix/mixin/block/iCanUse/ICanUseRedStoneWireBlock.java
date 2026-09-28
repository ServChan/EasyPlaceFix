package org.uiop.easyplacefix.mixin.block.iCanUse;

import net.minecraft.world.level.block.RedstoneWireBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.ICanUse;

@Mixin(RedstoneWireBlock.class)
public class ICanUseRedStoneWireBlock implements ICanUse {
}
