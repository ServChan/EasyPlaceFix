package org.uiop.easyplacefix.until;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Direction;
import org.uiop.easyplacefix.struct.DirectionRange;

public class PlayerRotationAction {

    public static void setServerBoundPlayerRotation(Float yaw, Float pitch, Boolean hor) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        minecraftClient.getNetworkHandler().sendPacket(
                new PlayerMoveC2SPacket.LookAndOnGround(
                        yaw,
                        pitch,
                        MinecraftClient.getInstance().player.isOnGround(), hor

                )
        );
    }

    public static void restRotation() {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        minecraftClient.getNetworkHandler().sendPacket(
                new PlayerMoveC2SPacket.LookAndOnGround(
                        minecraftClient.player.getYaw(),
                        minecraftClient.player.getPitch(),
                        MinecraftClient.getInstance().player.isOnGround(),
                        minecraftClient.player.horizontalCollision

                )
        );
    }

    public static Float limitYawRotation(Direction direction) {

        DirectionRange directionRange = DirectionRange.DirectionToRange(direction);
        if (directionRange == null) return null;
        Direction playerFacing = MinecraftClient.getInstance().player.getMovementDirection();
        if (directionRange.isInRange(playerFacing)) {
            return MinecraftClient.getInstance().player.getYaw();
        } else {
            float range1 = Math.abs(directionRange.getFirstValue() - MinecraftClient.getInstance().player.getYaw());
            float range2 = Math.abs(directionRange.getSecondValue() - MinecraftClient.getInstance().player.getYaw());
            return range1 < range2 ? directionRange.getFirstValue() : directionRange.getSecondValue();
        }
    }

    public static Float limitPitchRotation(Direction direction) {
        DirectionRange directionRange = DirectionRange.DirectionToRange(direction);
        if (directionRange == null) return null;
        Direction playerFacing = getVertical(MinecraftClient.getInstance().player.getPitch());

        if (directionRange.isInRange(playerFacing)) {
            return MinecraftClient.getInstance().player.getPitch();
        } else {
            float range1 = Math.abs(directionRange.getFirstValue() - MinecraftClient.getInstance().player.getPitch());
            float range2 = Math.abs(directionRange.getSecondValue() - MinecraftClient.getInstance().player.getPitch());
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
