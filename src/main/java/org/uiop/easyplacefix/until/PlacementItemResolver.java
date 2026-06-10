package org.uiop.easyplacefix.until;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DecoratedPotBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.uiop.easyplacefix.IBlock;

public final class PlacementItemResolver {
    private PlacementItemResolver() {
    }

    public static ItemStack getPlacementStack(BlockState stateSchematic, BlockPos pos, Level schematicWorld) {
        if (stateSchematic.getBlock() instanceof DecoratedPotBlock) {
            BlockEntity blockEntity = schematicWorld.getBlockEntity(pos);
            if (blockEntity instanceof DecoratedPotBlockEntity decoratedPotBlockEntity) {
                return DecoratedPotBlockEntity.createDecoratedPotInstance(decoratedPotBlockEntity.getDecorations());
            }
        }

        return new ItemStack(((IBlock) stateSchematic.getBlock()).getItemForBlockState(stateSchematic));
    }
}
