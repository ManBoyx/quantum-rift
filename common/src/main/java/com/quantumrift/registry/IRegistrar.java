package com.quantumrift.registry;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;

import java.util.function.Supplier;

/** Loader-specific registration. Implementations are discovered through ServiceLoader. */
public interface IRegistrar {

    <T> Supplier<T> register(Registry<T> registry, String name, Supplier<? extends T> factory);

    <T> Holder<T> registerHolder(Registry<T> registry, String name, Supplier<? extends T> factory);
}
