package org.uiop.easyplacefix.config;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBooleanHotkeyed;
import fi.dy.masa.malilib.config.options.ConfigInteger;
import fi.dy.masa.malilib.config.options.ConfigOptionList;

public final class easyPlacefixConfig {
    public static final ConfigBooleanHotkeyed LOOSEN_MODE =
            new ConfigBooleanHotkeyed("loosenMode", false, "","EasyPlaceFix.config.generic.comment.loosenMode");

    public static final ConfigBooleanHotkeyed IGNORE_NBT =
            new ConfigBooleanHotkeyed("nbtIgnore", false, "","EasyPlaceFix.config.generic.comment.nbtIgnore");
    public static final ConfigBooleanHotkeyed Allow_Interaction =
            new ConfigBooleanHotkeyed("AllowInteraction", false, "","EasyPlaceFix.config.generic.comment.AllowInteraction");
    public static final ConfigBooleanHotkeyed OBSERVER_DETECT =
            new ConfigBooleanHotkeyed("observerDetect", false,"","EasyPlaceFix.config.generic.comment.observerDetect");
    public static final ConfigBooleanHotkeyed ENABLE_FIX =
            new ConfigBooleanHotkeyed("enableFix", false,"","EasyPlaceFix.config.generic.comment.enableFix");
    public static final ConfigBooleanHotkeyed CLIENT_ROTATION_REVERT =
            new ConfigBooleanHotkeyed("clientRotationRevert", false,"","EasyPlaceFix.config.generic.comment.clientRotationRevert","Rotation Revert","Client Rotation Revert");
    public static final ConfigBooleanHotkeyed DIAGNOSTIC_STATUS =
            new ConfigBooleanHotkeyed("diagnosticStatus", false, "", "EasyPlaceFix.config.generic.comment.diagnosticStatus");
    public static final ConfigOptionList PLACEMENT_PRESET =
            new ConfigOptionList("placementPreset", PlacementPreset.BALANCED, "EasyPlaceFix.config.generic.comment.placementPreset");
    public static final ConfigInteger PLACEMENT_DELAY =
            new ConfigInteger("placementDelay", 0, 0, 20, "EasyPlaceFix.config.generic.comment.placementDelay");

    static {
        ENABLE_FIX.translatedName("easyplacefix.config.name.enableFix");
        PLACEMENT_PRESET.translatedName("easyplacefix.config.name.placementPreset");
        DIAGNOSTIC_STATUS.translatedName("easyplacefix.config.name.diagnosticStatus");
        LOOSEN_MODE.translatedName("easyplacefix.config.name.loosenMode");
        IGNORE_NBT.translatedName("easyplacefix.config.name.nbtIgnore");
        Allow_Interaction.translatedName("easyplacefix.config.name.AllowInteraction");
        OBSERVER_DETECT.translatedName("easyplacefix.config.name.observerDetect");
        CLIENT_ROTATION_REVERT.translatedName("easyplacefix.config.name.clientRotationRevert");
        PLACEMENT_DELAY.translatedName("easyplacefix.config.name.placementDelay");
    }

    public static int getEffectivePlacementDelayTicks() {
        PlacementPreset preset = (PlacementPreset) PLACEMENT_PRESET.getOptionListValue();
        return preset.getDelayTicks(PLACEMENT_DELAY.getIntegerValue());
    }

    public static IConfigBase[] getExtraGenericConfigs() {
        return new IConfigBase[]{
                ENABLE_FIX,
                PLACEMENT_PRESET,
                DIAGNOSTIC_STATUS,
                LOOSEN_MODE,
                IGNORE_NBT,
                Allow_Interaction,
                OBSERVER_DETECT,
                CLIENT_ROTATION_REVERT,
                PLACEMENT_DELAY,
        };
    }
}
