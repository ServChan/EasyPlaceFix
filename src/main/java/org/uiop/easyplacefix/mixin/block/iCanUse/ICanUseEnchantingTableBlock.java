package org.uiop.easyplacefix.mixin.block.iCanUse;

import net.minecraft.world.level.block.EnchantingTableBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.ICanUse;

@Mixin(EnchantingTableBlock.class)
public class ICanUseEnchantingTableBlock implements ICanUse {
}
