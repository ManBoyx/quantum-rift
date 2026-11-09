package com.quantumrift.world;

import com.quantumrift.Constants;
import com.quantumrift.content.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/** Travel between an overworld rift gate and the Quantum Realm arrival gate. */
public final class QuantumTravel {

    private static final int PLATFORM_Y = 96;
    private static final ResourceLocation RETURN_GATE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "return_gate");

    private QuantumTravel() {}

    @Nullable
    public static DimensionTransition destination(ServerLevel from, Entity entity, BlockPos portalPos) {
        MinecraftServer server = from.getServer();
        if (from.dimension() == ModDimensions.QUANTUM) {
            ServerLevel overworld = server.overworld();
            BlockPos back = GateLinks.get(from).back(portalPos);
            if (back != null) {   // the gate we came through: arrive inside its membrane, like a nether portal
                return new DimensionTransition(overworld, new Vec3(back.getX() + 0.5, back.getY(), back.getZ() + 0.5), Vec3.ZERO,
                        entity.getYRot(), entity.getXRot(), DimensionTransition.PLAY_PORTAL_SOUND);
            }
            BlockPos spawn = overworld.getSharedSpawnPos();
            int y = overworld.getHeight(Heightmap.Types.MOTION_BLOCKING, spawn.getX(), spawn.getZ());
            return new DimensionTransition(overworld, new Vec3(spawn.getX() + 0.5, y, spawn.getZ() + 0.5), Vec3.ZERO,
                    entity.getYRot(), entity.getXRot(), DimensionTransition.PLAY_PORTAL_SOUND);
        }
        ServerLevel quantum = server.getLevel(ModDimensions.QUANTUM);
        if (quantum == null) return null;
        // One arrival gate per 64x64 cell so several overworld gates do not pile up platforms.
        BlockPos origin = new BlockPos(Math.floorDiv(portalPos.getX(), 64) * 64 + 32 - GateLayout.SIZE_X / 2, PLATFORM_Y, Math.floorDiv(portalPos.getZ(), 64) * 64 + 32 - GateLayout.SIZE_Z / 2);
        buildArrival(quantum, origin);
        GateLinks.get(quantum).link(origin, portalPos);
        return new DimensionTransition(quantum, new Vec3(origin.getX() + GateLayout.SPAWN_X, origin.getY() + GateLayout.SPAWN_Y, origin.getZ() + GateLayout.SPAWN_Z), Vec3.ZERO,
                0.0F, 0.0F, DimensionTransition.PLAY_PORTAL_SOUND);
    }

    /** Places the return gate template (see tools/gen_structures.py) once; clears the volume above it first. */
    private static void buildArrival(ServerLevel level, BlockPos origin) {
        BlockPos marker = origin.offset(GateLayout.PORTAL_X, GateLayout.PORTAL_Y, GateLayout.PORTAL_Z);
        if (level.getBlockState(marker).is(ModBlocks.RIFT_PORTAL.get())) return;
        for (int x = 0; x < GateLayout.SIZE_X; x++) {
            for (int z = 0; z < GateLayout.SIZE_Z; z++) {
                for (int y = 1; y < GateLayout.SIZE_Y + 2; y++) level.setBlock(origin.offset(x, y, z), Blocks.AIR.defaultBlockState(), 2);
            }
        }
        level.getStructureManager().get(RETURN_GATE).ifPresentOrElse(
                template -> template.placeInWorld(level, origin, origin, new StructurePlaceSettings(), level.getRandom(), 2),
                () -> Constants.LOG.error("Missing structure template {}", RETURN_GATE));
    }
}
