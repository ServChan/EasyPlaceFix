package org.uiop.easyplacefix.Mixin.block;

import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.litematica.world.WorldSchematic;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.ObserverBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.LookAt;

import static org.uiop.easyplacefix.config.easyPlacefixConfig.OBSERVER_DETECT;
import static org.uiop.easyplacefix.until.doEasyPlace.isSchematicBlock;

@Mixin(ObserverBlock.class)
public class MixinObserverBlock implements IBlock {
    @Override
    public boolean HasSleepTime(BlockState blockState) {
      Direction facing = blockState.getValue(BlockStateProperties.FACING);
        return facing != Direction.UP && facing != Direction.DOWN;
    }

    @Override
    public Tuple<LookAt, LookAt> getYawAndPitch(BlockState blockState) {
        return switch (blockState.getValue(BlockStateProperties.FACING)) {
            case DOWN -> new Tuple<>(LookAt.PlayerYaw, LookAt.Down);
            case UP -> new Tuple<>(LookAt.PlayerYaw, LookAt.Up);
            case SOUTH -> new Tuple<>(LookAt.South, LookAt.Horizontal);
            case WEST -> new Tuple<>(LookAt.West, LookAt.Horizontal);
            case EAST -> new Tuple<>(LookAt.East, LookAt.Horizontal);
            case NORTH -> new Tuple<>(LookAt.North, LookAt.Horizontal);
        };
    }

    @Override
    public InteractionResult isSchemaTermination(BlockPos pos, BlockState blockState, BlockState worldBlockstate) {
        // Observer placement validation
        if (OBSERVER_DETECT.getBooleanValue()) {
            Direction direction = blockState.getValue(BlockStateProperties.FACING);
            BlockPos offset = pos.relative(direction);
            WorldSchematic schematicWorld = SchematicWorldHandler.getSchematicWorld();
            // Check whether observer target is within schematic
            if (isSchematicBlock(offset) && schematicWorld != null) {
                BlockState lookBlock = Minecraft.getInstance().level.getBlockState(offset);
                if (!schematicWorld.getBlockState(offset).getBlock().equals(lookBlock.getBlock()))
                    return InteractionResult.FAIL;
            }
        }//Needs facing and position parameters
        return null;
    }
}
