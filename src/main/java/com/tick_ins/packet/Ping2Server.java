package com.tick_ins.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;

public final class Ping2Server {
    private Ping2Server() {
    }

    public static int getRtt() {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.player == null || client.getConnection() == null) {
            return 150;
        }

        PlayerInfo entry = client.getConnection().getPlayerInfo(client.player.getUUID());
        if (entry == null) {
            return 150;
        }

        int latency = entry.getLatency();
        return Math.max(latency, 50);
    }
}
