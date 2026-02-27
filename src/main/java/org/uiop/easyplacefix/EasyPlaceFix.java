package org.uiop.easyplacefix;

import fi.dy.masa.litematica.gui.GuiConfigs;
import net.fabricmc.api.ModInitializer;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uiop.easyplacefix.Mixin.config.ConfigGuiTabAccessor;
import org.uiop.easyplacefix.config.Hotkeys;
import org.uiop.easyplacefix.config.easyPlaceFixHotkeys;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class EasyPlaceFix implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("easyplacefix");

    public static final GuiConfigs.ConfigGuiTab EASY_FIX = ConfigGuiTabAccessor.init("EASY_FIX", 6, "litematica.gui.button.config_gui.easy_fix");
    public static List<Boolean> crafterSlot = new ArrayList<>(Arrays.asList(false, false, false, false, false, false, false, false, false));
    public static boolean crafterOperation = false;
    public static volatile int screenId=1;

    @Override
    public void onInitialize() {

        Hotkeys.init();
        easyPlaceFixHotkeys.addCallbacks();
//        ClientCommandRegistrationCallback.
//                EVENT.
//                register((dispatcher, registryAccess) ->
//                                dispatcher.register(ClientCommandManager.literal("loosenMode").executes(context -> {
////                            if (loosenMode){
////                                context.getSource().sendFeedback(Text.literal("loosenModeSetting OFF"));
////                                loosenMode=false;
////                            }else {
////                                context.getSource().sendFeedback(Text.literal("loosenModeSetting ON"));
////                                loosenMode=true;
////                            }
//                                    MinecraftClient client = MinecraftClient.getInstance();
//                                    client.getMessageHandler().onGameMessage(Text.of("Feature removed"),false);
////                            client.send(()-> client.setScreen(screen));
//                                    return 1;
//                                }))
//                );
    }

    public static ItemStack findBlockInInventory(PlayerInventory inv, Predicate<Block> predicate) {
        for (int slot = 0; slot < inv.size(); slot++) {
            ItemStack stack = inv.getStack(slot);
            if (!stack.isEmpty()) {
                Block block = Block.getBlockFromItem(stack.getItem());
                if (predicate.test(block)) {
//                    InventoryUtils.setPickedItemToHand(slot, stack, MinecraftClient.getInstance());
                    return stack; // Found a matching item stack and return it
                }
            }
        }
        return null; // Return null when nothing matches
    }

}
