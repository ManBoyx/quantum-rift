package com.quantumrift.client;

import com.quantumrift.Constants;
import com.quantumrift.entity.VoidSovereign;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

public class VoidSovereignRenderer extends MobRenderer<VoidSovereign, VoidSovereignModel> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/entity/void_sovereign.png");
    private static final RenderType GLOW = RenderType.eyes(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/entity/void_sovereign_glow.png"));

    public VoidSovereignRenderer(EntityRendererProvider.Context context) {
        super(context, new VoidSovereignModel(context.bakeLayer(VoidSovereignModel.LAYER)), 1.6F);
        addLayer(new EyesLayer<>(this) {
            @Override
            public RenderType renderType() {
                return GLOW;
            }
        });
    }

    @Override
    public ResourceLocation getTextureLocation(VoidSovereign entity) {
        return TEXTURE;
    }
}
