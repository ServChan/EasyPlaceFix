package org.uiop.easyplacefix.mixin.block.iCanUse;

import net.minecraft.world.level.block.AnvilBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.ICanUse;
@Mixin(AnvilBlock.class)
public class ICanUseAnvilBlock implements ICanUse {
}
