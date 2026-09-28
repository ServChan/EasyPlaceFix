package org.uiop.easyplacefix.mixin.block.iCanUse;

import net.minecraft.world.level.block.DiodeBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.ICanUse;

@Mixin(DiodeBlock.class)
public class ICanUseDiodeBlock implements ICanUse {
}
