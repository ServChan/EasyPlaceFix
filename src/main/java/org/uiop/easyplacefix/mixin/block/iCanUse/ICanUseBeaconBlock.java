package org.uiop.easyplacefix.mixin.block.iCanUse;

import net.minecraft.world.level.block.BeaconBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.ICanUse;

@Mixin(BeaconBlock.class)
public class ICanUseBeaconBlock implements ICanUse {
}
