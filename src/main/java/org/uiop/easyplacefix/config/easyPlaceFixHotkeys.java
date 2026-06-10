package org.uiop.easyplacefix.config;

import fi.dy.masa.malilib.config.options.ConfigHotkey;

public final class easyPlaceFixHotkeys {
    public static final ConfigHotkey LOOSEN_MODE_HOTKEY =
            new ConfigHotkey("loosenModeHotkey", "", "easyPlaceFix.config.hotkeys.comment.loosenmode");
    public static final ConfigHotkey IGNORE_NBT_HOTKEY =
            new ConfigHotkey("nbtIgnoreHotkey", "", "easyPlaceFix.config.hotkeys.comment.nbtIgnore");
    public static final ConfigHotkey Allow_Interaction_HOTKEY =
            new ConfigHotkey("AllowInteractionHotkey", "", "easyPlaceFix.config.hotkeys.comment.AllowInteraction");
    public static final ConfigHotkey DIAGNOSTIC_STATUS_HOTKEY =
            new ConfigHotkey("diagnosticStatusHotkey", "", "easyPlaceFix.config.hotkeys.comment.diagnosticStatus");

    public static ConfigHotkey[] getExtraHotkeys() {
        return new ConfigHotkey[]{
                LOOSEN_MODE_HOTKEY,
                IGNORE_NBT_HOTKEY,
                Allow_Interaction_HOTKEY,
                DIAGNOSTIC_STATUS_HOTKEY,
        };
    }

    public static void addCallbacks() {
        easyPlacefixConfig.ENABLE_FIX.getKeybind().setCallback(new PrettyToggleCallback(easyPlacefixConfig.ENABLE_FIX, "easyplacefix.config.name.enableFix"));
        easyPlacefixConfig.LOOSEN_MODE.getKeybind().setCallback(new PrettyToggleCallback(easyPlacefixConfig.LOOSEN_MODE, "easyplacefix.config.name.loosenMode"));
        easyPlacefixConfig.IGNORE_NBT.getKeybind().setCallback(new PrettyToggleCallback(easyPlacefixConfig.IGNORE_NBT, "easyplacefix.config.name.nbtIgnore"));
        easyPlacefixConfig.Allow_Interaction.getKeybind().setCallback(new PrettyToggleCallback(easyPlacefixConfig.Allow_Interaction, "easyplacefix.config.name.AllowInteraction"));
        easyPlacefixConfig.OBSERVER_DETECT.getKeybind().setCallback(new PrettyToggleCallback(easyPlacefixConfig.OBSERVER_DETECT, "easyplacefix.config.name.observerDetect"));
        easyPlacefixConfig.CLIENT_ROTATION_REVERT.getKeybind().setCallback(new PrettyToggleCallback(easyPlacefixConfig.CLIENT_ROTATION_REVERT, "easyplacefix.config.name.clientRotationRevert"));
        easyPlacefixConfig.DIAGNOSTIC_STATUS.getKeybind().setCallback(new PrettyToggleCallback(easyPlacefixConfig.DIAGNOSTIC_STATUS, "easyplacefix.config.name.diagnosticStatus"));
        LOOSEN_MODE_HOTKEY.getKeybind().setCallback(new PrettyToggleCallback(easyPlacefixConfig.LOOSEN_MODE, "easyplacefix.config.name.loosenMode"));
        IGNORE_NBT_HOTKEY.getKeybind().setCallback(new PrettyToggleCallback(easyPlacefixConfig.IGNORE_NBT, "easyplacefix.config.name.nbtIgnore"));
        Allow_Interaction_HOTKEY.getKeybind().setCallback(new PrettyToggleCallback(easyPlacefixConfig.Allow_Interaction, "easyplacefix.config.name.AllowInteraction"));
        DIAGNOSTIC_STATUS_HOTKEY.getKeybind().setCallback(new PrettyToggleCallback(easyPlacefixConfig.DIAGNOSTIC_STATUS, "easyplacefix.config.name.diagnosticStatus"));


    }

}

