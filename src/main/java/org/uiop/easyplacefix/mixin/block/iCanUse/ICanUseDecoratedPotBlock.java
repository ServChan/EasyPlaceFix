package org.uiop.easyplacefix.mixin.block.iCanUse;

import net.minecraft.world.level.block.DecoratedPotBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.ICanUse;

@Mixin(DecoratedPotBlock.class)
public class ICanUseDecoratedPotBlock implements ICanUse {
}
