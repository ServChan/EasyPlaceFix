package org.uiop.easyplacefix.until;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NoteBlockHelperTest {
    @Test
    void clickCountWrapsAcrossTheTwentyFiveNotes() {
        assertEquals(0, NoteBlockHelper.calculateClicks(0, 0));
        assertEquals(1, NoteBlockHelper.calculateClicks(24, 0));
        assertEquals(24, NoteBlockHelper.calculateClicks(1, 0));
        assertEquals(7, NoteBlockHelper.calculateClicks(3, 10));
    }
}
