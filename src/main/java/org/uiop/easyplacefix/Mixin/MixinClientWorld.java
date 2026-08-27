package org.uiop.easyplacefix.mixin;

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
        // startPredicting() sets isPredicting=true and must be balanced with close(),
        // otherwise the client's block-prediction state leaks and desyncs. Use
        // try-with-resources so the handler is closed even though we send the packet
        // ourselves.
        try (BlockStatePredictionHandler handler = this.blockStatePredictionHandler.startPredicting()) {
            return handler.currentSequence();
        }
    }
}
