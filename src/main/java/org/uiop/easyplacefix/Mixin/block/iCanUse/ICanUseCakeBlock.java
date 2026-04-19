package org.uiop.easyplacefix.Mixin.block.iCanUse;

import net.minecraft.world.level.block.CakeBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.ICanUse;
@Mixin(CakeBlock.class)
public class ICanUseCakeBlock implements ICanUse {
}
