package org.uiop.easyplacefix.Mixin.block.signBlock;

import fi.dy.masa.litematica.world.SchematicWorldHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.ICanUse;
import org.uiop.easyplacefix.LookAt;
import org.uiop.easyplacefix.data.RelativeBlockHitResult;
import org.uiop.easyplacefix.until.PlayerBlockAction;
import org.uiop.easyplacefix.until.PlayerInputAction;

@Mixin(WallHangingSignBlock.class)
public abstract class MixinWallHangingSignBlock implements IBlock {
    @Shadow
    public abstract boolean canPlace(BlockState state, LevelReader world, BlockPos pos);

    @Override
    public boolean HasSleepTime(BlockState blockState) {
        return true;
    }
//    @Override
//    public void afterAction(BlockState stateSchematic, BlockHitResult blockHitResult) {
//        BlockState blockState = MinecraftClient.getInstance().world.getBlockState(blockHitResult.getBlockPos().down());
////        if (blockState.getBlock() instanceof ICanUse){
////            PlayerInputAction.SetShift(false);
////        }
//    }
//This block does not use adjacent-side interaction
    @Override
    public void firstAction(BlockState stateSchematic, BlockHitResult blockHitResult) {
        PlayerBlockAction.openSignEditorAction.count++;
}

    //TODO TODO orientation packet may be avoidable, but support checks are unclear so keep it for now
//Send orientation packet because text-facing side depends on facing
@Override
public Tuple<LookAt, LookAt> getYawAndPitch(BlockState blockState) {
    return switch (blockState.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
        case SOUTH -> new Tuple<>(LookAt.South, LookAt.PlayerPitch);
        case WEST -> new Tuple<>(LookAt.West, LookAt.PlayerPitch);
        case EAST -> new Tuple<>(LookAt.East, LookAt.PlayerPitch);
        default -> new Tuple<>(LookAt.North, LookAt.PlayerPitch);
    };
}
//    @Override
//    public Pair<LookAt, LookAt> getYawAndPitch(BlockState blockState) {
//        switch (blockState.get(Properties.FACING)){
//            case WEST ->this.canAttachAt()
//        }
//    }

    @Override
    public Tuple<RelativeBlockHitResult, Integer> getHitResult(BlockState blockState, BlockPos blockPos, BlockState worldBlockState) {
        var direction = blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);
        return canPlace(blockState, Minecraft.getInstance().level, blockPos) ?
                new Tuple<>(
                        new RelativeBlockHitResult(new Vec3(0.5, 0.5, 0.5),
                                direction,
                                blockPos.relative(direction.getOpposite()),
                                false
                        ), 1) : null;
    }
}
