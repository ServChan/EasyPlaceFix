package org.uiop.easyplacefix.mixin.block.iCanUse;

import net.minecraft.world.level.block.BarrelBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.ICanUse;

@Mixin(BarrelBlock.class)
public class ICanUseBarrelBlock implements ICanUse {
}
