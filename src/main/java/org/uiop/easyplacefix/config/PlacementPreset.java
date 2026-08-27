package org.uiop.easyplacefix.config;

import fi.dy.masa.malilib.config.IConfigOptionListEntry;

public enum PlacementPreset implements IConfigOptionListEntry {
    // delayTicks = client ticks to wait between two placements (20 ticks = 1 s).
    // These are the server-facing pacing limits; too fast trips "timer" anti-cheat.
    BALANCED("balanced", "Balanced", 2),
    SAFE("safe", "Safe", 4),
    FAST("fast", "Fast", 1),
    CUSTOM("custom", "Custom", -1);

    private final String value;
    private final String displayName;
    private final int delayTicks;

    PlacementPreset(String value, String displayName, int delayTicks) {
        this.value = value;
        this.displayName = displayName;
        this.delayTicks = delayTicks;
    }

    public int getDelayTicks(int customDelayTicks) {
        if (this == CUSTOM) {
            // Custom is the explicit opt-out: allow 0 (no limit) for servers without a build anti-cheat.
            return Math.max(0, customDelayTicks);
        }
        return this.delayTicks;
    }

    @Override
    public String getStringValue() {
        return this.value;
    }

    @Override
    public String getDisplayName() {
        return this.displayName;
    }

    @Override
    public IConfigOptionListEntry cycle(boolean forward) {
        PlacementPreset[] values = values();
        int index = this.ordinal() + (forward ? 1 : -1);
        if (index < 0) {
            index = values.length - 1;
        } else if (index >= values.length) {
            index = 0;
        }
        return values[index];
    }

    @Override
    public IConfigOptionListEntry fromString(String value) {
        for (PlacementPreset preset : values()) {
            if (preset.value.equalsIgnoreCase(value)) {
                return preset;
            }
        }
        return BALANCED;
    }
}
