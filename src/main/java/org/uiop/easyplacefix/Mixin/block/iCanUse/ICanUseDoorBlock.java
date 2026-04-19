package org.uiop.easyplacefix.Mixin.block.iCanUse;

import net.minecraft.world.level.block.DoorBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.ICanUse;
@Mixin(DoorBlock.class)
public class ICanUseDoorBlock implements ICanUse {
}
