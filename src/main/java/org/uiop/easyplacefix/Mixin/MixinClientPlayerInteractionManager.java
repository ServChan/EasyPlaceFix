package org.uiop.easyplacefix.Mixin;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.uiop.easyplacefix.IClientPlayerInteractionManager;

@Mixin(MultiPlayerGameMode.class)
public abstract class MixinClientPlayerInteractionManager implements IClientPlayerInteractionManager {
    @Shadow
    protected abstract void ensureHasSentCarriedItem();

    @Shadow
    protected abstract InteractionResult performUseItemOn(LocalPlayer player, InteractionHand hand, BlockHitResult hitResult);

    @Override
    public void syn2(LocalPlayer player, InteractionHand hand, BlockHitResult hitResult) {
        this.performUseItemOn(player, hand, hitResult);
    }

    @Override
    public void syn() {
        this.ensureHasSentCarriedItem();
    }
}
