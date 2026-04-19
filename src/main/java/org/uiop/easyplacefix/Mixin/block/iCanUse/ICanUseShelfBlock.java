package org.uiop.easyplacefix.Mixin.block.iCanUse;

import net.minecraft.world.level.block.ShelfBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.ICanUse;

@Mixin(ShelfBlock.class)
public class ICanUseShelfBlock implements ICanUse {
}

