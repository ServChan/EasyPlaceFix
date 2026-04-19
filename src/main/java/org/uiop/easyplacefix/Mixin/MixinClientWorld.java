package org.uiop.easyplacefix.Mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.uiop.easyplacefix.IClientWorld;

@Mixin(ClientLevel.class)
public class MixinClientWorld implements IClientWorld {
    @Shadow
    @Final
    private BlockStatePredictionHandler blockStatePredictionHandler;

    @Override
    public int Sequence() {
        return this.blockStatePredictionHandler.startPredicting().currentSequence();
    }
}
