package com.quantumrift;

import com.quantumrift.client.ClientRenderers;
import com.quantumrift.client.ClientScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Client-only entry point (keeps client classes away from dedicated servers). */
@Mod(value = Constants.MOD_ID, dist = Dist.CLIENT)
public class QuantumRiftClient {

    public QuantumRiftClient(IEventBus eventBus) {
        eventBus.addListener((EntityRenderersEvent.RegisterLayerDefinitions e) -> ClientRenderers.registerLayers(e::registerLayerDefinition));
        eventBus.addListener((RegisterMenuScreensEvent e) -> ClientScreens.register(new ClientScreens.Sink() {
            @Override
            public <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void register(MenuType<? extends M> type, ClientScreens.Constructor<M, U> constructor) {
                e.register(type, constructor::create);
            }
        }));
        eventBus.addListener((EntityRenderersEvent.RegisterRenderers e) -> ClientRenderers.register(new ClientRenderers.Sink() {
            @Override
            public <T extends Entity> void register(EntityType<? extends T> type, EntityRendererProvider<T> provider) {
                e.registerEntityRenderer(type, provider);
            }
        }));
    }
}
