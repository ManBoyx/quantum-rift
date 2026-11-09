package com.quantumrift.registry;

import com.quantumrift.Constants;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import com.mojang.datafixers.util.Either;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ForgeRegistrar implements IRegistrar {

    private static final Map<ResourceKey<?>, DeferredRegister<?>> REGISTERS = new LinkedHashMap<>();

    @SuppressWarnings("unchecked")
    private static <T> DeferredRegister<T> of(Registry<T> registry) {
        return (DeferredRegister<T>) REGISTERS.computeIfAbsent(registry.key(), k -> DeferredRegister.create(registry.key(), Constants.MOD_ID));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Supplier<T> register(Registry<T> registry, String name, Supplier<? extends T> factory) {
        return (Supplier<T>) of(registry).register(name, factory);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Holder<T> registerHolder(Registry<T> registry, String name, Supplier<? extends T> factory) {
        return new LazyHolder<>((RegistryObject<T>) of(registry).register(name, factory));
    }

    /** Called from the mod constructor once common content has been declared. */
    public static void flush(IEventBus bus) {
        REGISTERS.values().forEach(r -> r.register(bus));
    }

    /** Forge's RegistryObject is not a Holder; vanilla needs one for armor materials. */
    private record LazyHolder<T>(RegistryObject<T> object) implements Holder<T> {

        private Holder<T> delegate() {
            return object.getHolder().orElseThrow(() -> new IllegalStateException("Not registered yet: " + object.getId()));
        }

        @Override public T value() { return object.get(); }
        @Override public boolean isBound() { return object.isPresent(); }
        @Override public boolean is(ResourceLocation id) { return object.getId().equals(id); }
        @Override public boolean is(ResourceKey<T> key) { return object.getKey().equals(key); }
        @Override public boolean is(Predicate<ResourceKey<T>> predicate) { return predicate.test(object.getKey()); }
        @Override public boolean is(Holder<T> other) { return other.is(object.getId()); }
        @Override public boolean is(TagKey<T> tag) { return delegate().is(tag); }
        @Override public Stream<TagKey<T>> tags() { return delegate().tags(); }
        @Override public Either<ResourceKey<T>, T> unwrap() { return Either.left(object.getKey()); }
        @Override public Optional<ResourceKey<T>> unwrapKey() { return Optional.of(object.getKey()); }
        @Override public Kind kind() { return Kind.REFERENCE; }
        @Override public boolean canSerializeIn(HolderOwner<T> owner) { return delegate().canSerializeIn(owner); }
    }
}
