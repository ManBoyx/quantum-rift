package com.quantumrift.world;

import com.mojang.datafixers.util.Pair;
import com.quantumrift.Constants;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.SurfaceRules;
import terrablender.api.ParameterUtils;
import terrablender.api.Region;
import terrablender.api.RegionType;
import terrablender.api.Regions;
import terrablender.api.SurfaceRuleManager;

import java.util.function.Consumer;

/** The three "fused" overworld biomes, injected through TerraBlender (same code for Fabric, NeoForge and Forge). */
public final class RiftBiomes {

    public static final ResourceKey<Biome> FROSTBLOOM = key("fused_frostbloom");
    public static final ResourceKey<Biome> SCORCHWILD = key("fused_scorchwild");
    public static final ResourceKey<Biome> MIREWOOD = key("fused_mirewood");
    public static final ResourceKey<Biome> CRYSTAL_CAVERN = key("quantum_crystal_cavern");
    public static final ResourceKey<Biome> WHISPERING_HOLLOWS = key("whispering_hollows");

    /** Share of the world that uses our region (vanilla's own region weighs 10). */
    private static final int REGION_WEIGHT = 6;

    private RiftBiomes() {}

    private static ResourceKey<Biome> key(String name) {
        return ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name));
    }

    public static void register() {
        Regions.register(new RiftRegion());
        SurfaceRuleManager.addSurfaceRules(SurfaceRuleManager.RuleCategory.OVERWORLD, Constants.MOD_ID, surfaceRules());
        Constants.LOG.info("Rift biomes registered with TerraBlender");
    }

    private static final class RiftRegion extends Region {

        RiftRegion() {
            super(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "overworld"), RegionType.OVERWORLD, REGION_WEIGHT);
        }

        @Override
        public void addBiomes(Registry<Biome> registry, Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
            // Keep vanilla's layout inside this region, but swap three biomes for their fused counterparts.
            addModifiedVanillaOverworldBiomes(mapper, builder -> {
                builder.replaceBiome(Biomes.SNOWY_PLAINS, FROSTBLOOM);
                builder.replaceBiome(Biomes.DESERT, SCORCHWILD);
                builder.replaceBiome(Biomes.SWAMP, MIREWOOD);
            });
            // Two rare underground biomes, only inside our region.
            addBiome(mapper, ParameterUtils.Temperature.span(ParameterUtils.Temperature.ICY, ParameterUtils.Temperature.HOT), ParameterUtils.Humidity.span(ParameterUtils.Humidity.NEUTRAL, ParameterUtils.Humidity.WET),
                    ParameterUtils.Continentalness.span(ParameterUtils.Continentalness.MID_INLAND, ParameterUtils.Continentalness.FAR_INLAND), ParameterUtils.Erosion.span(ParameterUtils.Erosion.EROSION_0, ParameterUtils.Erosion.EROSION_6),
                    ParameterUtils.Weirdness.span(ParameterUtils.Weirdness.MID_SLICE_NORMAL_ASCENDING, ParameterUtils.Weirdness.HIGH_SLICE_NORMAL_DESCENDING),
                    ParameterUtils.Depth.span(ParameterUtils.Depth.UNDERGROUND, ParameterUtils.Depth.UNDERGROUND), 0.0F, CRYSTAL_CAVERN);
            addBiome(mapper, ParameterUtils.Temperature.span(ParameterUtils.Temperature.ICY, ParameterUtils.Temperature.NEUTRAL), ParameterUtils.Humidity.span(ParameterUtils.Humidity.ARID, ParameterUtils.Humidity.NEUTRAL),
                    ParameterUtils.Continentalness.span(ParameterUtils.Continentalness.NEAR_INLAND, ParameterUtils.Continentalness.FAR_INLAND), ParameterUtils.Erosion.span(ParameterUtils.Erosion.EROSION_0, ParameterUtils.Erosion.EROSION_6),
                    ParameterUtils.Weirdness.span(ParameterUtils.Weirdness.LOW_SLICE_NORMAL_DESCENDING, ParameterUtils.Weirdness.MID_SLICE_VARIANT_ASCENDING),
                    ParameterUtils.Depth.span(ParameterUtils.Depth.UNDERGROUND, ParameterUtils.Depth.UNDERGROUND), 0.0F, WHISPERING_HOLLOWS);
        }
    }

    private static SurfaceRules.RuleSource block(net.minecraft.world.level.block.Block b) {
        return SurfaceRules.state(b.defaultBlockState());
    }

    /** Ground made of patches from several vanilla biomes, so the terrain itself looks "altered". */
    private static SurfaceRules.RuleSource surfaceRules() {
        SurfaceRules.ConditionSource patchA = SurfaceRules.noiseCondition(Noises.SURFACE, -0.35D, 0.05D);
        SurfaceRules.ConditionSource patchB = SurfaceRules.noiseCondition(Noises.SURFACE, 0.15D, 0.55D);
        SurfaceRules.ConditionSource patchC = SurfaceRules.noiseCondition(Noises.PATCH, 0.2D, 1.0D);
        return SurfaceRules.sequence(
                SurfaceRules.ifTrue(SurfaceRules.isBiome(FROSTBLOOM), SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, SurfaceRules.sequence(
                        SurfaceRules.ifTrue(patchA, block(Blocks.SNOW_BLOCK)),
                        SurfaceRules.ifTrue(patchB, block(Blocks.PACKED_ICE)),
                        SurfaceRules.ifTrue(patchC, block(Blocks.PODZOL)),
                        block(Blocks.GRASS_BLOCK)))),
                SurfaceRules.ifTrue(SurfaceRules.isBiome(SCORCHWILD), SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, SurfaceRules.sequence(
                        SurfaceRules.ifTrue(patchA, block(Blocks.RED_SAND)),
                        SurfaceRules.ifTrue(patchB, block(Blocks.TERRACOTTA)),
                        SurfaceRules.ifTrue(patchC, block(Blocks.GRASS_BLOCK)),
                        block(Blocks.SAND)))),
                SurfaceRules.ifTrue(SurfaceRules.isBiome(MIREWOOD), SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, SurfaceRules.sequence(
                        SurfaceRules.ifTrue(patchA, block(Blocks.MUD)),
                        SurfaceRules.ifTrue(patchB, block(Blocks.MYCELIUM)),
                        SurfaceRules.ifTrue(patchC, block(Blocks.MOSS_BLOCK)),
                        block(Blocks.GRASS_BLOCK)))));
    }
}
