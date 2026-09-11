package org.uiop.easyplacefix.util;

import fi.dy.masa.litematica.config.Configs;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import static org.uiop.easyplacefix.config.easyPlacefixConfig.EXCAVATION_GUARD;
import static org.uiop.easyplacefix.config.easyPlacefixConfig.EXCAVATION_GUARD_HINT;
import static org.uiop.easyplacefix.config.easyPlacefixConfig.EXCAVATION_GUARD_PROTECT_CORRECT;
import static org.uiop.easyplacefix.config.easyPlacefixConfig.EXCAVATION_GUARD_PROTECT_OUTSIDE;

/**
 * Optional companion to Easy Place: while Litematica's Easy Place mode is on and this guard is
 * enabled, cancels breaking blocks outside every loaded schematic placement and blocks that
 * already match the schematic exactly, so raw excavation only clears leftover or wrong material.
 * Reuses {@link EasyPlaceHandler#isSchematicBlock(BlockPos)} for placement geometry, the same
 * MaLiLib-bounding-box-version-tolerant check the rest of EasyPlaceFix already relies on.
 */
public final class ExcavationGuard {
    private ExcavationGuard() {
    }

    public static boolean shouldBlockBreak(BlockPos pos) {
        if (!EXCAVATION_GUARD.getBooleanValue() || !Configs.Generic.EASY_PLACE_MODE.getBooleanValue()) {
            return false;
        }

        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null) {
            return false;
        }

        if (!EasyPlaceHandler.isSchematicBlock(pos)) {
            if (EXCAVATION_GUARD_PROTECT_OUTSIDE.getBooleanValue()) {
                notify(mc, "easyplacefix.excavationguard.hint.outside");
                return true;
            }
            return false;
        }

        if (EXCAVATION_GUARD_PROTECT_CORRECT.getBooleanValue()) {
            Level schematicWorld = SchematicWorldHandler.getSchematicWorld();
            if (schematicWorld != null) {
                BlockState expected = schematicWorld.getBlockState(pos);
                if (!expected.isAir() && expected.equals(level.getBlockState(pos))) {
                    notify(mc, "easyplacefix.excavationguard.hint.correct");
                    return true;
                }
            }
        }

        return false;
    }

    private static void notify(Minecraft mc, String translationKey) {
        if (!EXCAVATION_GUARD_HINT.getBooleanValue()) {
            return;
        }
        LocalPlayer player = mc.player;
        if (player != null) {
            player.sendOverlayMessage(Component.translatable(translationKey));
        }
    }
}
