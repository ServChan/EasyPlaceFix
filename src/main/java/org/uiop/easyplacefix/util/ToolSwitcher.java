package org.uiop.easyplacefix.util;

import fi.dy.masa.litematica.config.Configs;
import fi.dy.masa.litematica.util.EntityUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import static org.uiop.easyplacefix.config.easyPlacefixConfig.AUTO_TOOL_SWITCH;
import static org.uiop.easyplacefix.util.PlacementInventory.pickItem;

public final class ToolSwitcher {
    private ToolSwitcher() {
    }

    public static void trySwitchTool(BlockPos pos) {
        if (!AUTO_TOOL_SWITCH.getBooleanValue() || !Configs.Generic.EASY_PLACE_MODE.getBooleanValue()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        Level level = mc.level;
        if (player == null || level == null || EntityUtils.isCreativeMode(player)) {
            return;
        }

        BlockState state = level.getBlockState(pos);
        if (state.isAir()) {
            return;
        }

        ItemStack current = player.getMainHandItem();
        Inventory inventory = player.getInventory();

        ItemStack best = current;
        float bestSpeed = effectiveSpeed(current, state);

        for (int i = 0; i < inventory.getContainerSize(); ++i) {
            ItemStack stack = inventory.getItem(i);
            if (stack.isEmpty() || stack == current || isAboutToBreak(stack)) {
                continue;
            }
            float speed = effectiveSpeed(stack, state);
            if (speed > bestSpeed) {
                bestSpeed = speed;
                best = stack;
            }
        }

        if (best != current) {
            pickItem(mc, best);
        }
    }

    private static boolean isAboutToBreak(ItemStack stack) {
        return stack.isDamageableItem() && stack.getMaxDamage() - stack.getDamageValue() <= 1;
    }

    private static float effectiveSpeed(ItemStack stack, BlockState state) {
        if (state.requiresCorrectToolForDrops() && !stack.isCorrectToolForDrops(state)) {
            return -1.0F;
        }
        return stack.getDestroySpeed(state);
    }
}
