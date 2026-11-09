package com.quantumrift;

import com.quantumrift.client.ClientRenderers;
import com.quantumrift.client.ClientScreens;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import com.quantumrift.content.ModBlocks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.RenderType;

public class QuantumRiftClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        DebugHooks.init();
        ClientRenderers.registerLayers((layer, definition) -> net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer, definition::get));
        ClientRenderers.register(EntityRendererRegistry::register);
        ClientScreens.register(new ClientScreens.Sink() {
            @Override
            public <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void register(MenuType<? extends M> type, ClientScreens.Constructor<M, U> constructor) {
                MenuScreens.register(type, constructor::create);
            }
        });
        // NeoForge/Forge read "render_type" from the block model json instead.
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.RIFT_PORTAL.get(), RenderType.translucent());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.DORMANT_RIFT.get(), RenderType.translucent());
    }
}
