package org.uiop.easyplacefix.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NoteBlockHelperTest {

    @ParameterizedTest(name = "current={0}, target={1} => clicks={2}")
    @CsvSource({
            "0, 0, 0",
            "0, 1, 1",
            "0, 12, 12",
            "0, 24, 24",
            "20, 3, 8",
            "24, 0, 1",
            "12, 12, 0",
            "1, 0, 24",
            "24, 23, 24",
            "15, 16, 1",
            "16, 15, 24"
    })
    @DisplayName("Verify cyclic note click calculation formula")
    void testCalculateClicks(int currentNote, int targetNote, int expectedClicks) {
        assertEquals(expectedClicks, NoteBlockHelper.calculateClicks(currentNote, targetNote));
    }

    @Test
    @DisplayName("Verify all note combinations stay within 0..24 range and cycle properly")
    void testAllCombinations() {
        for (int current = 0; current <= 24; current++) {
            for (int target = 0; target <= 24; target++) {
                int clicks = NoteBlockHelper.calculateClicks(current, target);
                assertEquals((current + clicks) % 25, target,
                        String.format("Cycling from %d with %d clicks should reach %d", current, clicks, target));
            }
        }
    }
}
