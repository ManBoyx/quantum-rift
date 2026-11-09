package com.quantumrift.content;

import com.quantumrift.content.block.RiftGateCoreBlock;
import com.quantumrift.content.block.RiftPortalBlock;
import com.quantumrift.content.block.SovereignAltarBlock;
import com.quantumrift.machine.FusionChamberBlockEntity;
import com.quantumrift.machine.MatterCollectorBlockEntity;
import com.quantumrift.machine.QuantumFurnaceBlock;
import com.quantumrift.machine.QuantumVaultBlockEntity;
import com.quantumrift.machine.SimpleMachineBlock;
import com.quantumrift.registry.Registrar;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.MapColor;

import java.util.function.Supplier;

public final class ModBlocks {

    public static final Supplier<Block> QUANTITE_ORE = block("quantite_ore", () -> new DropExperienceBlock(UniformInt.of(2, 5),
            Properties.of().mapColor(MapColor.STONE).strength(3.5F, 3.5F).requiresCorrectToolForDrops()));
    public static final Supplier<Block> DEEPSLATE_QUANTITE_ORE = block("deepslate_quantite_ore", () -> new DropExperienceBlock(UniformInt.of(2, 5),
            Properties.of().mapColor(MapColor.DEEPSLATE).strength(4.5F, 3.5F).requiresCorrectToolForDrops().sound(SoundType.DEEPSLATE)));
    public static final Supplier<Block> RAW_QUANTITE_BLOCK = block("raw_quantite_block", () -> new Block(
            Properties.of().mapColor(MapColor.COLOR_CYAN).strength(5.0F, 6.0F).requiresCorrectToolForDrops()));
    public static final Supplier<Block> QUANTITE_BLOCK = block("quantite_block", () -> new Block(
            Properties.of().mapColor(MapColor.COLOR_CYAN).strength(5.0F, 6.0F).requiresCorrectToolForDrops().sound(SoundType.METAL)));

    public static final Supplier<Block> SINGULARITY_ORE = block("singularity_ore", () -> new DropExperienceBlock(UniformInt.of(5, 9),
            Properties.of().mapColor(MapColor.COLOR_BLACK).strength(8.0F, 9.0F).requiresCorrectToolForDrops().lightLevel(s -> 4)));
    public static final Supplier<Block> SINGULARITY_BLOCK = block("singularity_block", () -> new Block(
            Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(50.0F, 1200.0F).requiresCorrectToolForDrops().sound(SoundType.NETHERITE_BLOCK)));

    public static final Supplier<Block> QUANTUM_STONE = block("quantum_stone", () -> new Block(
            Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(4.0F, 6.0F).requiresCorrectToolForDrops().sound(SoundType.DEEPSLATE)));
    /** Inert gate surface; the core block wakes it up. */
    public static final Supplier<Block> DORMANT_RIFT = block("dormant_rift", () -> new Block(
            Properties.of().mapColor(MapColor.COLOR_BLACK).strength(-1.0F, 3600000.0F).noOcclusion().sound(SoundType.AMETHYST)));
    public static final Supplier<Block> RIFT_PORTAL = block("rift_portal", () -> new RiftPortalBlock(
            Properties.of().mapColor(MapColor.COLOR_PURPLE).noCollission().noOcclusion().strength(-1.0F).lightLevel(s -> 11).sound(SoundType.GLASS)));
    public static final Supplier<Block> RIFT_GATE_CORE = block("rift_gate_core", () -> new RiftGateCoreBlock(
            Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(50.0F, 1200.0F).requiresCorrectToolForDrops()
                    .lightLevel(s -> 4 + 3 * s.getValue(RiftGateCoreBlock.RODS)).sound(SoundType.AMETHYST)));

