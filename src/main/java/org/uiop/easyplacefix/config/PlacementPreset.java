package org.uiop.easyplacefix.config;

import fi.dy.masa.malilib.config.IConfigOptionListEntry;

public enum PlacementPreset implements IConfigOptionListEntry {
    BALANCED("balanced", "Balanced", 1),
    SAFE("safe", "Safe", 3),
    FAST("fast", "Fast", 0),
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
        return this == CUSTOM ? customDelayTicks : this.delayTicks;
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
