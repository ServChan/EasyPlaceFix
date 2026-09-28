package org.uiop.easyplacefix.mixin.block.iCanUse;

import net.minecraft.world.level.block.StonecutterBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.ICanUse;

@Mixin(StonecutterBlock.class)
public class ICanUseStonecutterBlock implements ICanUse {
}
