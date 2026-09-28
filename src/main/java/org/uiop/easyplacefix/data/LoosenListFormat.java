package org.uiop.easyplacefix.data;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

public final class LoosenListFormat {
    private LoosenListFormat() {
    }

    public record Parsed(List<String> ids, List<Integer> legacyIds) {
        public boolean hasLegacyEntries() {
            return !this.legacyIds.isEmpty();
        }
    }

    public static Parsed parse(String json) {
        JsonElement root = JsonParser.parseString(json);
        List<String> ids = new ArrayList<>();
        List<Integer> legacyIds = new ArrayList<>();
        if (root == null || root.isJsonNull()) {
            return new Parsed(ids, legacyIds);
        }
        for (JsonElement element : root.getAsJsonArray()) {
            if (!element.isJsonPrimitive()) {
                continue;
            }
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isNumber()) {
                legacyIds.add(primitive.getAsInt());
            } else if (primitive.isString()) {
                String id = primitive.getAsString().trim();
                if (!id.isEmpty()) {
                    ids.add(id.contains(":") ? id : "minecraft:" + id);
                }
            }
        }
        return new Parsed(ids, legacyIds);
    }

    public static String serialize(Collection<String> ids) {
        JsonArray array = new JsonArray();
        for (String id : new TreeSet<>(ids)) {
            array.add(id);
        }
        return new GsonBuilder().setPrettyPrinting().create().toJson(array);
    }
}
