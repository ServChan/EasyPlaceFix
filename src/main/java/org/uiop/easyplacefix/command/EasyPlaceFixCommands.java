package org.uiop.easyplacefix.command;

import com.tick_ins.packet.Ping2Server;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import org.uiop.easyplacefix.config.PlacementPreset;
import org.uiop.easyplacefix.until.PlacementDiagnostics;

import java.util.List;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;
import static org.uiop.easyplacefix.config.easyPlacefixConfig.*;

public final class EasyPlaceFixCommands {
    private EasyPlaceFixCommands() {
    }

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(literal("easyplacefix")
                        .then(literal("report").executes(context -> sendReport(context.getSource(), false)))
                        .then(literal("copy-report").executes(context -> sendReport(context.getSource(), true)))
                        .then(literal("last").executes(context -> sendLastDiagnostic(context.getSource())))
                )
        );
    }

    private static int sendReport(FabricClientCommandSource source, boolean copyToClipboard) {
        List<Component> lines = buildReportLines(source.getClient());
        for (Component line : lines) {
            source.sendFeedback(line);
        }

        if (copyToClipboard) {
            source.getClient().keyboardHandler.setClipboard(buildPlainReport(source.getClient()));
            source.sendFeedback(Component.translatable("easyplacefix.report.copied").withStyle(ChatFormatting.GREEN));
        }

        return 1;
    }

    private static int sendLastDiagnostic(FabricClientCommandSource source) {
        source.sendFeedback(Component.literal("[EasyPlaceFix] ").withStyle(ChatFormatting.GOLD)
                .append(PlacementDiagnostics.getLastEventMessage()));
        return 1;
    }

    private static List<Component> buildReportLines(Minecraft mc) {
        String server = getServerName(mc);
        int vanillaLatency = getVanillaLatency(mc);
        return List.of(
                Component.literal("==== EasyPlaceFix compatibility report ====").withStyle(ChatFormatting.GOLD),
                entry("easyplacefix.report.mod", getModVersion("easyplacefix")),
                entry("easyplacefix.report.minecraft", mc.getLaunchedVersion()),
                entry("easyplacefix.report.java", System.getProperty("java.version")),
                entry("easyplacefix.report.fabric_loader", FabricLoader.getInstance().getModContainer("fabricloader")
                        .map(container -> container.getMetadata().getVersion().getFriendlyString())
                        .orElse("unknown")),
                entry("easyplacefix.report.fabric_api", getModVersion("fabric-api")),
                entry("easyplacefix.report.litematica", getModVersion("litematica")),
                entry("easyplacefix.report.malilib", getModVersion("malilib")),
                entry("easyplacefix.report.server", server),
                entry("easyplacefix.report.ping", formatPing(vanillaLatency, Ping2Server.getRtt())),
                entry("easyplacefix.report.enabled", ENABLE_FIX.getBooleanValue()),
                entry("easyplacefix.report.preset", ((PlacementPreset) PLACEMENT_PRESET.getOptionListValue()).getDisplayName()),
                entry("easyplacefix.report.delay", getEffectivePlacementDelayTicks()),
                entry("easyplacefix.report.diagnostics", DIAGNOSTIC_STATUS.getBooleanValue()),
                Component.literal("[EasyPlaceFix] ").withStyle(ChatFormatting.GOLD)
                        .append(PlacementDiagnostics.getLastEventMessage())
        );
    }

    private static String buildPlainReport(Minecraft mc) {
        int vanillaLatency = getVanillaLatency(mc);
        PlacementDiagnostics.DiagnosticEvent last = PlacementDiagnostics.getLastEvent();
        return String.join(System.lineSeparator(),
                "==== EasyPlaceFix compatibility report ====",
                "EasyPlaceFix: " + getModVersion("easyplacefix"),
                "Minecraft: " + mc.getLaunchedVersion(),
                "Java: " + System.getProperty("java.version"),
                "Fabric Loader: " + getModVersion("fabricloader"),
                "Fabric API: " + getModVersion("fabric-api"),
                "Litematica: " + getModVersion("litematica"),
                "MaLiLib: " + getModVersion("malilib"),
                "Server: " + getServerName(mc),
                "Ping: " + formatPing(vanillaLatency, Ping2Server.getRtt()),
                "Enable Fix: " + ENABLE_FIX.getBooleanValue(),
                "Placement Preset: " + ((PlacementPreset) PLACEMENT_PRESET.getOptionListValue()).getDisplayName(),
                "Effective Delay: " + getEffectivePlacementDelayTicks() + " ticks",
                "Diagnostics: " + DIAGNOSTIC_STATUS.getBooleanValue(),
                "Last diagnostic: " + (last.isEmpty() ? "none" : last.translationKey() + " (" + last.ageSeconds() + "s ago)")
        );
    }

    private static Component entry(String key, Object value) {
        return Component.literal("[EasyPlaceFix] ").withStyle(ChatFormatting.GOLD)
                .append(Component.translatable(key).withStyle(ChatFormatting.YELLOW))
                .append(Component.literal(": ").withStyle(ChatFormatting.DARK_GRAY))
                .append(Component.literal(String.valueOf(value)).withStyle(ChatFormatting.GRAY));
    }

    private static String getModVersion(String modId) {
        return FabricLoader.getInstance().getModContainer(modId)
                .map(container -> container.getMetadata().getVersion().getFriendlyString())
                .orElse("not loaded");
    }

    private static String getServerName(Minecraft mc) {
        if (mc.hasSingleplayerServer()) {
            return "Singleplayer";
        }

        ServerData serverData = mc.getCurrentServer();
        if (serverData != null) {
            return serverData.ip;
        }

        return "Not connected";
    }

    private static int getVanillaLatency(Minecraft mc) {
        if (mc.player == null || mc.getConnection() == null) {
            return -1;
        }

        PlayerInfo info = mc.getConnection().getPlayerInfo(mc.player.getUUID());
        return info == null ? -1 : info.getLatency();
    }

    private static String formatPing(int vanillaLatency, long measuredRtt) {
        String vanilla = vanillaLatency < 0 ? "unknown" : vanillaLatency + " ms";
        return vanilla + " vanilla / " + measuredRtt + " ms measured";
    }
}
