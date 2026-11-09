package com.quantumrift;

import com.quantumrift.client.FusionChamberScreen;
import com.quantumrift.machine.FusionChamberMenu;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.Screenshot;
import net.minecraft.network.chat.Component;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Development-only helpers, active only when the client is started with -Dquantumrift.debug=true (see fabric/build.gradle).
 * /tmp/qr-use (content "x y z") right-clicks that block with the main hand, through the real client->server path.
 * Touch a trigger file to act: /tmp/qr-shot (content = file name) saves a screenshot, /tmp/qr-gui opens the Fusion Chamber screen, /tmp/qr-close closes it.
 */
final class DebugHooks {

    private static final Path USE = Path.of("/tmp/qr-use");
    private static final Path SHOT = Path.of("/tmp/qr-shot"), GUI = Path.of("/tmp/qr-gui"), CLOSE = Path.of("/tmp/qr-close");

    private DebugHooks() {}

    static void init() {
        if (System.getProperty("quantumrift.debug") == null) return;
        Constants.LOG.warn("Quantum Rift debug hooks enabled");
        ClientTickEvents.END_CLIENT_TICK.register(DebugHooks::tick);
    }

    private static void tick(Minecraft mc) {
        try {
            if (Files.exists(SHOT)) {
                String name = Files.readString(SHOT).trim();
                Files.delete(SHOT);
                Screenshot.grab(mc.gameDirectory, (name.isEmpty() ? "shot" : name) + ".png", mc.getMainRenderTarget(), m -> {});
            }
            if (Files.exists(USE) && mc.player != null && mc.gameMode != null) {
                String[] c = Files.readString(USE).trim().split("\\s+");
                Files.delete(USE);
                BlockPos pos = new BlockPos(Integer.parseInt(c[0]), Integer.parseInt(c[1]), Integer.parseInt(c[2]));
                mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
            }
            if (Files.exists(GUI) && mc.player != null) {
                Files.delete(GUI);
                mc.setScreen(new FusionChamberScreen(new FusionChamberMenu(0, mc.player.getInventory()), mc.player.getInventory(), Component.literal("Fusion Chamber")));
            }
            if (Files.exists(CLOSE)) {
                Files.delete(CLOSE);
                mc.setScreen(null);
            }
        } catch (Exception e) {
            Constants.LOG.error("debug hook failed", e);
        }
    }
}
