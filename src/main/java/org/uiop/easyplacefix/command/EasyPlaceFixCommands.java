package org.uiop.easyplacefix.command;

import com.tick_ins.packet.Ping2Server;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.uiop.easyplacefix.data.LoosenModeData;
import org.uiop.easyplacefix.materials.MaterialExporter;
import org.uiop.easyplacefix.materials.MaterialSnapshot;
import org.uiop.easyplacefix.config.PlacementPreset;
import org.uiop.easyplacefix.util.PlacementDiagnostics;

import java.util.List;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument;
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
                        .then(literal("materials")
                                .executes(context -> materialsSummary(context.getSource()))
                                .then(literal("missing").executes(context -> materialsMissing(context.getSource())))
                                .then(literal("export")
                                        .executes(context -> materialsExport(context.getSource(), List.of(MaterialExporter.Format.XLSX)))
                                        .then(literal("xlsx").executes(context -> materialsExport(context.getSource(), List.of(MaterialExporter.Format.XLSX))))
                                        .then(literal("csv").executes(context -> materialsExport(context.getSource(), List.of(MaterialExporter.Format.CSV))))
                                        .then(literal("md").executes(context -> materialsExport(context.getSource(), List.of(MaterialExporter.Format.MARKDOWN))))
                                        .then(literal("json").executes(context -> materialsExport(context.getSource(), List.of(MaterialExporter.Format.JSON))))
                                        .then(literal("all").executes(context -> materialsExport(context.getSource(), List.of(MaterialExporter.Format.values()))))))
                        .then(literal("loosen")
                                .then(literal("add")
                                        .executes(context -> addLoosenItem(context.getSource(), heldItem(context.getSource())))
                                        .then(argument("item", ItemArgument.item(registryAccess))
                                                .executes(context -> addLoosenItem(context.getSource(),
                                                        ItemArgument.getItem(context, "item").item().value()))))
                                .then(literal("remove")
                                        .executes(context -> removeLoosenItem(context.getSource(), heldItem(context.getSource())))
                                        .then(argument("item", ItemArgument.item(registryAccess))
                                                .executes(context -> removeLoosenItem(context.getSource(),
                                                        ItemArgument.getItem(context, "item").item().value()))))
                                .then(literal("list").executes(context -> listLoosenItems(context.getSource())))
                                .then(literal("clear").executes(context -> clearLoosenItems(context.getSource())))
                                .then(literal("reload").executes(context -> reloadLoosenItems(context.getSource()))))
                )
        );
    }

    private static final int SUMMARY_TOP = 10;
    private static final int MISSING_LIMIT = 40;

    private static MaterialSnapshot requireMaterials(FabricClientCommandSource source) {
        MaterialSnapshot.Result result = MaterialSnapshot.capture(true);
        switch (result.status()) {
            case NO_LIST -> {
                source.sendError(Component.translatable("easyplacefix.materials.no_list"));
                return null;
            }
            case CREATED -> {
                source.sendFeedback(prefixed(Component.translatable("easyplacefix.materials.created"), ChatFormatting.YELLOW));
                return null;
            }
            default -> {
                return result.snapshot();
            }
        }
    }

    private static int materialsSummary(FabricClientCommandSource source) {
        MaterialSnapshot snapshot = requireMaterials(source);
        if (snapshot == null) {
            return 0;
        }
        source.sendFeedback(prefixed(Component.translatable("easyplacefix.materials.summary.header", snapshot.title()), ChatFormatting.GOLD));
        source.sendFeedback(prefixed(Component.translatable("easyplacefix.materials.summary.line",
                snapshot.rows().size(), snapshot.total(), snapshot.missing(), snapshot.stillNeeded(),
                snapshot.stacksNeeded(), (long) Math.ceil(snapshot.stacksNeeded() / (double) MaterialSnapshot.SHULKER_SLOTS)),
                ChatFormatting.GRAY));
        List<MaterialSnapshot.Row> needed = snapshot.neededRows();
        if (needed.isEmpty()) {
            source.sendFeedback(prefixed(Component.translatable("easyplacefix.materials.summary.complete"), ChatFormatting.GREEN));
        } else {
            for (MaterialSnapshot.Row row : needed.subList(0, Math.min(SUMMARY_TOP, needed.size()))) {
                source.sendFeedback(materialLine(row));
            }
        }
        source.sendFeedback(actionLinks());
        return 1;
    }

    private static int materialsMissing(FabricClientCommandSource source) {
        MaterialSnapshot snapshot = requireMaterials(source);
        if (snapshot == null) {
            return 0;
        }
        List<MaterialSnapshot.Row> needed = snapshot.neededRows();
        if (needed.isEmpty()) {
            source.sendFeedback(prefixed(Component.translatable("easyplacefix.materials.summary.complete"), ChatFormatting.GREEN));
            return 0;
        }
        for (MaterialSnapshot.Row row : needed.subList(0, Math.min(MISSING_LIMIT, needed.size()))) {
            source.sendFeedback(materialLine(row));
        }
        if (needed.size() > MISSING_LIMIT) {
            source.sendFeedback(prefixed(Component.translatable("easyplacefix.materials.missing.more", needed.size() - MISSING_LIMIT), ChatFormatting.GRAY));
        }
        source.sendFeedback(actionLinks());
        return needed.size();
    }

    private static int materialsExport(FabricClientCommandSource source, List<MaterialExporter.Format> formats) {
        MaterialSnapshot snapshot = requireMaterials(source);
        if (snapshot == null) {
            return 0;
        }
        source.sendFeedback(prefixed(Component.translatable("easyplacefix.materials.export.started", snapshot.rows().size()), ChatFormatting.GRAY));
        MaterialExporter.export(snapshot, formats, source::sendFeedback);
        return 1;
    }

    private static Component materialLine(MaterialSnapshot.Row row) {
        int needed = row.stillNeeded();
        return Component.literal("  ").append(row.stack().getHoverName().copy().withStyle(ChatFormatting.WHITE))
                .append(Component.translatable("easyplacefix.materials.line", needed, row.stacks(needed),
                        String.format(java.util.Locale.ROOT, "%.1f", row.shulkerBoxes(needed)), row.available())
                        .withStyle(ChatFormatting.GRAY));
    }

    private static Component actionLinks() {
        return Component.literal("  ")
                .append(link("easyplacefix.materials.link.xlsx", "/easyplacefix materials export xlsx"))
                .append(Component.literal(" "))
                .append(link("easyplacefix.materials.link.csv", "/easyplacefix materials export csv"))
                .append(Component.literal(" "))
                .append(link("easyplacefix.materials.link.all", "/easyplacefix materials export all"))
                .append(Component.literal(" "))
                .append(link("easyplacefix.materials.link.missing", "/easyplacefix materials missing"));
    }

    private static Component link(String key, String command) {
        return Component.literal("[").append(Component.translatable(key)).append("]")
                .withStyle(style -> style.withColor(ChatFormatting.AQUA)
                        .withClickEvent(new ClickEvent.RunCommand(command))
                        .withHoverEvent(new HoverEvent.ShowText(Component.literal(command))));
    }

    private static Item heldItem(FabricClientCommandSource source) {
        ItemStack stack = source.getPlayer().getMainHandItem();
        return stack.isEmpty() ? null : stack.getItem();
    }

    private static int addLoosenItem(FabricClientCommandSource source, Item item) {
        if (item == null || item == Items.AIR) {
            source.sendError(Component.translatable("easyplacefix.loosen.empty_hand"));
            return 0;
        }
        if (!LoosenModeData.add(item)) {
            source.sendFeedback(prefixed(Component.translatable("easyplacefix.loosen.already", itemName(item)), ChatFormatting.GRAY));
            return 0;
        }
        source.sendFeedback(prefixed(Component.translatable("easyplacefix.loosen.added", itemName(item)), ChatFormatting.GREEN));
        return 1;
    }

    private static int removeLoosenItem(FabricClientCommandSource source, Item item) {
        if (item == null || item == Items.AIR) {
            source.sendError(Component.translatable("easyplacefix.loosen.empty_hand"));
            return 0;
        }
        if (!LoosenModeData.remove(item)) {
            source.sendFeedback(prefixed(Component.translatable("easyplacefix.loosen.not_listed", itemName(item)), ChatFormatting.GRAY));
            return 0;
        }
        source.sendFeedback(prefixed(Component.translatable("easyplacefix.loosen.removed", itemName(item)), ChatFormatting.GREEN));
        return 1;
    }

    private static int listLoosenItems(FabricClientCommandSource source) {
        List<Item> listed = LoosenModeData.snapshot();
        if (listed.isEmpty()) {
            source.sendFeedback(prefixed(Component.translatable("easyplacefix.loosen.list_empty"), ChatFormatting.GRAY));
            return 0;
        }
        MutableComponent names = Component.empty();
        for (int i = 0; i < listed.size(); i++) {
            if (i > 0) {
                names.append(Component.literal(", ").withStyle(ChatFormatting.DARK_GRAY));
            }
            names.append(itemName(listed.get(i)));
        }
        source.sendFeedback(prefixed(Component.translatable("easyplacefix.loosen.list", listed.size(), names), ChatFormatting.GRAY));
        return listed.size();
    }

    private static int clearLoosenItems(FabricClientCommandSource source) {
        int removed = LoosenModeData.clear();
        source.sendFeedback(prefixed(Component.translatable("easyplacefix.loosen.cleared", removed), ChatFormatting.GREEN));
        return removed;
    }

    private static int reloadLoosenItems(FabricClientCommandSource source) {
        LoosenModeData.reload();
        int size = LoosenModeData.items.size();
        source.sendFeedback(prefixed(Component.translatable("easyplacefix.loosen.reloaded", size), ChatFormatting.GREEN));
        return size;
    }

    private static Component itemName(Item item) {
        return item.getDefaultInstance().getHoverName().copy().withStyle(ChatFormatting.WHITE);
    }

    private static Component prefixed(Component body, ChatFormatting color) {
        return Component.literal("[EasyPlaceFix] ").withStyle(ChatFormatting.GOLD)
                .append(body.copy().withStyle(color));
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
