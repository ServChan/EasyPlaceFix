package org.uiop.easyplacefix.Mixin.block.iCanUse;

import net.minecraft.world.level.block.LoomBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.ICanUse;
@Mixin(LoomBlock.class)
public class ICanUseLoomBlock implements ICanUse {
}
