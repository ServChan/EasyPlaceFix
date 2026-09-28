package org.uiop.easyplacefix.mixin.block.iCanUse;

import net.minecraft.world.level.block.ButtonBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.ICanUse;

@Mixin(ButtonBlock.class)
public class ICanUseButtonBlock implements ICanUse {
}
