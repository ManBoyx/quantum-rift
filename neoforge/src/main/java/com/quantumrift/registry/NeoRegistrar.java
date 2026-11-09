package com.quantumrift.registry;

import com.quantumrift.Constants;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class NeoRegistrar implements IRegistrar {

    private static final Map<ResourceKey<?>, DeferredRegister<?>> REGISTERS = new LinkedHashMap<>();

    @SuppressWarnings("unchecked")
    private static <T> DeferredRegister<T> of(Registry<T> registry) {
        return (DeferredRegister<T>) REGISTERS.computeIfAbsent(registry.key(), k -> DeferredRegister.create(registry, Constants.MOD_ID));
    }

    @Override
    public <T> Supplier<T> register(Registry<T> registry, String name, Supplier<? extends T> factory) {
        return of(registry).register(name, factory);
    }

    @Override
    public <T> Holder<T> registerHolder(Registry<T> registry, String name, Supplier<? extends T> factory) {
        return of(registry).register(name, factory);
    }

    /** Called from the mod constructor once common content has been declared. */
    public static void flush(IEventBus bus) {
        REGISTERS.values().forEach(r -> r.register(bus));
    }
}
