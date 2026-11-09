package com.quantumrift;

import com.quantumrift.content.ModItems;
import com.quantumrift.content.RiftTrades;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import com.quantumrift.entity.ModEntities;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class QuantumRift implements ModInitializer {

    @Override
    public void onInitialize() {
        Constants.LOG.info("Quantum Rift (Fabric)");
        CommonClass.init();
        // NeoForge/Forge do this through data/<ns>/<loader>/biome_modifier/*.json
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Decoration.UNDERGROUND_ORES,
                ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "quantite_ore")));
        FuelRegistry.INSTANCE.add(ModItems.QUANTINIUM_COAL.get(), 6400);
        // Omnium ore exists in all three vanilla dimensions (the Quantum Realm biomes list it themselves).
        ResourceKey<PlacedFeature> omnium = ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "omnium_ore"));
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Decoration.UNDERGROUND_ORES, omnium);
        BiomeModifications.addFeature(BiomeSelectors.foundInTheNether(), GenerationStep.Decoration.UNDERGROUND_ORES, omnium);
        BiomeModifications.addFeature(BiomeSelectors.foundInTheEnd(), GenerationStep.Decoration.UNDERGROUND_ORES, omnium);
        ModEntities.attributes(FabricDefaultAttributeRegistry::register);
        ModEntities.placements(SpawnPlacements::register);
        TradeOfferHelper.registerVillagerOffers(VillagerProfession.CARTOGRAPHER, RiftTrades.CARTOGRAPHER_LEVEL,
                factories -> factories.add(RiftTrades.riftMap()));
    }
}
