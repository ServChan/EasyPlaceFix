package org.uiop.easyplacefix.Mixin.block;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.GrowingPlantBlock;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.uiop.easyplacefix.IBlock;

@Mixin(GrowingPlantBlock.class)
public abstract class MixinAbstractPlantPartBlock implements IBlock {


    @Shadow protected abstract GrowingPlantHeadBlock getHeadBlock();

    @Override
    public Item getItemForBlockState(BlockState blockState) {
        return  this.getHeadBlock().asItem();
    }

}
