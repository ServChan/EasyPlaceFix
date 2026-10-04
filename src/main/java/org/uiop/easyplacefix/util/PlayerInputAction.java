package org.uiop.easyplacefix.util;

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.world.entity.player.Input;

public class PlayerInputAction {

    public static void SetShift(boolean isPressed) {
        Input playerInput = Minecraft.getInstance().player.getLastSentInput();
        boolean shift = isPressed || Minecraft.getInstance().player.input.keyPresses.shift();
        Minecraft.getInstance().getConnection().send(
                new ServerboundPlayerInputPacket(
                        new Input(
                                playerInput.forward(),
                                playerInput.backward(),
                                playerInput.left(), playerInput.
                                right(), playerInput.jump(),
                                shift,
                                playerInput.sprint()
                        ))
        );

    }
}
