package com.quantumrift.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/** Remembers which overworld rift gate each Quantum Realm arrival gate (one per 64x64 cell) leads back to. Stored in the Quantum Realm. */
public class GateLinks extends SavedData {

    private static final String NAME = "quantumrift_gates";
    private final Map<Long, BlockPos> links = new HashMap<>();

    public static GateLinks get(ServerLevel quantum) {
        return quantum.getDataStorage().computeIfAbsent(new SavedData.Factory<>(GateLinks::new, GateLinks::load, null), NAME);
    }

    private static long cell(BlockPos pos) {
        return ((long) Math.floorDiv(pos.getX(), 64) << 32) ^ (Math.floorDiv(pos.getZ(), 64) & 0xFFFFFFFFL);
    }

    public void link(BlockPos arrival, BlockPos overworldGate) {
        links.put(cell(arrival), overworldGate.immutable());
        setDirty();
    }

    @Nullable
    public BlockPos back(BlockPos arrival) {
        return links.get(cell(arrival));
    }

    private static GateLinks load(CompoundTag tag, HolderLookup.Provider registries) {
        GateLinks data = new GateLinks();
        ListTag list = tag.getList("links", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag e = list.getCompound(i);
            data.links.put(e.getLong("cell"), new BlockPos(e.getInt("x"), e.getInt("y"), e.getInt("z")));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        links.forEach((cell, pos) -> {
            CompoundTag e = new CompoundTag();
            e.putLong("cell", cell);
            e.putInt("x", pos.getX()); e.putInt("y", pos.getY()); e.putInt("z", pos.getZ());
            list.add(e);
        });
        tag.put("links", list);
        return tag;
    }
}
