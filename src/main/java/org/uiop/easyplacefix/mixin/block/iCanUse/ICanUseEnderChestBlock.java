package org.uiop.easyplacefix.mixin.block.iCanUse;

import net.minecraft.world.level.block.EnderChestBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.ICanUse;

@Mixin(EnderChestBlock.class)
public class ICanUseEnderChestBlock implements ICanUse {
}
