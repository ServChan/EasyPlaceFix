package org.uiop.easyplacefix.Mixin.block.iCanUse;

import net.minecraft.world.level.block.CrafterBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.ICanUse;

@Mixin(CrafterBlock.class)
public class ICanUseCrafterBlock implements ICanUse {
}
