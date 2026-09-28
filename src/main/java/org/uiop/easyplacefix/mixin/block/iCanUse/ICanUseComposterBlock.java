package org.uiop.easyplacefix.mixin.block.iCanUse;

import net.minecraft.world.level.block.ComposterBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.ICanUse;

@Mixin(ComposterBlock.class)
public class ICanUseComposterBlock implements ICanUse {
}
