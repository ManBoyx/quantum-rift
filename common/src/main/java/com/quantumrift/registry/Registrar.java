package com.quantumrift.registry;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;

import java.util.ServiceLoader;
import java.util.function.Supplier;

public final class Registrar {

    private static final IRegistrar IMPL = ServiceLoader.load(IRegistrar.class).findFirst()
            .orElseThrow(() -> new IllegalStateException("No IRegistrar implementation found"));

    private Registrar() {}

    public static <T> Supplier<T> register(Registry<T> registry, String name, Supplier<? extends T> factory) {
        return IMPL.register(registry, name, factory);
    }

    public static <T> Holder<T> registerHolder(Registry<T> registry, String name, Supplier<? extends T> factory) {
        return IMPL.registerHolder(registry, name, factory);
    }
}
