package com.quantumrift.machine;

import com.quantumrift.content.ModItems;
import com.quantumrift.content.ModMenus;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public class FusionChamberMenu extends AbstractContainerMenu {

    /** GUI geometry, shared with tools/gen_content2.py (the background texture). */
    public static final int GRID_X = 17, GRID_Y = 18, CELL = 18, HEART_X = 104, HEART_Y = 36, OUT_X = 152, OUT_Y = 36;

    private final Container container;
    private final ContainerData data;

    public FusionChamberMenu(int id, Inventory inventory) {
        this(id, inventory, new SimpleContainer(FusionChamberBlockEntity.OUTPUT_SLOT + 1), new SimpleContainerData(2));
    }

    public FusionChamberMenu(int id, Inventory inventory, Container container, ContainerData data) {
        super(ModMenus.FUSION_CHAMBER.get(), id);
        checkContainerSize(container, FusionChamberBlockEntity.OUTPUT_SLOT + 1);
        this.container = container;
        this.data = data;
        container.startOpen(inventory.player);
        for (int i = 0; i < FusionChamberBlockEntity.MODULE_SLOTS; i++) {
            int index = i;
            addSlot(new OnlySlot(container, i, GRID_X + (i % 4) * CELL, GRID_Y + (i / 4) * CELL, () -> ModItems.MODULES.get(index).get()));
        }
        addSlot(new OnlySlot(container, FusionChamberBlockEntity.HEART_SLOT, HEART_X, HEART_Y, () -> ModItems.SOVEREIGN_HEART.get()));
        addSlot(new Slot(container, FusionChamberBlockEntity.OUTPUT_SLOT, OUT_X, OUT_Y) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }
        });
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
        }
        for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col, 8 + col * 18, 142));
        addDataSlots(data);
    }

    /** 0..1 progress for the arrow. */
    public float progress() {
        int total = data.get(1);
        return total <= 0 ? 0.0F : Math.min(1.0F, data.get(0) / (float) total);
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        container.stopOpen(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            int machineSlots = FusionChamberBlockEntity.OUTPUT_SLOT + 1;
            if (index < machineSlots) {
                if (!moveItemStackTo(stack, machineSlots, slots.size(), true)) return ItemStack.EMPTY;
            } else if (!moveItemStackTo(stack, 0, FusionChamberBlockEntity.OUTPUT_SLOT, false)) {
                return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        }
        return result;
    }

    private static final class OnlySlot extends Slot {
        private final Supplier<Item> required;

        OnlySlot(Container container, int index, int x, int y, Supplier<Item> required) {
            super(container, index, x, y);
            this.required = required;
        }

        @Override public boolean mayPlace(ItemStack stack) { return stack.is(required.get()); }
        @Override public int getMaxStackSize() { return 1; }
    }
}
