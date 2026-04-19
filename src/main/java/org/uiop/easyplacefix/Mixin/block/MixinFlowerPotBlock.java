package org.uiop.easyplacefix.Mixin.block;

import fi.dy.masa.litematica.materials.MaterialCache;
import fi.dy.masa.litematica.util.EntityUtils;
import fi.dy.masa.litematica.util.InventoryUtils;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.uiop.easyplacefix.IBlock;
@Mixin(FlowerPotBlock.class)
public abstract class MixinFlowerPotBlock implements IBlock {
    @Shadow public abstract Block getPotted();

    @Shadow protected abstract boolean isEmpty();

    @Override
    public Item getItemForBlockState(BlockState blockState) {
        return Blocks.FLOWER_POT.asItem();
    }

    @Override
    public void BlockAction(BlockState blockState, BlockHitResult blockHitResult) {
        if (!this.isEmpty()){//TODO extract placement logic and include block-to-item conversion

            Block flower = this.getPotted();
            ItemStack stack = new ItemStack(flower.asItem());
            InventoryUtils.schematicWorldPickBlock(stack, blockHitResult.getBlockPos(),  SchematicWorldHandler.getSchematicWorld(), Minecraft.getInstance());
            InteractionHand hand2 = EntityUtils.getUsedHandForItem(Minecraft.getInstance().player, stack);
            if (hand2==null)return;
            Minecraft.getInstance().gameMode.useItemOn(Minecraft.getInstance().player, hand2, blockHitResult);

        }


    }
}
