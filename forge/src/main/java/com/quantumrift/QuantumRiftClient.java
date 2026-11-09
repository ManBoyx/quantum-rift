package com.quantumrift;

import com.quantumrift.client.ClientRenderers;
import com.quantumrift.client.ClientScreens;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Client-only subscriber (keeps client classes away from dedicated servers). */
@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class QuantumRiftClient {

    private QuantumRiftClient() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent e) {
        e.enqueueWork(() -> ClientScreens.register(new ClientScreens.Sink() {
            @Override
            public <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void register(MenuType<? extends M> type, ClientScreens.Constructor<M, U> constructor) {
                MenuScreens.register(type, constructor::create);
            }
        }));
    }

    @SubscribeEvent
    public static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions e) {
        ClientRenderers.registerLayers(e::registerLayerDefinition);
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers e) {
        ClientRenderers.register(new ClientRenderers.Sink() {
            @Override
            public <T extends Entity> void register(EntityType<? extends T> type, EntityRendererProvider<T> provider) {
                e.registerEntityRenderer(type, provider);
            }
        });
    }
}
