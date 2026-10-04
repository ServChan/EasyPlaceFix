package org.uiop.easyplacefix.Mixin.block;

import fi.dy.masa.litematica.materials.MaterialCache;
import fi.dy.masa.litematica.util.EntityUtils;
import fi.dy.masa.litematica.util.InventoryUtils;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.uiop.easyplacefix.IBlock;
@Mixin(FlowerPotBlock.class)
public abstract class MixinFlowerPotBlock implements IBlock {
    @Shadow public abstract Block getContent();

    @Shadow protected abstract boolean isEmpty();

    @Override
    public Item getItemForBlockState(BlockState blockState) {
        return Blocks.FLOWER_POT.asItem();
    }

    @Override
    public void BlockAction(BlockState blockState, BlockHitResult blockHitResult) {
        if (!this.isEmpty()){

            Block flower = this.getContent();
            ItemStack stack = new ItemStack(flower.asItem());
            InventoryUtils.schematicWorldPickBlock(stack, blockHitResult.getBlockPos(),  SchematicWorldHandler.getSchematicWorld(), MinecraftClient.getInstance());
            Hand hand2 = EntityUtils.getUsedHandForItem(MinecraftClient.getInstance().player, stack);
            if (hand2==null)return;
            MinecraftClient.getInstance().interactionManager.interactBlock(MinecraftClient.getInstance().player, hand2, blockHitResult);

        }

    }
}
