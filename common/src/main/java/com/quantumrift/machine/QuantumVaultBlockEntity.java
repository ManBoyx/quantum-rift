package com.quantumrift.machine;

import com.quantumrift.content.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.level.block.state.BlockState;

/** 6x9 storage. */
public class QuantumVaultBlockEntity extends ContainerEntity {

    public QuantumVaultBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.QUANTUM_VAULT.get(), pos, state, 54);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.quantumrift.quantum_vault");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return ChestMenu.sixRows(id, inventory, this);
    }
}
