package org.uiop.easyplacefix.Mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static org.uiop.easyplacefix.until.PlayerBlockAction.useItemOnAction.modifyBoolean;
import static org.uiop.easyplacefix.until.PlayerBlockAction.useItemOnAction.pistonBlockState;

@Mixin(PistonBaseBlock.class)
public class MixinPistonBlock {//Sync piston client placement state to avoid client/server desync during placement.

    @ModifyReturnValue(method = "getStateForPlacement", at = @At(value = "RETURN"))
    private BlockState ModgetPlacementState(BlockState original) {
        if (modifyBoolean) {//Only override while placing a piston
            modifyBoolean = false;
            return pistonBlockState;//Schematic block state
        }
        return original;
    }
}
