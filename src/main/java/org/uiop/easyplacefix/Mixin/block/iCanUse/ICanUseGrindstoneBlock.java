package org.uiop.easyplacefix.Mixin.block.iCanUse;

import net.minecraft.world.level.block.GrindstoneBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.ICanUse;
@Mixin(GrindstoneBlock.class)
public class ICanUseGrindstoneBlock implements ICanUse {
}
