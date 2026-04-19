package org.uiop.easyplacefix.Mixin.block.iCanUse;

import net.minecraft.world.level.block.HopperBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.ICanUse;
@Mixin(HopperBlock.class)
public class ICanUseHopperBlock implements ICanUse {
}
