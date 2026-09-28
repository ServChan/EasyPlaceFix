package org.uiop.easyplacefix.materials;

import com.tick_ins.tick.RunnableWithCountDown;
import com.tick_ins.tick.TickThread;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.DispenserMenu;
import net.minecraft.world.inventory.HopperMenu;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import org.uiop.easyplacefix.mixin.AccessorMixin.AbstractContainerScreenAccessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.uiop.easyplacefix.config.easyPlacefixConfig.MATERIAL_HELPER;

public final class ContainerMaterialHelper {
    private static final int REFRESH_INTERVAL_TICKS = 10;
    private static final int DIRECT_COLOR = 0x6030D040;
    private static final int SHULKER_COLOR = 0x60E0B020;
    private static final int OUTLINE_COLOR = 0xC0FFFFFF;
    private static final int BUTTON_WIDTH = 96;
    private static final int BUTTON_HEIGHT = 20;

    private static AbstractContainerScreen<?> openScreen;
    private static MaterialSnapshot snapshot;
    private static Plan plan = Plan.EMPTY;
    private static int ticksUntilRefresh;
    private static boolean refillRunning;

    private enum Kind {
        DIRECT,
        SHULKER
    }

    private record Move(int slotIndex, ItemStack expected, Kind kind) {
    }

    private record Plan(List<Move> moves, Map<Integer, Kind> highlighted) {
        private static final Plan EMPTY = new Plan(List.of(), Map.of());

        private long directCount() {
            return this.moves.stream().filter(move -> move.kind() == Kind.DIRECT).count();
        }

        private long shulkerCount() {
            return this.moves.stream().filter(move -> move.kind() == Kind.SHULKER).count();
        }
    }

    private ContainerMaterialHelper() {
    }

