package org.uiop.easyplacefix.config;

import fi.dy.masa.malilib.config.IConfigBoolean;
import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class PrettyToggleCallback implements IHotkeyCallback {
    private final IConfigBoolean config;
    private final String labelKey;

    public PrettyToggleCallback(IConfigBoolean config, String labelKey) {
        this.config = config;
        this.labelKey = labelKey;
    }

    @Override
    public boolean onKeyAction(KeyAction action, IKeybind keybind) {
        this.config.toggleBooleanValue();

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            boolean enabled = this.config.getBooleanValue();
            Component state = Component.translatable(enabled
                            ? "easyplacefix.message.state.on"
                            : "easyplacefix.message.state.off")
                    .withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED);
            Component message = Component.literal("EasyPlaceFix ")
                    .withStyle(ChatFormatting.GOLD)
                    .append(Component.literal(":: ").withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.translatable(this.labelKey).withStyle(ChatFormatting.YELLOW))
                    .append(Component.literal(" -> ").withStyle(ChatFormatting.GRAY))
                    .append(state);
            mc.player.sendOverlayMessage(message);
        }

        return true;
    }
}
