package com.quantumrift;

import com.quantumrift.content.RiftTrades;
import com.quantumrift.entity.ModEntities;
import com.quantumrift.registry.NeoRegistrar;
import com.quantumrift.world.RiftBiomes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;

@Mod(Constants.MOD_ID)
public class QuantumRift {

    public QuantumRift(IEventBus eventBus) {
        Constants.LOG.info("Quantum Rift (NeoForge)");
        CommonClass.init();
        NeoRegistrar.flush(eventBus);

        eventBus.addListener((FMLCommonSetupEvent e) -> e.enqueueWork(RiftBiomes::register));
        eventBus.addListener((EntityAttributeCreationEvent e) -> ModEntities.attributes(e::put));
        eventBus.addListener((RegisterSpawnPlacementsEvent e) -> ModEntities.placements(new ModEntities.PlacementRegistrar() {
            @Override
            public <T extends Mob> void register(EntityType<T> type, SpawnPlacementType placement, Heightmap.Types heightmap,
                                                 SpawnPlacements.SpawnPredicate<T> predicate) {
                e.register(type, placement, heightmap, predicate, RegisterSpawnPlacementsEvent.Operation.REPLACE);
            }
        }));
        NeoForge.EVENT_BUS.addListener((VillagerTradesEvent e) -> {
            if (e.getType() == VillagerProfession.CARTOGRAPHER) {
                e.getTrades().get(RiftTrades.CARTOGRAPHER_LEVEL).add(RiftTrades.riftMap());
            }
        });
    }
}
