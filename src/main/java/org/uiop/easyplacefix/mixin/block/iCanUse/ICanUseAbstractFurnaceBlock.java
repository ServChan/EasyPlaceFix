package org.uiop.easyplacefix.mixin.block.iCanUse;

import net.minecraft.world.level.block.AbstractFurnaceBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.ICanUse;

@Mixin(AbstractFurnaceBlock.class)
public class ICanUseAbstractFurnaceBlock implements ICanUse {
}
