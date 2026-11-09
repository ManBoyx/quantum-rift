package com.quantumrift.machine;

import com.quantumrift.content.ModBlockEntities;
import com.quantumrift.content.ModItems;
import com.quantumrift.content.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Fuses one upgrade module of every ore plus a Sovereign Heart into a Prismatic Template.
 * Slots 0-11: modules (in {@link ModItems#MODULES} order), 12: heart, 13: output.
 */
public class FusionChamberBlockEntity extends ContainerEntity {

    public static final int MODULE_SLOTS = 12;
    public static final int HEART_SLOT = 12;
    public static final int OUTPUT_SLOT = 13;
    public static final int TOTAL_TIME = 160;

    private int progress;

    private final ContainerData data = new ContainerData() {
        @Override public int get(int index) { return index == 0 ? progress : TOTAL_TIME; }
        @Override public void set(int index, int value) { if (index == 0) progress = value; }
        @Override public int getCount() { return 2; }
    };

    public FusionChamberBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FUSION_CHAMBER.get(), pos, state, MODULE_SLOTS + 2);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.quantumrift.fusion_chamber");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new FusionChamberMenu(id, inventory, this, data);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        progress = tag.getInt("Progress");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Progress", progress);
    }

    private boolean canCraft() {
        for (int i = 0; i < MODULE_SLOTS; i++) {
            if (!items.get(i).is(ModItems.MODULES.get(i).get())) return false;
        }
        if (!items.get(HEART_SLOT).is(ModItems.SOVEREIGN_HEART.get())) return false;
        ItemStack out = items.get(OUTPUT_SLOT);
        return out.isEmpty() || (out.is(ModItems.PRISMATIC_TEMPLATE.get()) && out.getCount() < out.getMaxStackSize());
    }

    private void craft() {
        for (int i = 0; i <= HEART_SLOT; i++) items.get(i).shrink(1);
        ItemStack out = items.get(OUTPUT_SLOT);
        if (out.isEmpty()) items.set(OUTPUT_SLOT, new ItemStack(ModItems.PRISMATIC_TEMPLATE.get())); else out.grow(1);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FusionChamberBlockEntity be) {
        if (be.canCraft()) {
            be.progress++;
            if (be.progress % 30 == 1) {
                level.playSound(null, pos, ModSounds.BLOCK_MACHINE_HUM.get(), SoundSource.BLOCKS, 0.5F, 1.0F);
            }
            if (be.progress >= TOTAL_TIME) {
                be.craft();
                be.progress = 0;
                level.playSound(null, pos, ModSounds.BLOCK_FUSION_COMPLETE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            be.setChanged();
        } else if (be.progress > 0) {
            be.progress = Math.max(0, be.progress - 2);
            be.setChanged();
        }
    }
}
