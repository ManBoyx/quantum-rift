#!/usr/bin/env python3
"""Hand-designed structures: rift_gate (overworld), return_gate (Quantum Realm arrival), sovereign_altar (floating boss island).
Every design is checked for left/right symmetry and rendered to tools/previews/ so it can be inspected.
Usage: python3 tools/gen_structures.py"""
import hashlib, json, math, os, sys
sys.path.insert(0, os.path.dirname(__file__))
from structure_lib import Voxels, render_iso, render_top, render_section
from gen_resources import MOD, ROOT, w

PREVIEW = os.path.join(os.path.dirname(__file__), "previews"); os.makedirs(PREVIEW, exist_ok=True)
OUT = lambda n: os.path.join(ROOT, f"data/{MOD}/structure/{n}.nbt")
Q = f"{MOD}:"

def hsh(x, y, z, salt=0): return int(hashlib.md5(f"{x},{y},{z},{salt}".encode()).hexdigest()[:6], 16) / 0xFFFFFF
def dist(x, z, cx, cz): return math.hypot(x - cx, z - cz)

_SYM = 24   # x index of the mirror partner of x=0 (width - 1); set by each builder


def brick(x, y, z):
    """Wall/floor brick with a little deterministic wear, identical on both sides of the symmetry axis."""
    h = hsh(min(x, _SYM - x), y, z)
    return "cracked_polished_blackstone_bricks" if h < 0.12 else ("polished_blackstone" if h < 0.2 else "polished_blackstone_bricks")

def stairs(v, x, y, z, facing, block="polished_blackstone_brick_stairs", half="bottom"):
    v.set(x, y, z, block, facing=facing, half=half, shape="straight", waterlogged=False)

def slab(v, x, y, z, block="polished_blackstone_brick_slab", kind="bottom"):
    v.set(x, y, z, block, type=kind, waterlogged=False)

def candle(v, x, y, z, color="black", n=3):
    v.set(x, y, z, f"{color}_candle", candles=n, lit=True, waterlogged=False)

def pillar(v, px, pz, shaft_top, base_y=1):
    """Column on a 3x3 plinth: bricks, banded shaft, slab capital and a soul campfire on top."""
    for dx in (-1, 0, 1):
        for dz in (-1, 0, 1):
            v.set(px + dx, base_y, pz + dz, "polished_blackstone_bricks")
            if (dx, dz) != (0, 0): slab(v, px + dx, base_y + 1, pz + dz)
    for y in range(base_y + 1, shaft_top + 1):
        v.set(px, y, pz, "chiseled_polished_blackstone" if y in (base_y + 3, shaft_top - 1) else ("blackstone" if y % 2 else "polished_blackstone"))
    for dx in (-1, 0, 1):
        for dz in (-1, 0, 1):
            if (dx, dz) == (0, 0): v.set(px, shaft_top + 1, pz, "chiseled_polished_blackstone")
            else: slab(v, px + dx, shaft_top + 1, pz + dz, kind="top")
    v.set(px, shaft_top + 2, pz, "soul_campfire", lit=True, facing="north", signal_fire=False, waterlogged=False)

def arch(v, cx, z0, plane_z, outer, inner, floor_y, portal, keystone, frame_inner="quantite_block", accent="crying_obsidian", depth=3):
    """Pointed gothic arch centred on x=cx. `outer`/`inner` map y -> (x_from, x_to). The membrane (portal block) fills the inner cells of the middle layer."""
    for y, (xa, xb) in outer.items():
        for x in range(xa, xb + 1):
            inside = y in inner and inner[y][0] <= x <= inner[y][1]
            for z in range(z0, z0 + depth):
                if inside:
                    if z == plane_z: v.set(x, y, z, portal)
                    continue
                near = any((y + dy in inner and inner[y + dy][0] <= x + dx <= inner[y + dy][1]) for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))
                if near: v.set(x, y, z, accent if (y + abs(x - cx)) % 3 == 0 else frame_inner)
                elif x == outer[y][0] or x == outer[y][1]: v.set(x, y, z, "chiseled_polished_blackstone" if y % 2 == 0 else brick(x, y, z))
                else: v.set(x, y, z, brick(x, y, z))
    top = max(outer)
    for z in range(z0, z0 + depth): v.set(cx, top, z, keystone)

