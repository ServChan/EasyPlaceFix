package org.uiop.easyplacefix;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;

public interface IClientPlayerInteractionManager {
    default void syn() {
    }

    default void syn2(LocalPlayer player, InteractionHand hand, BlockHitResult hitResult) {
    }
}
