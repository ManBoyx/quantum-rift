package com.quantumrift.world;

import com.quantumrift.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public final class ModDimensions {

    public static final ResourceKey<Level> QUANTUM = ResourceKey.create(Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "quantum_realm"));

    private ModDimensions() {}
}
