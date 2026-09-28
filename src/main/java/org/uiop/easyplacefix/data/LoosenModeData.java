package org.uiop.easyplacefix.data;

import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.uiop.easyplacefix.EasyPlaceFix.LOGGER;

public final class LoosenModeData {

    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("loosenMode.json");
    private static final Path BACKUP_PATH = CONFIG_PATH.resolveSibling("loosenMode.json.bak");
    private static final Path TEMP_PATH = CONFIG_PATH.resolveSibling("loosenMode.json.tmp");

    private static final Object WRITE_LOCK = new Object();
    private static final AtomicReference<String> PENDING_JSON = new AtomicReference<>();

    public static final Set<Item> items = new LinkedHashSet<>();

    private LoosenModeData() {
    }

    public static synchronized void reload() {
        items.clear();
        if (!Files.exists(CONFIG_PATH)) {
            save();
            return;
        }
        try {
            if (loadFrom(CONFIG_PATH)) {
                save();
            }
        } catch (IOException | JsonParseException | IllegalStateException error) {
            LOGGER.warn("Failed to load loosen mode config file {}", CONFIG_PATH, error);
            items.clear();
            if (Files.exists(BACKUP_PATH)) {
                try {
                    loadFrom(BACKUP_PATH);
                    LOGGER.info("Recovered loosen mode config from backup {}", BACKUP_PATH);
                } catch (IOException | JsonParseException | IllegalStateException backupError) {
                    items.clear();
                    LOGGER.warn("Failed to load loosen mode config backup {}", BACKUP_PATH, backupError);
                }
            }
        }
    }

    public static synchronized boolean add(Item item) {
        if (item == null || item == Items.AIR || !items.add(item)) {
            return false;
        }
        save();
        return true;
    }

    public static synchronized boolean remove(Item item) {
        if (!items.remove(item)) {
            return false;
        }
        save();
        return true;
    }

    public static synchronized int clear() {
        int removed = items.size();
        items.clear();
        save();
        return removed;
    }

    public static synchronized List<Item> snapshot() {
        List<Item> sorted = new ArrayList<>(items);
        sorted.sort(Comparator.comparing(item -> BuiltInRegistries.ITEM.getKey(item).toString()));
        return sorted;
    }

    public static synchronized void saveToFile(Collection<Item> newItems) {
        items.clear();
        for (Item item : newItems) {
            if (item != null && item != Items.AIR) {
                items.add(item);
            }
        }
        save();
    }

    private static boolean loadFrom(Path path) throws IOException {
        LoosenListFormat.Parsed parsed = LoosenListFormat.parse(Files.readString(path, StandardCharsets.UTF_8));
        for (String rawId : parsed.ids()) {
            Identifier id = Identifier.tryParse(rawId);
            Item item = id == null ? null : BuiltInRegistries.ITEM.getOptional(id).orElse(null);
            if (item == null) {
                LOGGER.warn("Unknown item '{}' in {}", rawId, path);
            } else if (item != Items.AIR) {
                items.add(item);
            }
        }
        for (int legacyId : parsed.legacyIds()) {
            Item item = Item.byId(legacyId);
            if (item != null && item != Items.AIR) {
                items.add(item);
            }
        }
        return parsed.hasLegacyEntries();
    }

    private static void save() {
        List<String> ids = new ArrayList<>();
        for (Item item : snapshot()) {
            ids.add(BuiltInRegistries.ITEM.getKey(item).toString());
        }
        PENDING_JSON.set(LoosenListFormat.serialize(ids));
        Util.ioPool().execute(LoosenModeData::flush);
    }

    private static void flush() {
        synchronized (WRITE_LOCK) {
            String json = PENDING_JSON.getAndSet(null);
            if (json != null) {
                write(json);
            }
        }
    }

    private static void write(String json) {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(TEMP_PATH, json, StandardCharsets.UTF_8);
            if (Files.exists(CONFIG_PATH)) {
                try {
                    Files.copy(CONFIG_PATH, BACKUP_PATH, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException backupError) {
                    LOGGER.debug("Failed to refresh loosen mode config backup {}", BACKUP_PATH, backupError);
                }
            }
            try {
                Files.move(TEMP_PATH, CONFIG_PATH, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(TEMP_PATH, CONFIG_PATH, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException error) {
            LOGGER.warn("Failed to save loosen mode config file {}", CONFIG_PATH, error);
            try {
                Files.deleteIfExists(TEMP_PATH);
            } catch (IOException cleanupError) {
                LOGGER.debug("Failed to remove temporary loosen mode config {}", TEMP_PATH, cleanupError);
            }
        }
    }
}
