package org.uiop.easyplacefix.mixin.block.iCanUse;

import net.minecraft.world.level.block.BellBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.ICanUse;

@Mixin(BellBlock.class)
public class ICanUseBellBlock implements ICanUse {
}
