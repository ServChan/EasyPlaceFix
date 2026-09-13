package org.uiop.easyplacefix.data;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

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
            try {
                return loadFrom(CONFIG_PATH);
            } catch (IOException | JsonSyntaxException e) {
                Path backup = CONFIG_PATH.resolveSibling("loosenMode.json.bak");
                if (Files.exists(backup)) {
                    try {
                        return loadFrom(backup);
                    } catch (IOException | JsonSyntaxException ignored) {
                    }
                }
            }
        } else {
            saveToFile(new HashSet<>());
            // Create file on first load
        }
        return new HashSet<>();
    }

    private static HashSet<ItemStack> loadFrom(Path path) throws IOException, JsonSyntaxException {
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            HashSet<Integer> itemIds = GSON.fromJson(reader, ITEM_SET_TYPE);
            items.clear();
            if (itemIds == null) return new HashSet<>();
            return itemIds.stream()
                    .map(id -> {
                        Item item = Item.byRawId(id);
                        if (item == null) return null;
                        items.add(item);
                        return item.getDefaultStack();
                    })
                    .filter(stack -> stack != null && !stack.isEmpty())
                    .collect(Collectors.toCollection(HashSet::new));
        }
    }

    public static void saveToFile(Collection<ItemStack> itemHashSet) {
        items.clear();
        HashSet<Integer> itemIds = itemHashSet.stream()
                .map(itemStack -> {
                    Item item = itemStack.getItem();
                    items.add(item);
                    return Item.getRawId(item);
                })
                .collect(Collectors.toCollection(HashSet::new));

        Path temp = CONFIG_PATH.resolveSibling("loosenMode.json.tmp");
        Path backup = CONFIG_PATH.resolveSibling("loosenMode.json.bak");
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(temp, GSON.toJson(itemIds), StandardCharsets.UTF_8);
            if (Files.exists(CONFIG_PATH)) {
                Files.copy(CONFIG_PATH, backup, StandardCopyOption.REPLACE_EXISTING);
            }
            try {
                Files.move(temp, CONFIG_PATH, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temp, CONFIG_PATH, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            try {
                Files.deleteIfExists(temp);
            } catch (IOException ignored) {
            }
        }
    }

}
