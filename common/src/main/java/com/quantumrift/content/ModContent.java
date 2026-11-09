package com.quantumrift.content;

public final class ModContent {

    private ModContent() {}

    /** Order matters on Fabric (immediate registration): sounds, entities, materials, items, blocks, block entities, menus, tab. */
    public static void init() {
        ModSounds.init();
        com.quantumrift.entity.ModEntities.init();
        ModArmorMaterials.init();
        ModItems.init();
        ModEggs.init();
        ModBlocks.init();
        ModBlockEntities.init();
        ModMenus.init();
        ModTabs.init();
    }
}
