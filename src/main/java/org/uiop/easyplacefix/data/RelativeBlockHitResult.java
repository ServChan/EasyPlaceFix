package org.uiop.easyplacefix.data;

import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class RelativeBlockHitResult extends BlockHitResult {
    public RelativeBlockHitResult(Vec3d pos, Direction side, BlockPos blockPos, boolean insideBlock) {
        super(pos, side, blockPos, insideBlock);
    }

}
