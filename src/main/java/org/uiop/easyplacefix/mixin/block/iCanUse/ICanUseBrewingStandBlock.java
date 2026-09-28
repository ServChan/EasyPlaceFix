package org.uiop.easyplacefix.mixin.block.iCanUse;

import net.minecraft.world.level.block.BrewingStandBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.ICanUse;

@Mixin(BrewingStandBlock.class)
public class ICanUseBrewingStandBlock implements ICanUse {
}
