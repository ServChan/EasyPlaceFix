package org.uiop.easyplacefix.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlacementPresetTest {
    @Test
    void presetsUseServerSafeDelays() {
        assertEquals(2, PlacementPreset.BALANCED.getDelayTicks(0));
        assertEquals(4, PlacementPreset.SAFE.getDelayTicks(0));
        assertEquals(1, PlacementPreset.FAST.getDelayTicks(0));
        assertEquals(7, PlacementPreset.CUSTOM.getDelayTicks(7));
        assertEquals(0, PlacementPreset.CUSTOM.getDelayTicks(-1));
    }
}
