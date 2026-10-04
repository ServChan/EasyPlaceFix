package org.uiop.easyplacefix.Mixin.block;

import net.minecraft.block.AbstractPlantPartBlock;
import net.minecraft.block.AbstractPlantStemBlock;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.uiop.easyplacefix.IBlock;

@Mixin(AbstractPlantPartBlock.class)
public abstract class MixinAbstractPlantPartBlock implements IBlock {

    @Shadow protected abstract AbstractPlantStemBlock getStem();

    @Override
    public Item getItemForBlockState(BlockState blockState) {
        return  this.getStem().asItem();
    }

}
