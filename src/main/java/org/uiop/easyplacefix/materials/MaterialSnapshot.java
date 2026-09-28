package org.uiop.easyplacefix.materials;

import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.materials.MaterialListBase;
import fi.dy.masa.litematica.materials.MaterialListEntry;
import fi.dy.masa.litematica.materials.MaterialListPlacement;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacement;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public record MaterialSnapshot(String title, List<Row> rows) {
    public static final int SHULKER_SLOTS = 27;

    public record Row(ItemStack stack, String id, String name, int total, int missing, int mismatched,
                      int available) {
        public int maxStackSize() {
            return Math.max(1, this.stack.getMaxStackSize());
        }

        public int stillNeeded() {
            return Math.max(0, this.missing - this.available);
        }

        public int stacks(int count) {
            return (count + maxStackSize() - 1) / maxStackSize();
        }

        public double shulkerBoxes(int count) {
            return stacks(count) / (double) SHULKER_SLOTS;
        }

        public boolean matches(ItemStack other) {
            return !other.isEmpty() && ItemStack.isSameItemSameComponents(this.stack, other);
        }
    }

    public enum Status {
        READY,
        NO_LIST,
        CREATED
    }

    public record Result(Status status, MaterialSnapshot snapshot) {
    }

    public static Result capture(boolean createIfMissing) {
        MaterialListBase list = DataManager.getMaterialList();
        if (list == null) {
            if (!createIfMissing) {
                return new Result(Status.NO_LIST, null);
            }
            SchematicPlacement placement = DataManager.getSchematicPlacementManager().getSelectedSchematicPlacement();
            if (placement == null) {
                return new Result(Status.NO_LIST, null);
            }
            DataManager.setMaterialList(new MaterialListPlacement(placement, true));
            return new Result(Status.CREATED, null);
        }

        if (Minecraft.getInstance().player != null) {
            list.updateCounts();
        }

        List<Row> rows = new ArrayList<>();
        for (MaterialListEntry entry : list.getMaterialsAll()) {
            ItemStack stack = entry.getStack();
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            ItemStack copy = stack.copyWithCount(1);
            rows.add(new Row(
                    copy,
                    BuiltInRegistries.ITEM.getKey(copy.getItem()).toString(),
                    copy.getHoverName().getString(),
                    entry.getCountTotal(),
                    entry.getCountMissing(),
                    entry.getCountMismatched(),
                    entry.getCountAvailable()
            ));
        }
        rows.sort(Comparator.comparingInt(Row::stillNeeded).reversed()
                .thenComparing(Comparator.comparingInt(Row::total).reversed())
                .thenComparing(Row::name));
        return new Result(Status.READY, new MaterialSnapshot(list.getTitle(), rows));
    }

    public long total() {
        return this.rows.stream().mapToLong(Row::total).sum();
    }

    public long missing() {
        return this.rows.stream().mapToLong(Row::missing).sum();
    }

    public long stillNeeded() {
        return this.rows.stream().mapToLong(Row::stillNeeded).sum();
    }

    public long stacksNeeded() {
        return this.rows.stream().mapToLong(row -> row.stacks(row.stillNeeded())).sum();
    }

    public List<Row> neededRows() {
        return this.rows.stream().filter(row -> row.stillNeeded() > 0).toList();
    }
}
