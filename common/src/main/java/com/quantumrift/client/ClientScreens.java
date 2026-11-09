package com.quantumrift.client;

import com.quantumrift.content.ModMenus;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public final class ClientScreens {

    /** Vanilla's MenuScreens.ScreenConstructor is not public in the common (vanilla-only) build, so we mirror it. */
    public interface Constructor<M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> {
        U create(M menu, Inventory inventory, Component title);
    }

    public interface Sink {
        <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void register(MenuType<? extends M> type, Constructor<M, U> constructor);
    }

    private ClientScreens() {}

    public static void register(Sink sink) {
        sink.register(ModMenus.FUSION_CHAMBER.get(), FusionChamberScreen::new);
    }
}
