package com.quantumrift;

import com.quantumrift.content.ModItems;
import com.quantumrift.content.RiftTrades;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;
import com.quantumrift.entity.ModEntities;
import com.quantumrift.registry.ForgeRegistrar;
import com.quantumrift.world.RiftBiomes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Constants.MOD_ID)
public class QuantumRift {

    public QuantumRift() {
        Constants.LOG.info("Quantum Rift (Forge)");
        CommonClass.init();
        var modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ForgeRegistrar.flush(modBus);

        modBus.addListener((FMLCommonSetupEvent e) -> e.enqueueWork(RiftBiomes::register));
        modBus.addListener((EntityAttributeCreationEvent e) -> ModEntities.attributes(e::put));
        modBus.addListener((SpawnPlacementRegisterEvent e) -> ModEntities.placements(new ModEntities.PlacementRegistrar() {
            @Override
            public <T extends Mob> void register(EntityType<T> type, SpawnPlacementType placement, Heightmap.Types heightmap,
                                                 SpawnPlacements.SpawnPredicate<T> predicate) {
                e.register(type, placement, heightmap, predicate, SpawnPlacementRegisterEvent.Operation.REPLACE);
            }
        }));
        MinecraftForge.EVENT_BUS.addListener((FurnaceFuelBurnTimeEvent e) -> {
            if (e.getItemStack().is(ModItems.QUANTINIUM_COAL.get())) e.setBurnTime(6400);
        });
        MinecraftForge.EVENT_BUS.addListener((VillagerTradesEvent e) -> {
            if (e.getType() == VillagerProfession.CARTOGRAPHER) {
                e.getTrades().get(RiftTrades.CARTOGRAPHER_LEVEL).add(RiftTrades.riftMap());
            }
        });
    }
}
