package org.uiop.easyplacefix.until;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import org.uiop.easyplacefix.struct.DirectionRange;

public class PlayerRotationAction {

    public static void setServerBoundPlayerRotation(Float yaw, Float pitch, Boolean hor) {
        Minecraft minecraftClient = Minecraft.getInstance();
        minecraftClient.getConnection().send(
                new ServerboundMovePlayerPacket.Rot(
                        yaw,
                        pitch,
                        Minecraft.getInstance().player.onGround(), hor//parameter kept from vanilla packet format

                )
        );
    }

    public static void restRotation() {
        Minecraft minecraftClient = Minecraft.getInstance();
        minecraftClient.getConnection().send(
                new ServerboundMovePlayerPacket.Rot(
                        minecraftClient.player.getYRot(),
                        minecraftClient.player.getXRot(),
                        Minecraft.getInstance().player.onGround(),
                        minecraftClient.player.horizontalCollision//parameter kept from vanilla packet format

                )
        );
    }

    public static Float limitYawRotation(Direction direction) {

        DirectionRange directionRange = DirectionRange.DirectionToRange(direction);
        if (directionRange == null) return null;
        Direction playerFacing = Minecraft.getInstance().player.getMotionDirection();
        if (directionRange.isInRange(playerFacing)) {
            return Minecraft.getInstance().player.getYRot();
        } else {
            float range1 = Math.abs(directionRange.getFirstValue() - Minecraft.getInstance().player.getYRot());
            float range2 = Math.abs(directionRange.getSecondValue() - Minecraft.getInstance().player.getYRot());
            return range1 < range2 ? directionRange.getFirstValue() : directionRange.getSecondValue();
        }
    }

    public static Float limitPitchRotation(Direction direction) {
        DirectionRange directionRange = DirectionRange.DirectionToRange(direction);
        if (directionRange == null) return null;
        Direction playerFacing = getVertical(Minecraft.getInstance().player.getXRot());

        if (directionRange.isInRange(playerFacing)) {
            return Minecraft.getInstance().player.getXRot();
        } else {
            float range1 = Math.abs(directionRange.getFirstValue() - Minecraft.getInstance().player.getXRot());
            float range2 = Math.abs(directionRange.getSecondValue() - Minecraft.getInstance().player.getXRot());
            return range1 < range2 ? directionRange.getFirstValue() : directionRange.getSecondValue();
        }
    }

    public static Direction getVertical(float pitchPlayer) {
        Direction playerFacing = null;

        if (pitchPlayer < -45) {
            playerFacing = Direction.UP;
        } else if (pitchPlayer > 45) {
            playerFacing = Direction.DOWN;
        }
        return playerFacing;
    }
}
