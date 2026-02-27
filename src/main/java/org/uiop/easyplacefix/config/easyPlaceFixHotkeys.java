package org.uiop.easyplacefix.config;

import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.hotkeys.KeyCallbackToggleBooleanConfigWithMessage;

public final class easyPlaceFixHotkeys {
    public static final ConfigHotkey LOOSEN_MODE_HOTKEY =
            new ConfigHotkey("loosenModeHotkey", "", "easyPlaceFix.config.hotkeys.comment.loosenmode");
    public static final ConfigHotkey IGNORE_NBT_HOTKEY =
            new ConfigHotkey("nbtIgnoreHotkey", "", "easyPlaceFix.config.hotkeys.comment.nbtIgnore");
    public static final ConfigHotkey Allow_Interaction_HOTKEY =
            new ConfigHotkey("AllowInteractionHotkey", "", "easyPlaceFix.config.hotkeys.comment.AllowInteraction");

    public static ConfigHotkey[] getExtraHotkeys() {
        return new ConfigHotkey[]{
                LOOSEN_MODE_HOTKEY,
                IGNORE_NBT_HOTKEY,
                Allow_Interaction_HOTKEY,
        };
    }

    public static void addCallbacks() {
        LOOSEN_MODE_HOTKEY.getKeybind().setCallback(new KeyCallbackToggleBooleanConfigWithMessage(easyPlacefixConfig.LOOSEN_MODE));
        IGNORE_NBT_HOTKEY.getKeybind().setCallback(new KeyCallbackToggleBooleanConfigWithMessage(easyPlacefixConfig.IGNORE_NBT));
        Allow_Interaction_HOTKEY.getKeybind().setCallback(new KeyCallbackToggleBooleanConfigWithMessage(easyPlacefixConfig.Allow_Interaction));


    }

}

