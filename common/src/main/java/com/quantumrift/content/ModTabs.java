package com.quantumrift.content;

import com.quantumrift.registry.Registrar;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public final class ModTabs {

    public static final Supplier<CreativeModeTab> MAIN = Registrar.register(BuiltInRegistries.CREATIVE_MODE_TAB, "main",
            () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                    .title(Component.translatable("itemGroup.quantumrift"))
                    .icon(() -> new ItemStack(ModItems.INTERDIMENSIONAL_ROD.get()))
                    .displayItems((params, out) -> ModItems.ALL.forEach(i -> out.accept(i.get())))
                    .build());

    private ModTabs() {}

    public static void init() {}
}
