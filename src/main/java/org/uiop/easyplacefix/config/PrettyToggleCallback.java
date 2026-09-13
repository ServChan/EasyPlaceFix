package org.uiop.easyplacefix.config;

import fi.dy.masa.malilib.config.IConfigBoolean;
import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

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
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            boolean enabled = this.config.getBooleanValue();
            Text message = Text.literal("EasyPlaceFix ").formatted(Formatting.GOLD)
                    .append(Text.literal(":: ").formatted(Formatting.DARK_GRAY))
                    .append(Text.translatable(this.labelKey).formatted(Formatting.YELLOW))
                    .append(Text.literal(" -> ").formatted(Formatting.GRAY))
                    .append(Text.translatable(enabled
                                    ? "easyplacefix.message.state.on"
                                    : "easyplacefix.message.state.off")
                            .formatted(enabled ? Formatting.GREEN : Formatting.RED));
            mc.player.sendMessage(message, true);
        }
        return true;
    }
}
