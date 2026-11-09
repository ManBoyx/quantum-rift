package com.quantumrift.content;

import com.quantumrift.machine.FusionChamberMenu;
import com.quantumrift.registry.Registrar;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

import java.util.function.Supplier;

public final class ModMenus {

    public static final Supplier<MenuType<FusionChamberMenu>> FUSION_CHAMBER = reg("fusion_chamber",
            () -> new MenuType<>(FusionChamberMenu::new, FeatureFlags.DEFAULT_FLAGS));

    private ModMenus() {}

    @SuppressWarnings("unchecked")
    private static <M extends net.minecraft.world.inventory.AbstractContainerMenu> Supplier<MenuType<M>> reg(String name, Supplier<MenuType<M>> factory) {
        Supplier<MenuType<?>> s = Registrar.register(BuiltInRegistries.MENU, name, factory);
        return () -> (MenuType<M>) s.get();
    }

    public static void init() {}
}
