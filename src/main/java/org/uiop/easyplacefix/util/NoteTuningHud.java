package org.uiop.easyplacefix.util;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import static org.uiop.easyplacefix.config.easyPlacefixConfig.NOTE_TUNING_HUD;

public final class NoteTuningHud {
    private static final Identifier ID = Identifier.fromNamespaceAndPath("easyplacefix", "note_tuning");
    private static final int PADDING = 3;
    private static final int MARGIN = 4;
    private static final int BACKGROUND = 0x90000000;
    private static final int ACCENT = 0xFF55FF55;
    private static final int BAR_BACKGROUND = 0xFF303030;

    private static int peakClicks;

    private NoteTuningHud() {
    }

    public static void init() {
        HudElementRegistry.attachElementAfter(VanillaHudElements.MISC_OVERLAYS, ID, (graphics, deltaTracker) -> render(graphics));
    }

    private static void render(GuiGraphicsExtractor graphics) {
        Minecraft mc = Minecraft.getInstance();
        if (!NOTE_TUNING_HUD.getBooleanValue() || mc.player == null) {
            return;
        }
        NoteBlockHelper.Progress progress = NoteBlockHelper.progress(mc);
        if (progress.blocks() == 0) {
            peakClicks = 0;
            return;
        }
        peakClicks = Math.max(peakClicks, progress.clicksLeft());

        Component title = Component.translatable("easyplacefix.hud.note_tuning", progress.blocks(), progress.clicksLeft());
        Component next = progress.next() == null
                ? Component.empty()
                : Component.translatable("easyplacefix.hud.note_tuning.next", progress.next().toShortString());
        int textWidth = Math.max(mc.font.width(title), mc.font.width(next));
        int width = textWidth + PADDING * 2;
        int lineHeight = mc.font.lineHeight + 1;
        int height = lineHeight * 2 + 4 + PADDING * 2;
        int x = graphics.guiWidth() - width - MARGIN;
        int y = MARGIN;

        graphics.fill(x, y, x + width, y + height, BACKGROUND);
        graphics.text(mc.font, title, x + PADDING, y + PADDING, 0xFFFFFFFF, true);
        graphics.text(mc.font, next, x + PADDING, y + PADDING + lineHeight, 0xFFB0B0B0, true);

        int barY = y + PADDING + lineHeight * 2 + 1;
        int barWidth = width - PADDING * 2;
        graphics.fill(x + PADDING, barY, x + PADDING + barWidth, barY + 2, BAR_BACKGROUND);
        int done = peakClicks == 0 ? 0 : (int) ((peakClicks - progress.clicksLeft()) / (double) peakClicks * barWidth);
        graphics.fill(x + PADDING, barY, x + PADDING + done, barY + 2, ACCENT);
    }
}
