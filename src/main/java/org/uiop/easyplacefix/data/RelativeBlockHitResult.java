package org.uiop.easyplacefix.data;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class RelativeBlockHitResult extends BlockHitResult {
    public RelativeBlockHitResult(Vec3 pos, Direction side, BlockPos blockPos, boolean insideBlock) {
        super(pos, side, blockPos, insideBlock);
    }
    //Stores relative coordinates directly to avoid unnecessary math and reduce overhead (idea from 7087z).
}
