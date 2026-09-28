package org.uiop.easyplacefix.mixin.block.iCanUse;

import net.minecraft.world.level.block.CraftingTableBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.ICanUse;

@Mixin(CraftingTableBlock.class)
public class ICanUseCraftingTableBlock implements ICanUse {
}
