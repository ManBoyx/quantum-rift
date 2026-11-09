package com.quantumrift.client;

import com.quantumrift.Constants;
import com.quantumrift.machine.FusionChamberMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class FusionChamberScreen extends AbstractContainerScreen<FusionChamberMenu> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/fusion_chamber.png");
    private static final int ARROW_X = 124, ARROW_Y = 36, ARROW_W = 24, ARROW_H = 17;

    public FusionChamberScreen(FusionChamberMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int w = (int) (ARROW_W * menu.progress());
        if (w > 0) graphics.blit(TEXTURE, leftPos + ARROW_X, topPos + ARROW_Y, 176, 0, w, ARROW_H);
    }
}
