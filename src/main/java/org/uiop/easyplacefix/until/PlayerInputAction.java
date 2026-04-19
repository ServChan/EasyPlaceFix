package org.uiop.easyplacefix.until;

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.world.entity.player.Input;

public class PlayerInputAction {

    public static void SetShift(boolean isPressed) {
        Input playerInput = Minecraft.getInstance().player.getLastSentInput();
        Minecraft.getInstance().getConnection().send(
                new ServerboundPlayerInputPacket(
                        new Input(
                                playerInput.forward(),
                                playerInput.backward(),
                                playerInput.left(), playerInput.
                                right(), playerInput.jump(),
                                isPressed,
                                playerInput.sprint()
                        ))
        );

    }
}
