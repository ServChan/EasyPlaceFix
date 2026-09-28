package org.uiop.easyplacefix.mixin.block.iCanUse;

import net.minecraft.world.level.block.AbstractCauldronBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.ICanUse;

@Mixin(AbstractCauldronBlock.class)
public class ICanUseAbstractCauldronBlock implements ICanUse {
}
