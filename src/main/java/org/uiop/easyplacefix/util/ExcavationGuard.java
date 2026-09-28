package org.uiop.easyplacefix.util;

import fi.dy.masa.litematica.config.Configs;
import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacement;
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
            if (EXCAVATION_GUARD_PROTECT_OUTSIDE.getBooleanValue() && hasEnabledPlacement()) {
                notify(mc, "easyplacefix.excavationguard.hint.outside");
                return true;
            }
            return false;
        }

        if (EXCAVATION_GUARD_PROTECT_CORRECT.getBooleanValue()) {
            Level schematicWorld = SchematicWorldHandler.getSchematicWorld();
            if (schematicWorld != null) {
                BlockState expected = schematicWorld.getBlockState(pos);
                if (!expected.isAir() && PlacementStateMatcher.isSatisfied(expected, level.getBlockState(pos))) {
                    notify(mc, "easyplacefix.excavationguard.hint.correct");
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean hasEnabledPlacement() {
        for (SchematicPlacement placement : DataManager.getSchematicPlacementManager().getAllSchematicsPlacements()) {
            if (placement.isEnabled()) {
                return true;
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
