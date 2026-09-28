package org.uiop.easyplacefix.materials;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import static org.uiop.easyplacefix.EasyPlaceFix.LOGGER;

public final class MaterialExporter {
    private static final DateTimeFormatter FILE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    private static final DateTimeFormatter DISPLAY_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public enum Format {
        XLSX("xlsx"),
        CSV("csv"),
        MARKDOWN("md"),
        JSON("json");

        private final String extension;

        Format(String extension) {
            this.extension = extension;
        }

        public String extension() {
            return this.extension;
        }

        public static Format byName(String name) {
            for (Format format : values()) {
                if (format.extension.equalsIgnoreCase(name) || format.name().equalsIgnoreCase(name)) {
                    return format;
                }
            }
            return null;
        }
    }

    private MaterialExporter() {
    }

    public static Path exportDirectory() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("easyplacefix").resolve("materials");
    }

    public static void export(MaterialSnapshot snapshot, List<Format> formats, Consumer<Component> feedback) {
        Labels labels = Labels.capture();
        LocalDateTime now = LocalDateTime.now();
        String baseName = sanitizeFileName(snapshot.title()) + "_" + FILE_TIME.format(now);
        Path directory = exportDirectory();

        Util.ioPool().execute(() -> {
            for (Format format : formats) {
                Path target = directory.resolve(baseName + "." + format.extension());
                try {
                    Files.createDirectories(directory);
                    switch (format) {
                        case XLSX -> writeXlsx(snapshot, labels, now, target);
                        case CSV -> Files.writeString(target, csv(snapshot, labels), StandardCharsets.UTF_8);
                        case MARKDOWN -> Files.writeString(target, markdown(snapshot, labels, now), StandardCharsets.UTF_8);
                        case JSON -> Files.writeString(target, json(snapshot, now), StandardCharsets.UTF_8);
                    }
                    Minecraft.getInstance().execute(() -> feedback.accept(savedMessage(target)));
                } catch (IOException | RuntimeException error) {
                    LOGGER.warn("Failed to export material list to {}", target, error);
                    Minecraft.getInstance().execute(() -> feedback.accept(
                            Component.translatable("easyplacefix.materials.export.failed", target.getFileName().toString(),
                                    String.valueOf(error.getMessage())).withStyle(ChatFormatting.RED)));
                }
            }
        });
    }

    private static Component savedMessage(Path target) {
        MutableComponent link = Component.literal(target.getFileName().toString())
                .withStyle(style -> style.withColor(ChatFormatting.AQUA).withUnderlined(true)
                        .withClickEvent(new ClickEvent.OpenFile(target.toAbsolutePath()))
                        .withHoverEvent(new HoverEvent.ShowText(Component.literal(target.toAbsolutePath().toString()))));
        MutableComponent folder = Component.translatable("easyplacefix.materials.export.open_folder")
                .withStyle(style -> style.withColor(ChatFormatting.GRAY).withUnderlined(true)
                        .withClickEvent(new ClickEvent.OpenFile(target.getParent().toAbsolutePath())));
        return Component.translatable("easyplacefix.materials.export.saved", link)
                .withStyle(ChatFormatting.GREEN)
                .append(Component.literal(" "))
                .append(folder);
    }

    private static void writeXlsx(MaterialSnapshot snapshot, Labels labels, LocalDateTime now, Path target)
            throws IOException {
        XlsxWriter workbook = new XlsxWriter();

        XlsxWriter.Sheet summary = workbook.addSheet(labels.sheetSummary);
        summary.columnWidths(34, 22)
                .title(labels.summaryTitle)
                .blank()
                .row(labels.schematic, snapshot.title())
                .row(labels.exportedAt, DISPLAY_TIME.format(now))
                .row(labels.itemTypes, snapshot.rows().size())
                .row(labels.totalItems, snapshot.total())
                .row(labels.missingItems, snapshot.missing())
                .row(labels.stillNeededItems, snapshot.stillNeeded())
                .row(labels.stacksNeeded, snapshot.stacksNeeded())
                .row(labels.shulkersNeeded, (long) Math.ceil(snapshot.stacksNeeded() / (double) MaterialSnapshot.SHULKER_SLOTS));

        fillTable(workbook.addSheet(labels.sheetNeeded), snapshot.neededRows(), labels);
        fillTable(workbook.addSheet(labels.sheetAll), snapshot.rows(), labels);
        workbook.write(target);
    }

    private static void fillTable(XlsxWriter.Sheet sheet, List<MaterialSnapshot.Row> rows, Labels labels) {
        sheet.columnWidths(32, 34, 11, 11, 12, 12, 13, 11, 13, 11)
                .header(labels.columns);
        for (MaterialSnapshot.Row row : rows) {
            int needed = row.stillNeeded();
            sheet.row(row.name(), row.id(), row.total(), row.missing(), row.mismatched(), row.available(), needed,
                    row.stacks(needed), round2(row.shulkerBoxes(needed)), row.maxStackSize());
        }
    }

    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private static String csv(MaterialSnapshot snapshot, Labels labels) {
        StringBuilder out = new StringBuilder("﻿");
        appendCsvLine(out, List.of(labels.columns));
        for (MaterialSnapshot.Row row : snapshot.rows()) {
            int needed = row.stillNeeded();
            appendCsvLine(out, List.of(row.name(), row.id(), String.valueOf(row.total()), String.valueOf(row.missing()),
                    String.valueOf(row.mismatched()), String.valueOf(row.available()), String.valueOf(needed),
                    String.valueOf(row.stacks(needed)), String.format(Locale.ROOT, "%.2f", row.shulkerBoxes(needed)),
                    String.valueOf(row.maxStackSize())));
        }
        return out.toString();
    }

    private static void appendCsvLine(StringBuilder out, List<String> values) {
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                out.append(',');
            }
            String value = values.get(i);
            if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
                out.append('"').append(value.replace("\"", "\"\"")).append('"');
            } else {
                out.append(value);
            }
        }
        out.append("\r\n");
    }

    private static String markdown(MaterialSnapshot snapshot, Labels labels, LocalDateTime now) {
        StringBuilder out = new StringBuilder();
        out.append("# ").append(snapshot.title()).append("\n\n");
        out.append("- ").append(labels.exportedAt).append(": ").append(DISPLAY_TIME.format(now)).append('\n');
        out.append("- ").append(labels.totalItems).append(": ").append(snapshot.total()).append('\n');
        out.append("- ").append(labels.stillNeededItems).append(": ").append(snapshot.stillNeeded()).append('\n');
        out.append("- ").append(labels.stacksNeeded).append(": ").append(snapshot.stacksNeeded()).append("\n\n");
        out.append('|');
        for (String column : labels.columns) {
            out.append(' ').append(column).append(" |");
        }
        out.append("\n|");
        for (int i = 0; i < labels.columns.length; i++) {
            out.append(i < 2 ? " --- |" : " ---: |");
        }
        out.append('\n');
        for (MaterialSnapshot.Row row : snapshot.rows()) {
            int needed = row.stillNeeded();
            out.append("| ").append(row.name().replace("|", "\\|")).append(" | `").append(row.id()).append("` | ")
                    .append(row.total()).append(" | ").append(row.missing()).append(" | ").append(row.mismatched())
                    .append(" | ").append(row.available()).append(" | ").append(needed).append(" | ")
                    .append(row.stacks(needed)).append(" | ")
                    .append(String.format(Locale.ROOT, "%.2f", row.shulkerBoxes(needed))).append(" | ")
                    .append(row.maxStackSize()).append(" |\n");
        }
        return out.toString();
    }

    private static String json(MaterialSnapshot snapshot, LocalDateTime now) {
        JsonObject root = new JsonObject();
        root.addProperty("schematic", snapshot.title());
        root.addProperty("exportedAt", DISPLAY_TIME.format(now));
        root.addProperty("total", snapshot.total());
        root.addProperty("missing", snapshot.missing());
        root.addProperty("stillNeeded", snapshot.stillNeeded());
        root.addProperty("stacksNeeded", snapshot.stacksNeeded());
        JsonArray items = new JsonArray();
        for (MaterialSnapshot.Row row : snapshot.rows()) {
            int needed = row.stillNeeded();
            JsonObject item = new JsonObject();
            item.addProperty("id", row.id());
            item.addProperty("name", row.name());
            item.addProperty("total", row.total());
            item.addProperty("missing", row.missing());
            item.addProperty("mismatched", row.mismatched());
            item.addProperty("available", row.available());
            item.addProperty("stillNeeded", needed);
            item.addProperty("stacks", row.stacks(needed));
            item.addProperty("shulkerBoxes", round2(row.shulkerBoxes(needed)));
            item.addProperty("stackSize", row.maxStackSize());
            items.add(item);
        }
        root.add("items", items);
        return new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(root);
    }

    static String sanitizeFileName(String name) {
        String cleaned = name == null ? "" : name.replaceAll("[\\\\/:*?\"<>|\\p{Cntrl}]", "_").trim();
        if (cleaned.isEmpty()) {
            cleaned = "materials";
        }
        return cleaned.length() > 64 ? cleaned.substring(0, 64) : cleaned;
    }

    private record Labels(String sheetSummary, String sheetNeeded, String sheetAll, String summaryTitle,
                          String schematic, String exportedAt, String itemTypes, String totalItems,
                          String missingItems, String stillNeededItems, String stacksNeeded, String shulkersNeeded,
                          String[] columns) {
        private static Labels capture() {
            return new Labels(
                    tr("easyplacefix.materials.sheet.summary"),
                    tr("easyplacefix.materials.sheet.needed"),
                    tr("easyplacefix.materials.sheet.all"),
                    tr("easyplacefix.materials.summary.title"),
                    tr("easyplacefix.materials.summary.schematic"),
                    tr("easyplacefix.materials.summary.exported_at"),
                    tr("easyplacefix.materials.summary.item_types"),
                    tr("easyplacefix.materials.summary.total"),
                    tr("easyplacefix.materials.summary.missing"),
                    tr("easyplacefix.materials.summary.still_needed"),
                    tr("easyplacefix.materials.summary.stacks"),
                    tr("easyplacefix.materials.summary.shulkers"),
                    new String[]{
                            tr("easyplacefix.materials.column.item"),
                            tr("easyplacefix.materials.column.id"),
                            tr("easyplacefix.materials.column.total"),
                            tr("easyplacefix.materials.column.missing"),
                            tr("easyplacefix.materials.column.mismatched"),
                            tr("easyplacefix.materials.column.available"),
                            tr("easyplacefix.materials.column.still_needed"),
                            tr("easyplacefix.materials.column.stacks"),
                            tr("easyplacefix.materials.column.shulkers"),
                            tr("easyplacefix.materials.column.stack_size")
                    });
        }

        private static String tr(String key) {
            return Component.translatable(key).getString();
        }
    }
}
