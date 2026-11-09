package com.quantumrift.machine;

import com.quantumrift.content.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.FurnaceMenu;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** A furnace that runs {@link QuantumFurnaceBlock#SPEED} times faster. Uses the vanilla furnace menu. */
public class QuantumFurnaceBlockEntity extends AbstractFurnaceBlockEntity {

    public QuantumFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.QUANTUM_FURNACE.get(), pos, state, RecipeType.SMELTING);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.quantumrift.quantum_furnace");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new FurnaceMenu(id, inventory, this, this.dataAccess);
    }
}
