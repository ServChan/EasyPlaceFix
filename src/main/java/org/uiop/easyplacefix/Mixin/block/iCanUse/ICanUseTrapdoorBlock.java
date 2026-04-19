package org.uiop.easyplacefix.Mixin.block.iCanUse;

import net.minecraft.world.level.block.TrapDoorBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.ICanUse;
@Mixin(TrapDoorBlock.class)
public class ICanUseTrapdoorBlock implements ICanUse {
}
