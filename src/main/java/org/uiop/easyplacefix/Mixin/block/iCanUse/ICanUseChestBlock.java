package org.uiop.easyplacefix.Mixin.block.iCanUse;

import net.minecraft.world.level.block.ChestBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.ICanUse;
@Mixin(ChestBlock.class)
public class ICanUseChestBlock implements ICanUse {
}
