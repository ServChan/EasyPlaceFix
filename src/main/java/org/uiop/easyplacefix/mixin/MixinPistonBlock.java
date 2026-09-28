package org.uiop.easyplacefix.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static org.uiop.easyplacefix.util.PlayerBlockAction.useItemOnAction.modifyBoolean;
import static org.uiop.easyplacefix.util.PlayerBlockAction.useItemOnAction.pistonBlockState;

@Mixin(PistonBaseBlock.class)
public class MixinPistonBlock {

    @ModifyReturnValue(method = "getStateForPlacement", at = @At(value = "RETURN"))
    private BlockState ModgetPlacementState(BlockState original) {
        if (modifyBoolean && original != null && pistonBlockState != null && Minecraft.getInstance().isSameThread()) {
            modifyBoolean = false;
            return pistonBlockState.setValue(BlockStateProperties.EXTENDED, false);
        }
        return original;
    }
}
