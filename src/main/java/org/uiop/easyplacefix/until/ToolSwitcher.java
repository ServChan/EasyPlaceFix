package org.uiop.easyplacefix.until;

import fi.dy.masa.litematica.config.Configs;
import fi.dy.masa.litematica.util.EntityUtils;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import static org.uiop.easyplacefix.config.easyPlacefixConfig.AUTO_TOOL_SWITCH;

public final class ToolSwitcher {
    private ToolSwitcher() {
    }

    public static void trySwitchTool(BlockPos pos) {
        if (!AUTO_TOOL_SWITCH.getBooleanValue() || !Configs.Generic.EASY_PLACE_MODE.getBooleanValue()) {
            return;
        }

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || EntityUtils.isCreativeMode(mc.player)) {
            return;
        }

        BlockState state = mc.world.getBlockState(pos);
        if (state.isAir()) {
            return;
        }

        ItemStack current = mc.player.getMainHandStack();
        PlayerInventory inventory = mc.player.getInventory();
        ItemStack best = current;
        float bestSpeed = effectiveSpeed(current, state);

        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty() || stack == current) continue;
            float speed = effectiveSpeed(stack, state);
            if (speed > bestSpeed) {
                bestSpeed = speed;
                best = stack;
            }
        }

        if (best != current) {
            doEasyPlace.pickItem(mc, best);
        }
    }

    private static float effectiveSpeed(ItemStack stack, BlockState state) {
        if (state.isToolRequired() && !stack.isSuitableFor(state)) {
            return -1.0F;
        }
        return stack.getMiningSpeedMultiplier(state);
    }
}
