package org.uiop.easyplacefix.mixin.block.iCanUse;

import net.minecraft.world.level.block.LeverBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.ICanUse;

@Mixin(LeverBlock.class)
public class ICanUseLeverBlock implements ICanUse {
}
