package com.quantumrift.registry;

import com.quantumrift.Constants;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public class FabricRegistrar implements IRegistrar {

    @Override
    public <T> Supplier<T> register(Registry<T> registry, String name, Supplier<? extends T> factory) {
        T value = Registry.register(registry, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name), factory.get());
        return () -> value;
    }

    @Override
    public <T> Holder<T> registerHolder(Registry<T> registry, String name, Supplier<? extends T> factory) {
        return Registry.registerForHolder(registry, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name), factory.get());
    }
}
