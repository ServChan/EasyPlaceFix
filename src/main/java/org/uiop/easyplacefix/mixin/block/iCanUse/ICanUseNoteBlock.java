package org.uiop.easyplacefix.mixin.block.iCanUse;

import net.minecraft.world.level.block.NoteBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.ICanUse;

@Mixin(NoteBlock.class)
public class ICanUseNoteBlock implements ICanUse {
}
