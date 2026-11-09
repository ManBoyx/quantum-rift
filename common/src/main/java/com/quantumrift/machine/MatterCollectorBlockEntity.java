package com.quantumrift.machine;

import com.quantumrift.content.ModBlockEntities;
import com.quantumrift.content.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DispenserMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/** Pulls dropped items from a radius into its 3x3 inventory. Stops while powered by redstone. */
public class MatterCollectorBlockEntity extends ContainerEntity {

    public static final int RADIUS = 8;
    private static final int PERIOD = 8;

    public MatterCollectorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MATTER_COLLECTOR.get(), pos, state, 9);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.quantumrift.matter_collector");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new DispenserMenu(id, inventory, this);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MatterCollectorBlockEntity be) {
        if (level.getGameTime() % PERIOD != 0 || level.hasNeighborSignal(pos)) return;
        AABB area = new AABB(pos).inflate(RADIUS);
        boolean picked = false;
        for (ItemEntity entity : level.getEntitiesOfClass(ItemEntity.class, area, e -> e.isAlive() && !e.hasPickUpDelay())) {
            ItemStack stack = entity.getItem();
            ItemStack remainder = be.insert(stack.copy());
            if (remainder.getCount() != stack.getCount()) {
                picked = true;
                if (remainder.isEmpty()) entity.discard(); else entity.setItem(remainder);
            }
        }
        if (picked) {
            be.setChanged();
            level.playSound(null, pos, ModSounds.BLOCK_COLLECTOR_PICK.get(), SoundSource.BLOCKS, 0.6F, 1.0F);
        }
    }

    private ItemStack insert(ItemStack stack) {
        for (int i = 0; i < items.size() && !stack.isEmpty(); i++) {
            ItemStack slot = items.get(i);
            if (!slot.isEmpty() && ItemStack.isSameItemSameComponents(slot, stack)) {
                int move = Math.min(stack.getCount(), slot.getMaxStackSize() - slot.getCount());
                if (move > 0) { slot.grow(move); stack.shrink(move); }
            }
        }
        for (int i = 0; i < items.size() && !stack.isEmpty(); i++) {
            if (items.get(i).isEmpty()) { items.set(i, stack.copy()); stack.setCount(0); }
        }
        return stack;
    }
}
