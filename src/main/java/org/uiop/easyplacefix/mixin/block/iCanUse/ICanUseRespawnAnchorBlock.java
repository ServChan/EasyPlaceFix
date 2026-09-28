package org.uiop.easyplacefix.mixin.block.iCanUse;

import net.minecraft.world.level.block.RespawnAnchorBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.ICanUse;
@Mixin(RespawnAnchorBlock.class)
public class ICanUseRespawnAnchorBlock implements ICanUse {
}
