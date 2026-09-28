package org.uiop.easyplacefix.mixin.block.iCanUse;

import net.minecraft.world.level.block.DaylightDetectorBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.ICanUse;

@Mixin(DaylightDetectorBlock.class)
public class ICanUseDaylightDetectorBlock implements ICanUse {
}
