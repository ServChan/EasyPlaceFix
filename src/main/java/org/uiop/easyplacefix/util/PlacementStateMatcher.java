package org.uiop.easyplacefix.util;

import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.RedstoneWireBlock;
import net.minecraft.world.level.block.ShelfBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SnowyBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.SlabType;

import java.util.List;

public final class PlacementStateMatcher {
    private static final List<Property<?>> ORIENTATION_PROPERTIES = List.of(
            BlockStateProperties.FACING,
            BlockStateProperties.FACING_HOPPER,
            BlockStateProperties.HORIZONTAL_FACING,
            BlockStateProperties.AXIS,
            BlockStateProperties.HORIZONTAL_AXIS,
            BlockStateProperties.HALF,
            BlockStateProperties.ATTACH_FACE,
            BlockStateProperties.ROTATION_16,
            BlockStateProperties.DOOR_HINGE,
            BlockStateProperties.ORIENTATION,
            BlockStateProperties.VERTICAL_DIRECTION,
            BlockStateProperties.BELL_ATTACHMENT,
            BlockStateProperties.HANGING
    );

    private PlacementStateMatcher() {
    }

    public static boolean isSatisfied(BlockState schematic, BlockState world) {
        if (schematic.getBlock() != world.getBlock()) {
            return false;
        }

        if (schematic.getBlock() instanceof NoteBlock) {
            return schematic.getValue(BlockStateProperties.NOTE).equals(world.getValue(BlockStateProperties.NOTE));
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

        if (schematic.getBlock() instanceof CrossCollisionBlock
                || schematic.getBlock() instanceof WallBlock
                || schematic.getBlock() instanceof RedstoneWireBlock
                || schematic.getBlock() instanceof SnowyBlock) {
            return true;
        }

        return schematic.equals(world);
    }

    public static boolean isStructurallyWrong(BlockState schematic, BlockState world) {
        if (schematic.getBlock() != world.getBlock()) {
            return true;
        }
        if (schematic.getBlock() instanceof SlabBlock) {
            SlabType wanted = schematic.getValue(BlockStateProperties.SLAB_TYPE);
            return wanted != SlabType.DOUBLE && world.getValue(BlockStateProperties.SLAB_TYPE) != wanted;
        }
        for (Property<?> property : ORIENTATION_PROPERTIES) {
            if (schematic.hasProperty(property) && world.hasProperty(property)
                    && !schematic.getValue(property).equals(world.getValue(property))) {
                return true;
            }
        }
        return false;
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
