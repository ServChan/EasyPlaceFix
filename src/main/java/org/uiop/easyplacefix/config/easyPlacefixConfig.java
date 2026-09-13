package org.uiop.easyplacefix.config;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBooleanHotkeyed;
import fi.dy.masa.malilib.config.options.ConfigInteger;
import fi.dy.masa.malilib.config.options.ConfigOptionList;

public final class easyPlacefixConfig {
    // ---- Core ----
    public static final ConfigBooleanHotkeyed ENABLE_FIX =
            new ConfigBooleanHotkeyed("enableFix", false, "", "EasyPlaceFix.config.generic.comment.enableFix");

    // ---- Pacing / anti-cheat ----
    public static final ConfigOptionList PLACEMENT_PRESET =
            new ConfigOptionList("placementPreset", PlacementPreset.BALANCED, "EasyPlaceFix.config.generic.comment.placementPreset");
    public static final ConfigInteger PLACEMENT_DELAY =
            new ConfigInteger("placementDelay", 2, 0, 20, "EasyPlaceFix.config.generic.comment.placementDelay");
    public static final ConfigBooleanHotkeyed PLACEMENT_JITTER =
            new ConfigBooleanHotkeyed("placementJitter", false, "", "EasyPlaceFix.config.generic.comment.placementJitter");

    // ---- Item matching ----
    public static final ConfigBooleanHotkeyed LOOSEN_MODE =
            new ConfigBooleanHotkeyed("loosenMode", false, "", "EasyPlaceFix.config.generic.comment.loosenMode");
    public static final ConfigBooleanHotkeyed IGNORE_NBT =
            new ConfigBooleanHotkeyed("nbtIgnore", false, "", "EasyPlaceFix.config.generic.comment.nbtIgnore");

    // ---- Interaction behaviour ----
    public static final ConfigBooleanHotkeyed Allow_Interaction =
            new ConfigBooleanHotkeyed("AllowInteraction", false, "", "EasyPlaceFix.config.generic.comment.AllowInteraction");
    public static final ConfigBooleanHotkeyed OBSERVER_DETECT =
            new ConfigBooleanHotkeyed("observerDetect", false, "", "EasyPlaceFix.config.generic.comment.observerDetect");
    public static final ConfigBooleanHotkeyed CLIENT_ROTATION_REVERT =
            new ConfigBooleanHotkeyed("clientRotationRevert", false, "", "EasyPlaceFix.config.generic.comment.clientRotationRevert", "Rotation Revert", "Client Rotation Revert");

    // ---- Debugging ----
    public static final ConfigBooleanHotkeyed DIAGNOSTIC_STATUS =
            new ConfigBooleanHotkeyed("diagnosticStatus", false, "", "EasyPlaceFix.config.generic.comment.diagnosticStatus");

    // ---- Excavation guard ----
    public static final ConfigBooleanHotkeyed EXCAVATION_GUARD =
            new ConfigBooleanHotkeyed("excavationGuard", false, "", "EasyPlaceFix.config.generic.comment.excavationGuard");
    public static final ConfigBooleanHotkeyed EXCAVATION_GUARD_PROTECT_OUTSIDE =
            new ConfigBooleanHotkeyed("excavationGuardProtectOutside", true, "", "EasyPlaceFix.config.generic.comment.excavationGuardProtectOutside");
    public static final ConfigBooleanHotkeyed EXCAVATION_GUARD_PROTECT_CORRECT =
            new ConfigBooleanHotkeyed("excavationGuardProtectCorrect", true, "", "EasyPlaceFix.config.generic.comment.excavationGuardProtectCorrect");
    public static final ConfigBooleanHotkeyed EXCAVATION_GUARD_HINT =
            new ConfigBooleanHotkeyed("excavationGuardHint", true, "", "EasyPlaceFix.config.generic.comment.excavationGuardHint");

    // ---- Tool switching ----
    public static final ConfigBooleanHotkeyed AUTO_TOOL_SWITCH =
            new ConfigBooleanHotkeyed("autoToolSwitch", false, "", "EasyPlaceFix.config.generic.comment.autoToolSwitch");

    // ---- Terrain auto-replace ----
    public static final ConfigBooleanHotkeyed TERRAIN_AUTO_REPLACE =
            new ConfigBooleanHotkeyed("terrainAutoReplace", false, "", "EasyPlaceFix.config.generic.comment.terrainAutoReplace");

    static {
        ENABLE_FIX.translatedName("easyplacefix.config.name.enableFix");
        PLACEMENT_PRESET.translatedName("easyplacefix.config.name.placementPreset");
        PLACEMENT_DELAY.translatedName("easyplacefix.config.name.placementDelay");
        PLACEMENT_JITTER.translatedName("easyplacefix.config.name.placementJitter");
        LOOSEN_MODE.translatedName("easyplacefix.config.name.loosenMode");
        IGNORE_NBT.translatedName("easyplacefix.config.name.nbtIgnore");
        Allow_Interaction.translatedName("easyplacefix.config.name.AllowInteraction");
        OBSERVER_DETECT.translatedName("easyplacefix.config.name.observerDetect");
        CLIENT_ROTATION_REVERT.translatedName("easyplacefix.config.name.clientRotationRevert");
        DIAGNOSTIC_STATUS.translatedName("easyplacefix.config.name.diagnosticStatus");
        EXCAVATION_GUARD.translatedName("easyplacefix.config.name.excavationGuard");
        EXCAVATION_GUARD_PROTECT_OUTSIDE.translatedName("easyplacefix.config.name.excavationGuardProtectOutside");
        EXCAVATION_GUARD_PROTECT_CORRECT.translatedName("easyplacefix.config.name.excavationGuardProtectCorrect");
        EXCAVATION_GUARD_HINT.translatedName("easyplacefix.config.name.excavationGuardHint");
        AUTO_TOOL_SWITCH.translatedName("easyplacefix.config.name.autoToolSwitch");
        TERRAIN_AUTO_REPLACE.translatedName("easyplacefix.config.name.terrainAutoReplace");
    }

    /**
     * Effective number of client ticks to wait between two placements.
     * Driven by the selected preset; the {@link #PLACEMENT_DELAY} slider only
     * applies while the preset is {@code Custom}.
     */
    public static int getEffectivePlacementDelayTicks() {
        PlacementPreset preset = (PlacementPreset) PLACEMENT_PRESET.getOptionListValue();
        return preset.getDelayTicks(PLACEMENT_DELAY.getIntegerValue());
    }

    public static IConfigBase[] getExtraGenericConfigs() {
        return new IConfigBase[]{
                ENABLE_FIX,
                PLACEMENT_PRESET,
                PLACEMENT_DELAY,
                PLACEMENT_JITTER,
                LOOSEN_MODE,
                IGNORE_NBT,
                Allow_Interaction,
                OBSERVER_DETECT,
                CLIENT_ROTATION_REVERT,
                DIAGNOSTIC_STATUS,
                EXCAVATION_GUARD,
                EXCAVATION_GUARD_PROTECT_OUTSIDE,
                EXCAVATION_GUARD_PROTECT_CORRECT,
                EXCAVATION_GUARD_HINT,
                AUTO_TOOL_SWITCH,
                TERRAIN_AUTO_REPLACE,
        };
    }
}
