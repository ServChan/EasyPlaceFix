package org.uiop.easyplacefix.Mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.block.BlockState;
import net.minecraft.block.PistonBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static org.uiop.easyplacefix.until.PlayerBlockAction.useItemOnAction.modifyBoolean;
import static org.uiop.easyplacefix.until.PlayerBlockAction.useItemOnAction.pistonBlockState;

@Mixin(PistonBlock.class)
public class MixinPistonBlock {

    @ModifyReturnValue(method = "getPlacementState", at = @At(value = "RETURN"))
    private BlockState ModgetPlacementState(BlockState original) {
        if (modifyBoolean) {
            modifyBoolean = false;
            return pistonBlockState;
        }
        return original;
    }
}