# ================================================================== the rift gate
def rift_gate():
    global _SYM
    _SYM = 24
    N = 25; cx = cz = 12
    v = Voxels(N, 16, N)
    for x in range(N):
        for z in range(N):
            r = dist(x, z, cx, cz)
            if r > 12.4: continue
            name = brick(x, 0, z)
            if 5.0 <= r < 5.9: name = "blackstone"
            if 8.2 <= r < 9.2: name = "crying_obsidian"
            if r >= 11.2: name = "chiseled_polished_blackstone" if (x + z) % 2 == 0 else "polished_blackstone"
            ang = (math.degrees(math.atan2(z - cz, x - cx)) + 360) % 360
            if 2.0 < r < 8.0 and min(ang % 45, 45 - ang % 45) < 3.0 and r < 8.2 and not (8.2 <= r): name = "deepslate_tiles"
            if cx - 2 <= x <= cx + 2 and z < 9: name = "deepslate_tiles"          # processional path
            if x in (cx - 3, cx + 3) and z < 9: name = "gilded_blackstone"
            if cx - 2 <= x <= cx + 2 and 11 <= z <= 13: name = "chiseled_polished_blackstone"   # under the tunnel
            v.set(x, 0, z, name)
    # raised rim of low steps, open at the entrance
    for x in range(N):
        for z in range(N):
            r = dist(x, z, cx, cz)
            if 10.6 <= r <= 12.4 and not (abs(x - cx) <= 3 and z < 12): slab(v, x, 1, z)
    # arch at the centre
    outer = {y: (8, 16) for y in range(1, 7)}; outer.update({7: (9, 15), 8: (10, 14), 9: (11, 13), 10: (12, 12)})
    inner = {y: (10, 14) for y in range(1, 7)}; inner.update({7: (11, 13), 8: (12, 12)})
    arch(v, cx, 11, 12, outer, inner, 1, f"{Q}dormant_rift", "gilded_blackstone")
    for z in (11, 12, 13): v.set(cx, 11, z, "chiseled_polished_blackstone")
    v.set(cx, 12, 12, "polished_blackstone_wall"); v.set(cx, 13, 12, "amethyst_cluster", facing="up", waterlogged=False)
    for y in (7, 8):   # webs in the shoulders of the arch: it has been shut for a long time
        v.set(8 if y == 7 else 9, y, 12, "cobweb"); v.set(16 if y == 7 else 15, y, 12, "cobweb")
    # buttresses
    for sx_, face, mirror in ((7, "east", False), (17, "west", True)):
        for z in range(11, 14):
            v.set(sx_, 1, z, "polished_blackstone_bricks"); stairs(v, sx_, 2, z, face)
            stairs(v, sx_ - 1 if not mirror else sx_ + 1, 1, z, face)
    # nine columns: E/W, four diagonals, north axis, and two entrance pylons
    for px, pz, top in ((3, 12, 9), (21, 12, 9), (6, 6, 9), (18, 6, 9), (6, 18, 9), (18, 18, 9), (12, 21, 9), (7, 2, 5), (17, 2, 5)):
        pillar(v, px, pz, top)
    # the dais with the gate core (south of the arch) and a mirrored empty pedestal (north)
    for dz, core in ((9, True), (15, False)):
        for dx in (-1, 0, 1):
            for ddz in (-1, 0, 1): v.set(cx + dx, 1, dz + ddz, "polished_blackstone_bricks")
        v.set(cx, 1, dz, "chiseled_polished_blackstone")
        side = -1 if core else 1
        for dx in (-1, 0, 1): stairs(v, cx + dx, 1, dz + 2 * side, "south" if core else "north")   # high side towards the dais
        for ddz in (-1, 0, 1):
            stairs(v, cx - 2, 1, dz + ddz, "east"); stairs(v, cx + 2, 1, dz + ddz, "west")
        if core: v.set(cx, 2, dz, f"{Q}rift_gate_core", rods=0)
        else: v.set(cx, 2, dz, "crying_obsidian"); v.set(cx, 3, dz, "soul_lantern", hanging=False, waterlogged=False)
        for dx in (-1, 1):
            for ddz in (-1, 1): candle(v, cx + dx, 2, dz + ddz, "black" if ddz == -1 else "purple", 3)
    # lantern posts along the path
    for pz in (4, 7):
        for px in (cx - 3, cx + 3):
            v.set(px, 1, pz, "polished_blackstone_wall"); v.set(px, 2, pz, "soul_lantern", hanging=False, waterlogged=False)
    # bones and crystals at the rim: a few, in mirrored pairs
    for dx in (4, 8):
        for z in (19,):
            v.set(cx - dx, 1, z, "bone_block", axis="y"); v.set(cx + dx, 1, z, "bone_block", axis="y")
    for px in (cx - 5, cx + 5):
        v.set(px, 1, 20, "amethyst_cluster", facing="up", waterlogged=False)
    return v

