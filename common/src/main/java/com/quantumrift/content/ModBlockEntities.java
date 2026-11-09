package com.quantumrift.content;

import com.quantumrift.machine.FusionChamberBlockEntity;
import com.quantumrift.machine.MatterCollectorBlockEntity;
import com.quantumrift.machine.QuantumFurnaceBlockEntity;
import com.quantumrift.machine.QuantumVaultBlockEntity;
import com.quantumrift.registry.Registrar;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

public final class ModBlockEntities {

    public static final Supplier<BlockEntityType<QuantumFurnaceBlockEntity>> QUANTUM_FURNACE = reg("quantum_furnace",
            () -> BlockEntityType.Builder.of(QuantumFurnaceBlockEntity::new, ModBlocks.QUANTUM_FURNACE.get()).build(null));
    public static final Supplier<BlockEntityType<FusionChamberBlockEntity>> FUSION_CHAMBER = reg("fusion_chamber",
            () -> BlockEntityType.Builder.of(FusionChamberBlockEntity::new, ModBlocks.FUSION_CHAMBER.get()).build(null));
    public static final Supplier<BlockEntityType<MatterCollectorBlockEntity>> MATTER_COLLECTOR = reg("matter_collector",
            () -> BlockEntityType.Builder.of(MatterCollectorBlockEntity::new, ModBlocks.MATTER_COLLECTOR.get()).build(null));
    public static final Supplier<BlockEntityType<QuantumVaultBlockEntity>> QUANTUM_VAULT = reg("quantum_vault",
            () -> BlockEntityType.Builder.of(QuantumVaultBlockEntity::new, ModBlocks.QUANTUM_VAULT.get()).build(null));

    private ModBlockEntities() {}

    @SuppressWarnings("unchecked")
    private static <T extends BlockEntity> Supplier<BlockEntityType<T>> reg(String name, Supplier<BlockEntityType<T>> factory) {
        Supplier<BlockEntityType<?>> s = Registrar.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, name, factory);
        return () -> (BlockEntityType<T>) s.get();
    }

    public static void init() {}
}
