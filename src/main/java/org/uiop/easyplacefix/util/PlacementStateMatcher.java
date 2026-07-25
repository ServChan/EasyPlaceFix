package org.uiop.easyplacefix.util;

import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.ShelfBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class PlacementStateMatcher {
    private PlacementStateMatcher() {
    }

    public static boolean isSatisfied(BlockState schematic, BlockState world) {
        if (schematic.getBlock() != world.getBlock()) {
            return false;
        }

        if (schematic.getBlock() instanceof StairBlock) {
            return hasSameHorizontalFacing(schematic, world)
                    && schematic.getValue(BlockStateProperties.HALF) == world.getValue(BlockStateProperties.HALF);
        }

        if (schematic.getBlock() instanceof TrapDoorBlock) {
            if (!hasSameHorizontalFacing(schematic, world)
                    || schematic.getValue(BlockStateProperties.HALF) != world.getValue(BlockStateProperties.HALF)) {
                return false;
            }

            boolean schematicPowered = schematic.hasProperty(BlockStateProperties.POWERED)
                    && schematic.getValue(BlockStateProperties.POWERED);
            boolean worldPowered = world.hasProperty(BlockStateProperties.POWERED)
                    && world.getValue(BlockStateProperties.POWERED);
            if (schematicPowered || worldPowered) {
                return true;
            }

            return schematic.getValue(BlockStateProperties.OPEN) == world.getValue(BlockStateProperties.OPEN);
        }

        if (schematic.getBlock() instanceof ShelfBlock || schematic.getBlock() instanceof LecternBlock) {
            return hasSameHorizontalFacing(schematic, world);
        }

        return schematic.equals(world);
    }

    public static boolean shouldUsePlacementOverride(BlockState blockState) {
        return blockState.getBlock() instanceof StairBlock
                || blockState.getBlock() instanceof TrapDoorBlock
                || blockState.getBlock() instanceof ShelfBlock
                || blockState.getBlock() instanceof LecternBlock;
    }

    private static boolean hasSameHorizontalFacing(BlockState schematic, BlockState world) {
        return schematic.getValue(BlockStateProperties.HORIZONTAL_FACING)
                == world.getValue(BlockStateProperties.HORIZONTAL_FACING);
    }
}
