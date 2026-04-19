package org.uiop.easyplacefix;

import net.minecraft.client.Minecraft;

public final class LookAt {
    private static final int TYPE_STATIC = 0;
    private static final int TYPE_PLAYER_YAW = 1;
    private static final int TYPE_PLAYER_PITCH = 2;

    public static final LookAt East = new LookAt(-90f, TYPE_STATIC);
    public static final LookAt West = new LookAt(90f, TYPE_STATIC);
    public static final LookAt North = new LookAt(180f, TYPE_STATIC);
    public static final LookAt South = new LookAt(0f, TYPE_STATIC);
    public static final LookAt Down = new LookAt(90f, TYPE_STATIC);
    public static final LookAt Up = new LookAt(-90f, TYPE_STATIC);
    public static final LookAt Horizontal = new LookAt(0f, TYPE_STATIC);
    public static final LookAt PlayerYaw = new LookAt(0f, TYPE_PLAYER_YAW);
    public static final LookAt PlayerPitch = new LookAt(0f, TYPE_PLAYER_PITCH);

    private final float yawPitch;
    private final int type;

    private LookAt(float yawPitch, int type) {
        this.yawPitch = yawPitch;
        this.type = type;
    }

    public static LookAt of(float value) {
        return new LookAt(value, TYPE_STATIC);
    }

    public float Value() {
        return switch (type) {
            case TYPE_PLAYER_YAW -> Minecraft.getInstance().player.getYRot();
            case TYPE_PLAYER_PITCH -> Minecraft.getInstance().player.getXRot();
            default -> yawPitch;
        };
    }
}
