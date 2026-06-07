package org.uiop.easyplacefix.until;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.uiop.easyplacefix.config.PlacementPreset;

import static org.uiop.easyplacefix.config.easyPlacefixConfig.DIAGNOSTIC_STATUS;
import static org.uiop.easyplacefix.config.easyPlacefixConfig.PLACEMENT_PRESET;
import static org.uiop.easyplacefix.config.easyPlacefixConfig.getEffectivePlacementDelayTicks;

public final class PlacementDiagnostics {
    private static final long MESSAGE_INTERVAL_MS = 650L;
    private static long lastMessageTime;
    private static String lastMessageKey = "";
    private static DiagnosticEvent lastEvent = DiagnosticEvent.empty();

    private PlacementDiagnostics() {
    }

    public static void report(String translationKey, Object... args) {
        record(translationKey, args);

        if (!DIAGNOSTIC_STATUS.getBooleanValue()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }

        long now = System.currentTimeMillis();
        if (translationKey.equals(lastMessageKey) && now - lastMessageTime < MESSAGE_INTERVAL_MS) {
            return;
        }

        lastMessageKey = translationKey;
        lastMessageTime = now;

        Component message = Component.literal("EasyPlaceFix ")
                .withStyle(ChatFormatting.GOLD)
                .append(Component.literal(" | ").withStyle(ChatFormatting.DARK_GRAY))
                .append(Component.translatable(translationKey, args).withStyle(ChatFormatting.AQUA))
                .append(Component.literal(" | ").withStyle(ChatFormatting.DARK_GRAY))
                .append(Component.translatable(
                                "easyplacefix.diagnostic.context",
                                getPresetName(),
                                getEffectivePlacementDelayTicks(),
                                com.tick_ins.packet.Ping2Server.getRtt()
                        )
                        .withStyle(ChatFormatting.GRAY));
        mc.player.sendOverlayMessage(message);
    }

    public static DiagnosticEvent getLastEvent() {
        return lastEvent;
    }

    public static Component getLastEventMessage() {
        DiagnosticEvent event = getLastEvent();
        if (event.isEmpty()) {
            return Component.translatable("easyplacefix.report.last.none").withStyle(ChatFormatting.GRAY);
        }

        return Component.translatable(
                "easyplacefix.report.last.entry",
                Component.translatable(event.translationKey(), event.args()),
                event.ageSeconds()
        ).withStyle(ChatFormatting.GRAY);
    }

    private static void record(String translationKey, Object... args) {
        lastEvent = new DiagnosticEvent(translationKey, args, System.currentTimeMillis());
    }

    private static String getPresetName() {
        return ((PlacementPreset) PLACEMENT_PRESET.getOptionListValue()).getDisplayName();
    }

    public record DiagnosticEvent(String translationKey, Object[] args, long timestampMs) {
        private static DiagnosticEvent empty() {
            return new DiagnosticEvent("", new Object[0], 0L);
        }

        public boolean isEmpty() {
            return this.translationKey.isEmpty();
        }

        public long ageSeconds() {
            if (this.isEmpty()) {
                return 0L;
            }
            return Math.max(0L, (System.currentTimeMillis() - this.timestampMs) / 1000L);
        }
    }
}
