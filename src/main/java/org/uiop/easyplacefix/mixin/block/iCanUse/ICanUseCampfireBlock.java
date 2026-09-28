package org.uiop.easyplacefix.mixin.block.iCanUse;

import net.minecraft.world.level.block.CampfireBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.ICanUse;

@Mixin(CampfireBlock.class)
public class ICanUseCampfireBlock implements ICanUse {
}
