package org.uiop.easyplacefix.until;

import fi.dy.masa.litematica.config.Configs;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static org.uiop.easyplacefix.config.easyPlacefixConfig.*;

public final class ExcavationGuard {
    private ExcavationGuard() {
    }

    public static boolean shouldBlockBreak(BlockPos pos) {
        if (!EXCAVATION_GUARD.getBooleanValue() || !Configs.Generic.EASY_PLACE_MODE.getBooleanValue()) {
            return false;
        }

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null) {
            return false;
        }

        if (!doEasyPlace.isSchematicBlock(pos)) {
            if (EXCAVATION_GUARD_PROTECT_OUTSIDE.getBooleanValue()) {
                notify(mc, "easyplacefix.excavationguard.hint.outside");
                return true;
            }
            return false;
        }

        if (EXCAVATION_GUARD_PROTECT_CORRECT.getBooleanValue()) {
            World schematicWorld = SchematicWorldHandler.getSchematicWorld();
            if (schematicWorld != null) {
                BlockState expected = schematicWorld.getBlockState(pos);
                if (!expected.isAir() && expected.equals(mc.world.getBlockState(pos))) {
                    notify(mc, "easyplacefix.excavationguard.hint.correct");
                    return true;
                }
            }
        }
        return false;
    }

    private static void notify(MinecraftClient mc, String key) {
        if (EXCAVATION_GUARD_HINT.getBooleanValue() && mc.player != null) {
            mc.player.sendMessage(Text.translatable(key), true);
        }
    }
}
