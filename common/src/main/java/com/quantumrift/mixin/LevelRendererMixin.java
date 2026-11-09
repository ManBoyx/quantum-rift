package com.quantumrift.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.quantumrift.world.ModDimensions;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Draws the black hole (event horizon + glowing accretion disk) in the sky of the Quantum Realm. */
@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

    @Shadow private ClientLevel level;

    @Inject(method = "renderSky", at = @At("RETURN"))
    private void quantumrift$blackHole(Matrix4f frustumMatrix, Matrix4f projectionMatrix, float partialTick, Camera camera,
                                       boolean isFoggy, Runnable skyFogSetup, CallbackInfo ci) {
        if (this.level == null || this.level.dimension() != ModDimensions.QUANTUM) return;

        float time = (this.level.getGameTime() + partialTick) / 20.0F;
        Vector3f dir = new Vector3f(0.35F, 0.62F, -0.70F).normalize();
        Vector3f right = new Vector3f(dir).cross(0.0F, 1.0F, 0.0F).normalize();
        Vector3f up = new Vector3f(right).cross(dir).normalize();
        final float dist = 100.0F;

        RenderSystem.depthMask(false);
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        // Soft purple glow around the hole (additive).
        RenderSystem.blendFunc(com.mojang.blaze3d.platform.GlStateManager.SourceFactor.SRC_ALPHA, com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE);
        disc(frustumMatrix, dir, right, up, dist, 48.0F, 0.20F, 0.05F, 0.35F, 0.20F, 0.0F);

        // Tilted accretion disk with an orbiting hot spot.
        final int seg = 96;
        BufferBuilder ring = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        for (int i = 0; i <= seg; i++) {
            double a = Math.PI * 2.0 * i / seg;
            float heat = (float) (0.55 + 0.45 * Math.cos(a - time * 0.9));
            point(ring, frustumMatrix, dir, right, up, dist, (float) Math.cos(a) * 12.0F, (float) Math.sin(a) * 12.0F * 0.28F, 1.0F, 0.85F + 0.15F * heat, 0.55F + 0.35F * heat, 0.95F * heat + 0.2F);
            point(ring, frustumMatrix, dir, right, up, dist, (float) Math.cos(a) * 34.0F, (float) Math.sin(a) * 34.0F * 0.28F, 0.45F, 0.35F + 0.4F * heat, 0.15F, 0.0F);
        }
        BufferUploader.drawWithShader(ring.buildOrThrow());

        // Event horizon: opaque black disc, with a thin bright photon ring.
        RenderSystem.defaultBlendFunc();
        disc(frustumMatrix, dir, right, up, dist, 9.5F, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F);
        RenderSystem.blendFunc(com.mojang.blaze3d.platform.GlStateManager.SourceFactor.SRC_ALPHA, com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE);
        BufferBuilder photon = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        for (int i = 0; i <= seg; i++) {
            double a = Math.PI * 2.0 * i / seg;
            point(photon, frustumMatrix, dir, right, up, dist, (float) Math.cos(a) * 9.5F, (float) Math.sin(a) * 9.5F, 1.0F, 1.0F, 0.9F, 0.9F);
            point(photon, frustumMatrix, dir, right, up, dist, (float) Math.cos(a) * 11.5F, (float) Math.sin(a) * 11.5F, 0.6F, 0.4F, 0.9F, 0.0F);
        }
        BufferUploader.drawWithShader(photon.buildOrThrow());

        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
    }

    /** Filled disc: centre colour (r,g,b,aCentre) fading to aEdge. */
    private static void disc(Matrix4f m, Vector3f dir, Vector3f right, Vector3f up, float dist, float radius,
                             float r, float g, float b, float aCentre, float aEdge) {
        BufferBuilder bb = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        point(bb, m, dir, right, up, dist, 0.0F, 0.0F, r, g, b, aCentre);
        for (int i = 0; i <= 48; i++) {
            double a = Math.PI * 2.0 * i / 48;
            point(bb, m, dir, right, up, dist, (float) Math.cos(a) * radius, (float) Math.sin(a) * radius, r, g, b, aEdge);
        }
        BufferUploader.drawWithShader(bb.buildOrThrow());
    }

    private static void point(BufferBuilder bb, Matrix4f m, Vector3f dir, Vector3f right, Vector3f up, float dist,
                              float x, float y, float r, float g, float b, float a) {
        bb.addVertex(m, dir.x * dist + right.x * x + up.x * y, dir.y * dist + right.y * x + up.y * y, dir.z * dist + right.z * x + up.z * y)
                .setColor(r, g, b, a);
    }
}
