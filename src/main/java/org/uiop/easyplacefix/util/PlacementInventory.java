package org.uiop.easyplacefix.util;

import fi.dy.masa.litematica.config.Configs;
import fi.dy.masa.litematica.util.EntityUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import static fi.dy.masa.litematica.util.InventoryUtils.findSlotWithBoxWithItem;
import static fi.dy.masa.litematica.util.InventoryUtils.setPickedItemToHand;
import static org.uiop.easyplacefix.config.easyPlacefixConfig.IGNORE_NBT;

public final class PlacementInventory {
    private PlacementInventory() {
    }

    public static ItemStack searchItem(Minecraft mc, ItemStack stack) {
        if (mc.player == null || mc.gameMode == null || mc.level == null || stack.isEmpty()) {
            return null;
        }

        if (EntityUtils.isCreativeMode(mc.player)) {
            return stack.copy();
        }

        Inventory inv = mc.player.getInventory();
        int slot = IGNORE_NBT.getBooleanValue()
                ? getSlotWithStackWithoutNbt(stack, inv)
                : getSlotWithStack(stack, inv);

        if (slot != -1) {
            return inv.getItem(slot);
        }

        if (Configs.Generic.PICK_BLOCK_SHULKERS.getBooleanValue()) {
            slot = findSlotWithBoxWithItem(mc.player.inventoryMenu, stack, false);
            if (slot != -1) {
                pickItem(mc, mc.player.inventoryMenu.slots.get(slot).getItem());
            }
        }

        return null;
    }

    public static int getSlotWithStackWithoutNbt(ItemStack stack, Inventory inv) {
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            if (!inv.getItem(i).isEmpty() && ItemStack.isSameItem(stack, inv.getItem(i))) {
                return i;
            }
        }

        return -1;
    }

    public static int getSlotWithStack(ItemStack stack, Inventory inv) {
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            if (!inv.getItem(i).isEmpty() && ItemStack.isSameItemSameComponents(stack, inv.getItem(i))) {
                return i;
            }
        }

        return -1;
    }

    public static void pickItem(Minecraft mc, ItemStack stack) {
        if (EntityUtils.isCreativeMode(mc.player)) {
            setPickedItemToHand(stack, mc);
            mc.gameMode.handleCreativeModeItemAdd(
                    mc.player.getItemInHand(InteractionHand.MAIN_HAND),
                    36 + mc.player.getInventory().getSelectedSlot()
            );
        } else {
            setPickedItemToHand(stack, mc);
        }
    }
}