    public static void init() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof AbstractContainerScreen<?> containerScreen) || !isStorageScreen(containerScreen)) {
                return;
            }
            refillRunning = false;
            openScreen = containerScreen;
            ScreenEvents.remove(screen).register(current -> {
                if (openScreen == current) {
                    openScreen = null;
                }
            });
            refresh(containerScreen);
            if (!MATERIAL_HELPER.getBooleanValue() || snapshot == null) {
                return;
            }
            addRefillButton(containerScreen);
            ScreenEvents.afterExtract(screen).register((current, graphics, mouseX, mouseY, delta) ->
                    renderHighlights(containerScreen, graphics));
            ScreenEvents.afterTick(screen).register(current -> {
                if (--ticksUntilRefresh <= 0) {
                    refresh(containerScreen);
                }
            });
            ScreenEvents.remove(screen).register(current -> {
                plan = Plan.EMPTY;
                refillRunning = false;
            });
        });
    }

    public static boolean isStorageScreen(Screen screen) {
        if (!(screen instanceof AbstractContainerScreen<?> containerScreen)) {
            return false;
        }
        AbstractContainerMenu menu = containerScreen.getMenu();
        return menu instanceof ChestMenu
                || menu instanceof ShulkerBoxMenu
                || menu instanceof DispenserMenu
                || menu instanceof HopperMenu;
    }

    private static void refresh(AbstractContainerScreen<?> screen) {
        ticksUntilRefresh = REFRESH_INTERVAL_TICKS;
        MaterialSnapshot.Result result = MaterialSnapshot.capture(false);
        snapshot = result.snapshot();
        plan = snapshot == null ? Plan.EMPTY : buildPlan(screen, snapshot);
    }

    private static Plan buildPlan(AbstractContainerScreen<?> screen, MaterialSnapshot materials) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return Plan.EMPTY;
        }
        Map<MaterialSnapshot.Row, Integer> remaining = new HashMap<>();
        for (MaterialSnapshot.Row row : materials.neededRows()) {
            remaining.put(row, row.stillNeeded());
        }
        if (remaining.isEmpty()) {
            return Plan.EMPTY;
        }

        List<Move> moves = new ArrayList<>();
        Map<Integer, Kind> highlighted = new HashMap<>();
        for (Slot slot : screen.getMenu().slots) {
            if (slot.container == player.getInventory() || !slot.hasItem()) {
                continue;
            }
            ItemStack stack = slot.getItem();
            MaterialSnapshot.Row direct = findRow(remaining, stack);
            if (direct != null) {
                remaining.merge(direct, -stack.getCount(), Integer::sum);
                moves.add(new Move(slot.index, stack.copy(), Kind.DIRECT));
                highlighted.put(slot.index, Kind.DIRECT);
                continue;
            }
            ItemContainerContents contents = stack.get(DataComponents.CONTAINER);
            if (contents == null) {
                continue;
            }
            boolean useful = false;
            for (ItemStack inner : contents.nonEmptyItemCopyStream().toList()) {
                MaterialSnapshot.Row row = findRow(remaining, inner);
                if (row != null) {
                    remaining.merge(row, -inner.getCount(), Integer::sum);
                    useful = true;
                }
            }
            if (useful) {
                moves.add(new Move(slot.index, stack.copy(), Kind.SHULKER));
                highlighted.put(slot.index, Kind.SHULKER);
            }
        }
        return new Plan(List.copyOf(moves), Map.copyOf(highlighted));
    }

    private static MaterialSnapshot.Row findRow(Map<MaterialSnapshot.Row, Integer> remaining, ItemStack stack) {
        for (Map.Entry<MaterialSnapshot.Row, Integer> entry : remaining.entrySet()) {
            if (entry.getValue() > 0 && entry.getKey().matches(stack)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private static void addRefillButton(AbstractContainerScreen<?> screen) {
        AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor) screen;
        int left = accessor.easyplacefix$getLeftPos();
        int top = accessor.easyplacefix$getTopPos();
        int width = accessor.easyplacefix$getImageWidth();
        int x = left + width - BUTTON_WIDTH;
        int y = top - BUTTON_HEIGHT - 2;
        if (y < 2) {
            x = left + width + 4;
            y = top;
        }
        Button button = Button.builder(Component.translatable("easyplacefix.materials.button.refill"), pressed -> refill(screen))
                .bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .tooltip(Tooltip.create(Component.translatable("easyplacefix.materials.button.refill.tooltip")))
                .build();
        Screens.getWidgets(screen).add(button);
    }

    public static boolean refillCurrentScreen() {
        AbstractContainerScreen<?> screen = openScreen;
        if (screen != null && isStorageScreen(screen)) {
            refresh(screen);
            refill(screen);
            return true;
        }
        return false;
    }

    private static void refill(AbstractContainerScreen<?> screen) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.gameMode == null) {
            return;
        }
        if (refillRunning) {
            return;
        }
        refresh(screen);
        if (snapshot == null) {
            player.sendOverlayMessage(Component.translatable("easyplacefix.materials.no_list").withStyle(ChatFormatting.YELLOW));
            return;
        }
        if (plan.moves().isEmpty()) {
            player.sendOverlayMessage(Component.translatable("easyplacefix.materials.refill.nothing").withStyle(ChatFormatting.GRAY));
            return;
        }

        int freeSlots = 0;
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (stack.isEmpty()) {
                freeSlots++;
            }
        }
        List<Move> moves = plan.moves();
        int count = Math.min(moves.size(), freeSlots);
        if (count == 0) {
            player.sendOverlayMessage(Component.translatable("easyplacefix.materials.refill.full").withStyle(ChatFormatting.RED));
            return;
        }

        int containerId = screen.getMenu().containerId;
        refillRunning = true;
        for (int i = 0; i < count; i++) {
            Move move = moves.get(i);
            boolean last = i == count - 1;
            TickThread.addCountDownTask(new RunnableWithCountDown.Builder().setCount(i + 1).build(() -> {
                try {
                    performMove(screen, containerId, move);
                } finally {
                    if (last) {
                        refillRunning = false;
                        if (openScreen == screen) {
                            refresh(screen);
                        }
                    }
                }
            }));
        }
        long shulkers = moves.subList(0, count).stream().filter(move -> move.kind() == Kind.SHULKER).count();
        player.sendOverlayMessage(Component.translatable("easyplacefix.materials.refill.started", count, shulkers)
                .withStyle(ChatFormatting.GREEN));
        if (count < moves.size()) {
            player.sendSystemMessage(Component.translatable("easyplacefix.materials.refill.partial", moves.size() - count)
                    .withStyle(ChatFormatting.YELLOW));
        }
    }

    private static void performMove(AbstractContainerScreen<?> screen, int containerId, Move move) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.gameMode == null || openScreen != screen) {
            return;
        }
        AbstractContainerMenu menu = screen.getMenu();
        if (menu.containerId != containerId || move.slotIndex() >= menu.slots.size()) {
            return;
        }
        ItemStack current = menu.slots.get(move.slotIndex()).getItem();
        if (current.isEmpty() || !ItemStack.isSameItemSameComponents(current, move.expected())) {
            return;
        }
        mc.gameMode.handleContainerInput(containerId, move.slotIndex(), 0, ContainerInput.QUICK_MOVE, mc.player);
    }

    private static void renderHighlights(AbstractContainerScreen<?> screen, GuiGraphicsExtractor graphics) {
        if (!MATERIAL_HELPER.getBooleanValue() || plan.highlighted().isEmpty()) {
            return;
        }
        AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor) screen;
        int left = accessor.easyplacefix$getLeftPos();
        int top = accessor.easyplacefix$getTopPos();
        List<Slot> slots = screen.getMenu().slots;
        for (Map.Entry<Integer, Kind> entry : plan.highlighted().entrySet()) {
            if (entry.getKey() >= slots.size()) {
                continue;
            }
            Slot slot = slots.get(entry.getKey());
            int x = left + slot.x;
            int y = top + slot.y;
            graphics.fill(x, y, x + 16, y + 16, entry.getValue() == Kind.DIRECT ? DIRECT_COLOR : SHULKER_COLOR);
            graphics.outline(x - 1, y - 1, 18, 18, OUTLINE_COLOR);
        }
        Component summary = Component.translatable("easyplacefix.materials.highlight.summary",
                plan.directCount(), plan.shulkerCount());
        graphics.text(Minecraft.getInstance().font, summary, left, screen.height - top + 4, 0xFFE0E0E0, true);
    }
}
