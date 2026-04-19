package org.uiop.easyplacefix.Mixin.block.iCanUse;

import net.minecraft.world.level.block.FenceGateBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.ICanUse;
@Mixin(FenceGateBlock.class)
public class ICanUseFenceGateBlock implements ICanUse {
}