# ================================================================== the return gate (Quantum Realm arrival)
SPAWN_RETURN = (7.5, 1.0, 3.5)       # keep in sync with world/GateLayout.java (generated below)
def return_gate():
    global _SYM
    _SYM = 14
    N = 15; cx = cz = 7
    v = Voxels(N, 12, N)
    for x in range(N):
        for z in range(N):
            r = dist(x, z, cx, cz)
            if r > 7.4: continue
            name = "quantum_stone" if hsh(min(x, 14 - x), 0, z) > 0.18 else "polished_blackstone_bricks"
            if 6.0 <= r < 7.0: name = "polished_blackstone_bricks"
            if 3.5 <= r < 4.4: name = "crying_obsidian"
            if abs(x - cx) <= 1 and z < 6: name = "deepslate_tiles"
            if abs(x - cx) <= 2 and 6 <= z <= 8: name = "chiseled_polished_blackstone"
            v.set(x, 0, z, Q + name if name == "quantum_stone" else name)
    outer = {y: (4, 10) for y in range(1, 6)}; outer.update({6: (5, 9), 7: (6, 8), 8: (7, 7)})
    inner = {y: (5, 9) for y in range(1, 5)}; inner.update({5: (6, 8), 6: (7, 7)})
    arch(v, cx, 6, 7, outer, inner, 1, f"{Q}rift_portal", f"{Q}singularity_block", frame_inner="quantite_block", accent=f"{Q}rift_crystal")
    for px, pz in ((2, 2), (12, 2), (2, 12), (12, 12)):
        v.set(px, 1, pz, "amethyst_block")
        for y in range(2, 5): v.set(px, y, pz, f"{Q}rift_crystal")
        v.set(px, 5, pz, "amethyst_cluster", facing="up", waterlogged=False)
    for px in (cx - 2, cx + 2):
        v.set(px, 1, 3, "polished_blackstone_wall"); v.set(px, 2, 3, "soul_lantern", hanging=False, waterlogged=False)
    return v

# ================================================================== the Sovereign's altar island
def altar_island():
    global _SYM
    _SYM = 24
    N = 25; cx = cz = 12; TOP = 10
    v = Voxels(N, 20, N)
    for y in range(0, TOP):
        rad = 2.0 + y * 1.05
        for x in range(N):
            for z in range(N):
                r = dist(x, z, cx, cz)
                if r <= rad:
                    h = hsh(min(x, 24 - x), y, z)
                    v.set(x, y, z, "crying_obsidian" if y == 0 else (Q + "quantum_stone" if h > 0.25 else "deepslate"))
    for x in range(N):
        for z in range(N):
            r = dist(x, z, cx, cz)
            if r > 11.9: continue
            name = brick(x, TOP, z)
            if 3.4 <= r < 4.5: name = "crying_obsidian"
            if 7.0 <= r < 8.0: name = "polished_blackstone"
            if r >= 10.6: name = "chiseled_polished_blackstone"
            ang = (math.degrees(math.atan2(z - cz, x - cx)) + 360) % 360
            if 2.5 < r < 10.5 and min(ang % 45, 45 - ang % 45) < 2.5: name = "crying_obsidian"     # eight-pointed star
            v.set(x, TOP, z, name)
    # three-step dais
    for dx in range(-2, 3):
        for dz in range(-2, 3): v.set(cx + dx, TOP + 1, cz + dz, "polished_blackstone_bricks")
    for dx in range(-1, 2):
        for dz in range(-1, 2): v.set(cx + dx, TOP + 2, cz + dz, "chiseled_polished_blackstone")
    for i in range(-2, 3):
        if abs(i) <= 1:
            stairs(v, cx + i, TOP + 1, cz - 3, "south"); stairs(v, cx + i, TOP + 1, cz + 3, "north")
            stairs(v, cx - 3, TOP + 1, cz + i, "east"); stairs(v, cx + 3, TOP + 1, cz + i, "west")
    v.set(cx, TOP + 3, cz, f"{Q}sovereign_altar")
    for dx, dz in ((-1, -1), (1, -1), (-1, 1), (1, 1)): candle(v, cx + dx, TOP + 3, cz + dz, "purple", 3)
    # eight crystal obelisks
    for px, pz in ((cx - 9, cz), (cx + 9, cz), (cx, cz - 9), (cx, cz + 9), (cx - 6, cz - 6), (cx + 6, cz - 6), (cx - 6, cz + 6), (cx + 6, cz + 6)):
        v.set(px, TOP + 1, pz, "amethyst_block")
        for y in range(TOP + 2, TOP + 7): v.set(px, y, pz, f"{Q}rift_crystal")
        v.set(px, TOP + 7, pz, "amethyst_cluster", facing="up", waterlogged=False)
        for dx, dz in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            if v.get(px + dx, TOP, pz + dz): v.set(px + dx, TOP + 1, pz + dz, "polished_blackstone_wall")
    for px, pz in ((cx - 5, cz), (cx + 5, cz), (cx, cz - 5), (cx, cz + 5)):
        v.set(px, TOP + 1, pz, "soul_campfire", lit=True, facing="north", signal_fire=False, waterlogged=False)
    return v