    // ---- Omnium ore exists in every dimension (4 host variants), plus machines
    public static final Supplier<Block> OMNIUM_ORE = block("omnium_ore", () -> new DropExperienceBlock(UniformInt.of(3, 6),
            Properties.of().mapColor(MapColor.STONE).strength(4.0F, 4.0F).requiresCorrectToolForDrops()));
    public static final Supplier<Block> DEEPSLATE_OMNIUM_ORE = block("deepslate_omnium_ore", () -> new DropExperienceBlock(UniformInt.of(3, 6),
            Properties.of().mapColor(MapColor.DEEPSLATE).strength(5.0F, 4.0F).requiresCorrectToolForDrops().sound(SoundType.DEEPSLATE)));
    public static final Supplier<Block> NETHER_OMNIUM_ORE = block("nether_omnium_ore", () -> new DropExperienceBlock(UniformInt.of(3, 6),
            Properties.of().mapColor(MapColor.NETHER).strength(3.5F, 4.0F).requiresCorrectToolForDrops().sound(SoundType.NETHER_ORE)));
    public static final Supplier<Block> END_OMNIUM_ORE = block("end_omnium_ore", () -> new DropExperienceBlock(UniformInt.of(3, 6),
            Properties.of().mapColor(MapColor.SAND).strength(4.5F, 9.0F).requiresCorrectToolForDrops()));
    public static final Supplier<Block> OMNIUM_BLOCK = block("omnium_block", () -> new Block(
            Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE).strength(5.0F, 8.0F).requiresCorrectToolForDrops().sound(SoundType.METAL)));
    public static final Supplier<Block> RIFT_CRYSTAL = block("rift_crystal", () -> new Block(
            Properties.of().mapColor(MapColor.COLOR_CYAN).strength(1.5F, 3.0F).requiresCorrectToolForDrops().lightLevel(s -> 10).sound(SoundType.AMETHYST)));

    public static final Supplier<Block> QUANTUM_FURNACE = block("quantum_furnace", () -> new QuantumFurnaceBlock(
            Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(4.0F, 6.0F).requiresCorrectToolForDrops().sound(SoundType.METAL)
                    .lightLevel(s -> s.getValue(AbstractFurnaceBlock.LIT) ? 13 : 0)));
    public static final Supplier<Block> FUSION_CHAMBER = block("fusion_chamber", () -> new SimpleMachineBlock(
            Properties.of().mapColor(MapColor.COLOR_BLACK).strength(5.0F, 8.0F).requiresCorrectToolForDrops().sound(SoundType.METAL).lightLevel(s -> 7),
            FusionChamberBlockEntity::new, () -> ModBlockEntities.FUSION_CHAMBER.get(),
            (l, p, s, be) -> FusionChamberBlockEntity.serverTick(l, p, s, (FusionChamberBlockEntity) be)));
    public static final Supplier<Block> MATTER_COLLECTOR = block("matter_collector", () -> new SimpleMachineBlock(
            Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(3.5F, 5.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
            MatterCollectorBlockEntity::new, () -> ModBlockEntities.MATTER_COLLECTOR.get(),
            (l, p, s, be) -> MatterCollectorBlockEntity.serverTick(l, p, s, (MatterCollectorBlockEntity) be)));
    public static final Supplier<Block> QUANTUM_VAULT = block("quantum_vault", () -> new SimpleMachineBlock(
            Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(3.0F, 6.0F).requiresCorrectToolForDrops().sound(SoundType.METAL),
            QuantumVaultBlockEntity::new, () -> ModBlockEntities.QUANTUM_VAULT.get(), null));

    /** Where the Void Sovereign is called. Unbreakable so the arena cannot be dismantled. */
    public static final Supplier<Block> SOVEREIGN_ALTAR = block("sovereign_altar", () -> new SovereignAltarBlock(
            Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(-1.0F, 3600000.0F).lightLevel(s -> 12).sound(SoundType.AMETHYST)));

    private ModBlocks() {}

    private static Supplier<Block> block(String name, Supplier<Block> factory) {
        Supplier<Block> block = Registrar.register(BuiltInRegistries.BLOCK, name, factory);
        ModItems.item(name, () -> new BlockItem(block.get(), new Item.Properties()));
        return block;
    }

    public static void init() {}
}
