package org.uiop.easyplacefix.data;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

import static org.uiop.easyplacefix.EasyPlaceFix.LOGGER;

public class LoosenModeData {
//    static HashSet<Item> itemHashSet = new HashSet<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("loosenMode.json");
    private static final File CONFIG_FILE = CONFIG_PATH.toFile();
    private static final Type ITEM_SET_TYPE = new TypeToken<HashSet<Integer>>() {}.getType();
    public static HashSet<Item> items = new HashSet<>();
    static {
        loadFromFile();
    }

    public static HashSet<ItemStack> loadFromFile() {
        if (CONFIG_FILE.exists()) {
            try (Reader reader = new FileReader(CONFIG_FILE)) {
                HashSet<Integer> itemIds = GSON.fromJson(reader, ITEM_SET_TYPE);
                items.clear();
                if (itemIds == null) {
                    return new HashSet<>();
                }
                HashSet<ItemStack> itemStackHashSet = itemIds.stream()
                        .map(id -> {
                            Item item = Item.byId(id);
                            if (item == null) {
                                return null;
                            }
                            items.add(item);
                            return item.getDefaultInstance();
                        })
                        .filter(itemStack -> itemStack != null && !itemStack.isEmpty())
                        .collect(Collectors.toCollection(HashSet::new));
                return itemStackHashSet;
            } catch (IOException | JsonSyntaxException e) {
                LOGGER.warn("Failed to load loosen mode config file {}", CONFIG_FILE, e);
            }
        } else {
            saveToFile(new HashSet<>());
            // Create file on first load
        }
        return new HashSet<>();
    }

    public static void saveToFile(Collection<ItemStack> itemHashSet) {
        items.clear();
        HashSet<Integer> itemIds = itemHashSet.stream()
                .map(itemStack -> {
                    Item item = itemStack.getItem();
                    items.add(item);
                    return Item.getId(item);
                })
                .collect(Collectors.toCollection(HashSet::new));

        Path temp = CONFIG_PATH.resolveSibling("loosenMode.json.tmp");
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(temp, GSON.toJson(itemIds), StandardCharsets.UTF_8);
            try {
                Files.move(temp, CONFIG_PATH, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temp, CONFIG_PATH, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            LOGGER.warn("Failed to save loosen mode config file {}", CONFIG_FILE, e);
            try { Files.deleteIfExists(temp); } catch (IOException ignored) {}
        }
    }

}
