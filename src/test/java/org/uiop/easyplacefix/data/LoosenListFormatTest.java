package org.uiop.easyplacefix.data;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoosenListFormatTest {

    @Test
    void parsesItemIdsAndNormalizesNamespace() {
        LoosenListFormat.Parsed parsed = LoosenListFormat.parse("[\"minecraft:stone\", \" oak_planks \", \"mod:thing\", \"\"]");
        assertEquals(List.of("minecraft:stone", "minecraft:oak_planks", "mod:thing"), parsed.ids());
        assertFalse(parsed.hasLegacyEntries());
    }

    @Test
    void keepsLegacyNumericEntriesForMigration() {
        LoosenListFormat.Parsed parsed = LoosenListFormat.parse("[1, \"minecraft:dirt\", 42, {\"x\": 1}]");
        assertEquals(List.of("minecraft:dirt"), parsed.ids());
        assertEquals(List.of(1, 42), parsed.legacyIds());
        assertTrue(parsed.hasLegacyEntries());
    }

    @Test
    void nullDocumentIsEmpty() {
        LoosenListFormat.Parsed parsed = LoosenListFormat.parse("null");
        assertTrue(parsed.ids().isEmpty());
        assertTrue(parsed.legacyIds().isEmpty());
    }

    @Test
    void serializationIsSortedAndRoundTrips() {
        String json = LoosenListFormat.serialize(List.of("minecraft:stone", "minecraft:andesite", "minecraft:stone"));
        assertEquals(List.of("minecraft:andesite", "minecraft:stone"), LoosenListFormat.parse(json).ids());
    }
}