def check(name, v, ignore=()):
    bad = v.mirror_errors("x", ignore)
    print(f"{name}: size {v.size}, {len(v.cells)} blocks, left/right mirror differences: {len(bad)}")
    for b in bad[:6]: print("   ", b)
    render_iso(v, os.path.join(PREVIEW, f"{name}_iso.png"), title=name, scale=16, rot=2); render_iso(v, os.path.join(PREVIEW, f"{name}_iso_back.png"), title=name + " (back)", scale=16, rot=0); render_top(v, os.path.join(PREVIEW, f"{name}_top.png")); render_section(v, os.path.join(PREVIEW, f"{name}_front.png"))
    return not bad

def write_jigsaw(name, biomes_tag, **kw):
    w(f"data/{MOD}/worldgen/template_pool/{name}/start.json", {"fallback": "minecraft:empty", "elements": [{"weight": 1, "element": {
        "element_type": "minecraft:single_pool_element", "location": f"{MOD}:{name}", "processors": "minecraft:empty", "projection": "rigid"}}]})
    w(f"data/{MOD}/worldgen/structure/{name}.json", {"type": "minecraft:jigsaw", "biomes": biomes_tag, "step": "surface_structures", "spawn_overrides": {},
        "start_pool": f"{MOD}:{name}/start", "size": 1, "max_distance_from_center": 30, "use_expansion_hack": False, **kw})

if __name__ == "__main__":
    ok = True
    gate = rift_gate(); ok &= check("rift_gate", gate); gate.write_nbt(OUT("rift_gate"))
    ret = return_gate(); ok &= check("return_gate", ret); ret.write_nbt(OUT("return_gate"))
    alt = altar_island(); ok &= check("sovereign_altar", alt); alt.write_nbt(OUT("sovereign_altar"))
    # the overworld gate keeps the jigsaw set up by gen_world.py (heightmap-projected, beard_thin)
    write_jigsaw("sovereign_altar", f"#{MOD}:has_structure/sovereign_altar", terrain_adaptation="none", start_height={"absolute": 90})
    w(f"data/{MOD}/tags/worldgen/biome/has_structure/sovereign_altar.json", {"values": [f"{MOD}:quantum_shards", f"{MOD}:entropy_wastes", f"{MOD}:void_bloom"]})
    w(f"data/{MOD}/worldgen/structure_set/sovereign_altars.json", {"structures": [{"structure": f"{MOD}:sovereign_altar", "weight": 1}],
        "placement": {"type": "minecraft:random_spread", "spacing": 9, "separation": 4, "salt": 1177}})
    w(f"data/{MOD}/tags/worldgen/structure/on_altar_map.json", {"values": [f"{MOD}:sovereign_altar"]})
    # constants the Java side needs (arrival point and a cell that is only a portal once the gate has been built)
    sx, sy, sz = ret.size
    java = f"""package com.quantumrift.world;

/** GENERATED by tools/gen_structures.py - layout of data/quantumrift/structure/return_gate.nbt. */
public final class GateLayout {{
    public static final int SIZE_X = {sx}, SIZE_Y = {sy}, SIZE_Z = {sz};
    /** Where a player appears, relative to the template origin (in front of the gate, looking at it). */
    public static final double SPAWN_X = {SPAWN_RETURN[0]}, SPAWN_Y = {SPAWN_RETURN[1]}, SPAWN_Z = {SPAWN_RETURN[2]};
    /** A cell inside the portal membrane, relative to the template origin. */
    public static final int PORTAL_X = 7, PORTAL_Y = 1, PORTAL_Z = 7;

    private GateLayout() {{}}
}}
"""
    open(os.path.join(os.path.dirname(__file__), "..", "common/src/main/java/com/quantumrift/world/GateLayout.java"), "w").write(java)
    print("symmetry ok" if ok else "SYMMETRY PROBLEMS")
