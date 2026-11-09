#!/usr/bin/env python3
"""Omnium, upgrade modules, Prismatic gear, Quantinium items, machines (textures, models, GUI, recipes, loot, tags, worldgen).
Run after gen_resources.py and gen_world.py."""
import colorsys, io, json, math, os, sys
sys.path.insert(0, os.path.dirname(__file__))
import numpy as np
from PIL import Image, ImageDraw
from gen_resources import MOD, ROOT, Z, w, gradient, vanilla_img, save_tex, item, ore_mask
from gen_world import QUANTUM

PEARL = [(40, 44, 90), (110, 130, 215), (200, 210, 255), (255, 245, 255)]
STEEL = [(10, 10, 16), (36, 38, 52), (80, 84, 106), (150, 156, 184)]
ORE_COLORS = {  # inlay colours of the Prismatic gear: one per ore of the game + ours, in a fixed visual order
    "coal": (58, 58, 66), "copper": (224, 124, 70), "iron": (222, 206, 198), "gold": (252, 220, 70), "redstone": (214, 30, 30),
    "lapis": (38, 82, 206), "diamond": (84, 236, 224), "emerald": (36, 214, 96), "netherite": (112, 78, 78),
    "quantite": (80, 210, 205), "singularity": (150, 70, 225), "omnium": (205, 215, 255)}
MODULES = list(ORE_COLORS)

def shade(c, k): return tuple(int(max(0, min(255, v * k))) for v in c)

def lerp(a, b, t): return tuple(int(a[i] + (b[i] - a[i]) * t) for i in range(3))

# ------------------------------------------------------------------ item / block textures
def draw_core():
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0)); d = ImageDraw.Draw(im)
    d.rectangle([2, 2, 13, 13], fill=(40, 44, 60, 255), outline=(120, 130, 160, 255))
    for x in (4, 7, 10): d.line([x, 3, x, 12], fill=(210, 170, 60, 255))
    for y in (5, 9): d.line([3, y, 12, y], fill=(210, 170, 60, 255))
    d.rectangle([6, 6, 9, 9], fill=(90, 220, 220, 255)); d.point([7, 7], fill=(255, 255, 255, 255))
    return im

def draw_module(color):
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0)); d = ImageDraw.Draw(im)
    d.rectangle([1, 1, 14, 14], fill=(34, 36, 50, 255), outline=(150, 158, 190, 255))
    d.rectangle([2, 2, 13, 13], outline=(60, 64, 86, 255))
    for (x, y) in ((3, 3), (12, 3), (3, 12), (12, 12)): d.point([x, y], fill=(210, 170, 60, 255))
    # faceted gem in the middle, coloured by the ore
    d.polygon([(8, 4), (12, 8), (8, 12), (4, 8)], fill=color + (255,), outline=shade(color, 0.55) + (255,))
    d.polygon([(8, 4), (12, 8), (8, 8)], fill=shade(color, 1.25) + (255,))
    d.polygon([(4, 8), (8, 8), (8, 12)], fill=shade(color, 0.75) + (255,))
    d.point([7, 6], fill=(255, 255, 255, 255))
    return im

def draw_sigil():
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0)); d = ImageDraw.Draw(im)
    d.ellipse([1, 1, 14, 14], fill=(24, 8, 44, 255), outline=(170, 90, 240, 255))
    d.ellipse([3, 3, 12, 12], outline=(110, 50, 190, 255))
    d.polygon([(8, 2), (10, 8), (8, 14), (6, 8)], fill=(210, 150, 255, 255)); d.polygon([(2, 8), (8, 6), (14, 8), (8, 10)], fill=(150, 80, 230, 255))
    d.point([8, 8], fill=(255, 255, 255, 255))
    return im

def draw_heart():
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0)); px = im.load()
    rows = ["..XXX..XXX....", ".XXXXXXXXXXX..", "XXXXXXXXXXXXX.", "XXXXXXXXXXXXX.", "XXXXXXXXXXXXX.", ".XXXXXXXXXXX..", "..XXXXXXXXX...", "...XXXXXXX....", "....XXXXX.....", ".....XXX......", "......X......."]
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch == "X":
                t = (x * 0.4 + y * 0.9) / 12
                px[x + 1, y + 2] = lerp((60, 8, 50), (200, 60, 210), min(1, max(0, 1 - t))) + (255,)
    for (x, y) in ((4, 4), (5, 4), (4, 5)): px[x, y] = (255, 210, 255, 255)
    for (x, y) in ((8, 6), (9, 7), (7, 8)): px[x, y] = (90, 240, 235, 255)  # glowing veins
    return im

def rainbow_sheen(img):
    a = np.array(img).astype(np.float32); out = a.copy()
    h, wd = a.shape[:2]
    for y in range(h):
        for x in range(wd):
            if a[y, x, 3] == 0: continue
            lum = (0.299 * a[y, x, 0] + 0.587 * a[y, x, 1] + 0.114 * a[y, x, 2]) / 255
            r, g, b = colorsys.hsv_to_rgb(((x + y) / (h + wd)) % 1.0, 0.75, 0.30 + 0.70 * lum)
            out[y, x, :3] = (r * 255, g * 255, b * 255)
    return Image.fromarray(out.clip(0, 255).astype(np.uint8), "RGBA")

def inlay(img, tile_w=3, tile_h=3, step=None, thresh=0.52, texture_space=False):
    """Dark gunmetal base + ore-coloured inlays on the lit parts: every ore of the game shows up, in a fixed order."""
    base = gradient(img, STEEL); a = np.array(base).astype(np.float32)
    src = np.array(img).astype(np.float32); lum = (0.299 * src[..., 0] + 0.587 * src[..., 1] + 0.114 * src[..., 2]) / 255.0
    op = src[..., 3] > 0
    lo, hi = lum[op].min(), lum[op].max(); ln = (lum - lo) / max(1e-3, hi - lo)
    cols = list(ORE_COLORS.values()); n = len(cols); H, W = ln.shape
    for y in range(H):
        for x in range(W):
            if not op[y, x] or ln[y, x] < thresh: continue
            idx = (((x // tile_w) + (y // tile_h) * 2) if texture_space else ((x + y) // 2)) % n
            a[y, x, :3] = shade(cols[idx], 0.55 + 0.75 * ln[y, x])
    return Image.fromarray(a.clip(0, 255).astype(np.uint8), "RGBA")

def armor_layer(img):
    """Gunmetal armour with thin ore-coloured inlays on the brightest edges only; the colour changes slowly across the texture."""
    base = gradient(img, STEEL); a = np.array(base).astype(np.float32)
    src = np.array(img).astype(np.float32); lum = (0.299 * src[..., 0] + 0.587 * src[..., 1] + 0.114 * src[..., 2]) / 255.0
    op = src[..., 3] > 0; lo, hi = lum[op].min(), lum[op].max(); ln = (lum - lo) / max(1e-3, hi - lo)
    cols = list(ORE_COLORS.values()); n = len(cols); H, W = ln.shape
    for y in range(H):
        for x in range(W):
            if not op[y, x] or ln[y, x] < 0.66: continue
            c = cols[((x + y // 2) // 3) % n]
            a[y, x, :3] = [0.25 * a[y, x, i] + 0.75 * c[i] * (0.6 + 0.5 * ln[y, x]) for i in range(3)]
    return Image.fromarray(a.clip(0, 255).astype(np.uint8), "RGBA")

def omnium_ore(host, ore_src, mask_base):
    """Host rock with veins of pearly metal; each connected vein gets one ore colour (copper, gold, ... ) so it reads as 'all ores in one'."""
    host_im = vanilla_img("block/" + host); ore = vanilla_img("block/" + ore_src)
    m = ore_mask(ore, vanilla_img("block/" + mask_base)); out = np.array(host_im).astype(np.float32)
    lum = (0.299 * np.array(ore)[..., 0] + 0.587 * np.array(ore)[..., 1] + 0.114 * np.array(ore)[..., 2]) / 255.0
    flecks = [ORE_COLORS[k] for k in ("copper", "gold", "redstone", "lapis", "emerald", "diamond", "singularity")]
    label = -np.ones((16, 16), int); n = 0
    for y in range(16):
        for x in range(16):
            if m[y, x] and label[y, x] < 0:
                stack = [(x, y)]; label[y, x] = n
                while stack:
                    cx, cy = stack.pop()
                    for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                        nx, ny = cx + dx, cy + dy
                        if 0 <= nx < 16 and 0 <= ny < 16 and m[ny, nx] and label[ny, nx] < 0: label[ny, nx] = n; stack.append((nx, ny))
                n += 1
    for y in range(16):
        for x in range(16):
            if m[y, x]:
                c = flecks[(label[y, x] + x // 6 + (y // 6) * 2) % len(flecks)]; k = 0.55 + 0.7 * lum[y, x]
                pearl = [float(np.interp(lum[y, x], np.linspace(0, 1, 4), [q[i] for q in PEARL])) for i in range(3)]
                out[y, x, :3] = [0.25 * pearl[i] + 0.75 * c[i] * k for i in range(3)]
    return Image.fromarray(out.clip(0, 255).astype(np.uint8), "RGBA")

def panel(size, base, light, dark, lights=None):
    im = Image.new("RGBA", (size, size), base + (255,)); d = ImageDraw.Draw(im)
    d.rectangle([0, 0, size - 1, size - 1], outline=dark + (255,)); d.line([1, 1, size - 2, 1], fill=light + (255,)); d.line([1, 1, 1, size - 2], fill=light + (255,))
    for (x0, y0, x1, y1, c) in (lights or []): d.rectangle([x0, y0, x1, y1], fill=c + (255,))
    return im

def machine_textures():
    metal = [(14, 12, 24), (44, 38, 70), (96, 84, 140), (170, 160, 210)]
    base = gradient(vanilla_img("block/iron_block"), metal)
    teal, glow = (60, 230, 220), (200, 255, 250)
    # fusion chamber: grid of 12 sockets on top, light strips on the sides
    top = base.copy(); d = ImageDraw.Draw(top)
    for r in range(3):
        for c in range(4):
            x, y = 2 + c * 3, 3 + r * 3
            d.rectangle([x, y, x + 1, y + 1], fill=list(ORE_COLORS.values())[r * 4 + c] + (255,))
    d.rectangle([0, 0, 15, 15], outline=(20, 16, 36, 255)); d.line([2, 13, 13, 13], fill=teal + (255,))
    side = base.copy(); d = ImageDraw.Draw(side); d.rectangle([0, 0, 15, 15], outline=(20, 16, 36, 255))
    d.rectangle([3, 3, 12, 12], outline=(20, 16, 36, 255), fill=(30, 26, 52, 255)); d.line([4, 5, 11, 5], fill=teal + (255,)); d.line([4, 8, 11, 8], fill=(150, 80, 230, 255)); d.line([4, 11, 11, 11], fill=teal + (255,))
    save_tex("block/fusion_chamber_top", top); save_tex("block/fusion_chamber_side", side); save_tex("block/fusion_chamber_bottom", base)
    # matter collector: funnel ring on top, grille on the sides
    top = base.copy(); d = ImageDraw.Draw(top); d.rectangle([0, 0, 15, 15], outline=(20, 16, 36, 255))
    d.ellipse([3, 3, 12, 12], fill=(8, 6, 18, 255), outline=teal + (255,)); d.ellipse([6, 6, 9, 9], fill=(120, 60, 200, 255))
    side = base.copy(); d = ImageDraw.Draw(side); d.rectangle([0, 0, 15, 15], outline=(20, 16, 36, 255))
    for y in (4, 7, 10): d.line([3, y, 12, y], fill=(8, 6, 18, 255)); d.line([3, y + 1, 12, y + 1], fill=(90, 70, 150, 255))
    save_tex("block/matter_collector_top", top); save_tex("block/matter_collector_side", side); save_tex("block/matter_collector_bottom", base)
    # quantum vault: heavy door with a lock
    side = base.copy(); d = ImageDraw.Draw(side); d.rectangle([0, 0, 15, 15], outline=(20, 16, 36, 255)); d.rectangle([2, 2, 13, 13], outline=(60, 50, 100, 255))
    d.ellipse([5, 5, 10, 10], fill=(24, 20, 44, 255), outline=(250, 210, 80, 255)); d.point([7, 7], fill=teal + (255,)); d.point([8, 8], fill=teal + (255,))
    top = base.copy(); d = ImageDraw.Draw(top); d.rectangle([0, 0, 15, 15], outline=(20, 16, 36, 255)); d.rectangle([2, 2, 13, 13], outline=(60, 50, 100, 255))
    save_tex("block/quantum_vault_side", side); save_tex("block/quantum_vault_top", top); save_tex("block/quantum_vault_bottom", base)
    # quantum furnace: recolour of the vanilla furnace
    for n in ("furnace_front", "furnace_front_on", "furnace_side", "furnace_top"):
        im = vanilla_img("block/" + n); save_tex("block/quantum_" + n, gradient(im, [(14, 8, 28), (60, 30, 110), (130, 80, 210), (230, 190, 255)]))

def textures():
    save_tex("item/raw_omnium", gradient(vanilla_img("item/raw_iron"), PEARL)); save_tex("item/omnium_ingot", gradient(vanilla_img("item/iron_ingot"), PEARL))
    save_tex("item/upgrade_module_core", draw_core()); save_tex("item/sovereign_sigil", draw_sigil()); save_tex("item/sovereign_heart", draw_heart())
    for ore, col in ORE_COLORS.items(): save_tex(f"item/{ore}_upgrade_module", draw_module(col))
    save_tex("item/prismatic_template", rainbow_sheen(vanilla_img("item/netherite_upgrade_smithing_template")))
    coal = gradient(vanilla_img("item/coal"), [(4, 22, 32), (10, 90, 110), (60, 220, 220), (230, 255, 255)])
    save_tex("item/quantinium_coal", coal)
    steak = gradient(vanilla_img("item/cooked_beef"), [(34, 12, 10), (120, 52, 32), (205, 118, 74), (255, 210, 160)]).copy(); px = steak.load()
    for (x, y) in ((5, 6), (9, 5), (7, 9), (11, 8), (4, 10)):
        if px[x, y][3]: px[x, y] = (90, 245, 240, 255)
    save_tex("item/quantinium_steak", steak)
    for part in ("sword", "pickaxe", "axe", "shovel", "hoe", "helmet", "chestplate", "leggings", "boots"):
        save_tex(f"item/prismatic_{part}", inlay(vanilla_img(f"item/netherite_{part}"), tile_w=2, tile_h=2))
    for n in (1, 2):
        save_tex(f"models/armor/prismatic_layer_{n}", armor_layer(vanilla_img(f"models/armor/netherite_layer_{n}")))
    save_tex("block/omnium_ore", omnium_ore("stone", "iron_ore", "stone")); save_tex("block/deepslate_omnium_ore", omnium_ore("deepslate", "deepslate_iron_ore", "deepslate"))
    save_tex("block/nether_omnium_ore", omnium_ore("netherrack", "iron_ore", "stone")); save_tex("block/end_omnium_ore", omnium_ore("end_stone", "iron_ore", "stone"))
    save_tex("block/omnium_block", gradient(vanilla_img("block/iron_block"), PEARL))
    crystal = gradient(vanilla_img("block/amethyst_block"), [(8, 40, 56), (24, 120, 140), (90, 220, 225), (240, 255, 255)]).copy(); px = crystal.load()
    for (x, y) in ((3, 3), (4, 3), (3, 4), (11, 9), (12, 9), (7, 12)): px[x, y] = (255, 255, 255, 255)
    save_tex("block/rift_crystal", crystal)
    machine_textures()

def gui():
    W = H = 256; im = Image.new("RGBA", (W, H), (0, 0, 0, 0)); d = ImageDraw.Draw(im)
    d.rectangle([0, 0, 175, 165], fill=(198, 198, 198, 255), outline=(0, 0, 0, 255)); d.line([1, 1, 174, 1], fill=(255, 255, 255, 255)); d.line([1, 1, 1, 164], fill=(255, 255, 255, 255))
    d.line([2, 164, 174, 164], fill=(85, 85, 85, 255)); d.line([174, 2, 174, 164], fill=(85, 85, 85, 255))
    from_menu = dict(GRID_X=17, GRID_Y=18, CELL=18, HEART_X=104, HEART_Y=36, OUT_X=152, OUT_Y=36)  # keep in sync with FusionChamberMenu
    def slot(x, y, tint=None):
        d.rectangle([x - 1, y - 1, x + 16, y + 16], fill=(139, 139, 139, 255)); d.line([x - 1, y - 1, x + 16, y - 1], fill=(55, 55, 55, 255)); d.line([x - 1, y - 1, x - 1, y + 16], fill=(55, 55, 55, 255))
        d.line([x, y + 16, x + 16, y + 16], fill=(255, 255, 255, 255)); d.line([x + 16, y, x + 16, y + 16], fill=(255, 255, 255, 255))
        if tint:   # faint ghost gem showing which module belongs here
            d.polygon([(x + 8, y + 3), (x + 13, y + 8), (x + 8, y + 13), (x + 3, y + 8)], fill=tint + (110,), outline=shade(tint, 0.6) + (150,))
    for i, col in enumerate(ORE_COLORS.values()):
        slot(from_menu["GRID_X"] + (i % 4) * from_menu["CELL"], from_menu["GRID_Y"] + (i // 4) * from_menu["CELL"], col)
    slot(from_menu["HEART_X"], from_menu["HEART_Y"], (190, 60, 200)); slot(from_menu["OUT_X"], from_menu["OUT_Y"])
    d.rectangle([from_menu["OUT_X"] - 4, from_menu["OUT_Y"] - 4, from_menu["OUT_X"] + 19, from_menu["OUT_Y"] + 19], outline=(120, 90, 200, 255))
    for r in range(3):
        for c in range(9): slot(8 + c * 18, 84 + r * 18)
    for c in range(9): slot(8 + c * 18, 142)
    ax, ay = 124, 36   # arrow outline (empty) in the panel, filled version at (176, 0)
    arrow = [(0, 5), (14, 5), (14, 0), (23, 8), (14, 16), (14, 11), (0, 11)]
    d.polygon([(ax + x, ay + y) for x, y in arrow], fill=(139, 139, 139, 255), outline=(55, 55, 55, 255))
    d.polygon([(176 + x, 0 + y) for x, y in arrow], fill=(70, 235, 225, 255), outline=(20, 120, 140, 255))
    os.makedirs(os.path.join(ROOT, f"assets/{MOD}/textures/gui"), exist_ok=True); im.save(os.path.join(ROOT, f"assets/{MOD}/textures/gui/fusion_chamber.png"))

# ------------------------------------------------------------------ models, states, lang
def machine_models():
    for n in ("quantum_furnace",):
        for suffix, front in (("", "quantum_furnace_front"), ("_on", "quantum_furnace_front_on")):
            w(f"assets/{MOD}/models/block/{n}{suffix}.json", {"parent": "minecraft:block/orientable", "textures": {
                "top": f"{MOD}:block/quantum_furnace_top", "front": f"{MOD}:block/{front}", "side": f"{MOD}:block/quantum_furnace_side"}})
        rot = {"north": 0, "east": 90, "south": 180, "west": 270}
        w(f"assets/{MOD}/blockstates/{n}.json", {"variants": {f"facing={f},lit={lit}": {"model": f"{MOD}:block/{n}{'_on' if lit == 'true' else ''}", **({"y": r} if r else {})}
                                                  for f, r in rot.items() for lit in ("false", "true")}})
        w(f"assets/{MOD}/models/item/{n}.json", {"parent": f"{MOD}:block/{n}"})
    for n in ("fusion_chamber", "matter_collector", "quantum_vault"):
        w(f"assets/{MOD}/models/block/{n}.json", {"parent": "minecraft:block/cube_bottom_top", "textures": {
            "top": f"{MOD}:block/{n}_top", "bottom": f"{MOD}:block/{n}_bottom", "side": f"{MOD}:block/{n}_side"}})
        w(f"assets/{MOD}/blockstates/{n}.json", {"variants": {"": {"model": f"{MOD}:block/{n}"}}})
        w(f"assets/{MOD}/models/item/{n}.json", {"parent": f"{MOD}:block/{n}"})

LANG = {
 "en_us": {"block.quantumrift.quantum_furnace": "Quantum Furnace", "block.quantumrift.fusion_chamber": "Fusion Chamber", "block.quantumrift.matter_collector": "Matter Collector",
           "block.quantumrift.quantum_vault": "Quantum Vault", "container.quantumrift.quantum_furnace": "Quantum Furnace", "container.quantumrift.fusion_chamber": "Fusion Chamber",
           "container.quantumrift.matter_collector": "Matter Collector", "container.quantumrift.quantum_vault": "Quantum Vault"},
 "fr_fr": {"block.quantumrift.quantum_furnace": "Four quantique", "block.quantumrift.fusion_chamber": "Chambre de fusion", "block.quantumrift.matter_collector": "Collecteur de matiere",
           "block.quantumrift.quantum_vault": "Coffre-fort quantique", "container.quantumrift.quantum_furnace": "Four quantique", "container.quantumrift.fusion_chamber": "Chambre de fusion",
           "container.quantumrift.matter_collector": "Collecteur de matiere", "container.quantumrift.quantum_vault": "Coffre-fort quantique"}}
def lang():
    for lg, kv in LANG.items():
        p = os.path.join(ROOT, f"assets/{MOD}/lang/{lg}.json"); d = json.load(open(p)); d.update(kv); w(f"assets/{MOD}/lang/{lg}.json", d)

# ------------------------------------------------------------------ data
def self_loot(b, extra=None):
    w(f"data/{MOD}/loot_table/blocks/{b}.json", {"type": "minecraft:block", "pools": [{"rolls": 1.0, "bonus_rolls": 0.0,
        "conditions": [{"condition": "minecraft:survives_explosion"}], "entries": [{"type": "minecraft:item", "name": f"{MOD}:{b}", **(extra or {})}]}]})

def data():
    # loot
    for b, src in (("omnium_ore", "iron_ore"), ("deepslate_omnium_ore", "deepslate_iron_ore"), ("nether_omnium_ore", "iron_ore"), ("end_omnium_ore", "iron_ore")):
        s = Z.read(f"data/minecraft/loot_table/blocks/{src}.json").decode()
        s = s.replace('"minecraft:raw_iron"', f'"{MOD}:raw_omnium"').replace(f'"minecraft:{src}"', f'"{MOD}:{b}"')
        w(f"data/{MOD}/loot_table/blocks/{b}.json", s)
    for b in ("omnium_block", "rift_crystal", "quantum_furnace", "fusion_chamber", "matter_collector", "quantum_vault"): self_loot(b)
    # tags
    def merge(path, vals):
        p = os.path.join(ROOT, path); d = json.load(open(p)) if os.path.exists(p) else {"replace": False, "values": []}
        d["values"] = sorted(set(d["values"]) | set(vals)); w(path, d)
    allb = [f"{MOD}:{b}" for b in ("omnium_ore", "deepslate_omnium_ore", "nether_omnium_ore", "end_omnium_ore", "omnium_block", "rift_crystal", "quantum_furnace", "fusion_chamber", "matter_collector", "quantum_vault")]
    merge("data/minecraft/tags/block/mineable/pickaxe.json", allb)
    merge("data/minecraft/tags/block/needs_iron_tool.json", allb)
    w(f"data/{MOD}/tags/block/incorrect_for_prismatic_tool.json", {"values": []})
    # recipes
    R = f"data/{MOD}/recipe/"
    def shaped(name, pattern, key, result, count=1): w(R + name + ".json", {"type": "minecraft:crafting_shaped", "pattern": pattern, "key": key, "result": {"id": result if ":" in result else f"{MOD}:{result}", "count": count}})
    def shapeless(name, ings, result, count=1): w(R + name + ".json", {"type": "minecraft:crafting_shapeless", "ingredients": ings, "result": {"id": result if ":" in result else f"{MOD}:{result}", "count": count}})
    mc = lambda i: {"item": f"minecraft:{i}"}
    for kind, t in (("smelting", 200), ("blasting", 100)):
        for src in ("raw_omnium", "omnium_ore", "deepslate_omnium_ore", "nether_omnium_ore", "end_omnium_ore"):
            w(R + f"omnium_ingot_from_{kind}_{src}.json", {"type": f"minecraft:{kind}", "category": "misc", "ingredient": item(src), "result": {"id": f"{MOD}:omnium_ingot"}, "experience": 1.2, "cookingtime": t})
    shaped("omnium_block", ["XXX"] * 3, {"X": item("omnium_ingot")}, "omnium_block")
    shapeless("omnium_ingot_from_block", [item("omnium_block")], "omnium_ingot", 9)
    shaped("upgrade_module_core", [" A ", "IQI", " R "], {"A": mc("amethyst_shard"), "I": mc("iron_ingot"), "Q": item("quantite_ingot"), "R": mc("redstone")}, "upgrade_module_core", 2)
    mats = {"coal": mc("coal"), "copper": mc("copper_ingot"), "iron": mc("iron_ingot"), "gold": mc("gold_ingot"), "redstone": mc("redstone"), "lapis": mc("lapis_lazuli"),
            "diamond": mc("diamond"), "emerald": mc("emerald"), "netherite": mc("netherite_scrap"), "quantite": item("quantite_ingot"), "singularity": item("singularity_shard"), "omnium": item("omnium_ingot")}
    for ore, m in mats.items():
        shaped(f"{ore}_upgrade_module", [" X ", "XCX", " X "], {"X": m, "C": item("upgrade_module_core")}, f"{ore}_upgrade_module")
    shaped("quantum_furnace", ["QQQ", "QFQ", "QOQ"], {"Q": item("quantite_ingot"), "F": mc("furnace"), "O": item("omnium_ingot")}, "quantum_furnace")
    shaped("fusion_chamber", ["OSO", "QNQ", "OQO"], {"O": item("omnium_ingot"), "S": item("singularity_ingot"), "Q": item("quantite_block"), "N": mc("netherite_block")}, "fusion_chamber")
    shaped("matter_collector", ["QEQ", "OHO", "QQQ"], {"Q": item("quantite_ingot"), "E": mc("ender_eye"), "O": item("omnium_ingot"), "H": mc("hopper")}, "matter_collector")
    shaped("quantum_vault", ["QOQ", "OCO", "QOQ"], {"Q": item("quantite_ingot"), "O": item("omnium_ingot"), "C": mc("ender_chest")}, "quantum_vault")
    shapeless("quantinium_coal", [mc("coal_block")] * 4 + [item("omnium_ingot"), item("quantite_ingot")], "quantinium_coal")
    shaped("quantinium_steak", ["BBB", "BOB", "BBB"], {"B": mc("cooked_beef"), "O": item("omnium_ingot")}, "quantinium_steak")
    shaped("sovereign_sigil", ["SNS", "EOE", "SNS"], {"S": item("singularity_shard"), "N": mc("nether_star"), "E": mc("ender_eye"), "O": item("omnium_ingot")}, "sovereign_sigil")
    shaped("prismatic_template_duplication", ["OTO", "OSO", "OOO"], {"O": item("omnium_ingot"), "T": item("prismatic_template"), "S": item("singularity_block")}, "prismatic_template", 2)
    for part in ("sword", "pickaxe", "axe", "shovel", "hoe", "helmet", "chestplate", "leggings", "boots"):
        w(R + f"prismatic_{part}_smithing.json", {"type": "minecraft:smithing_transform", "template": item("prismatic_template"), "base": item(f"singularity_{part}"),
                                                  "addition": item("omnium_ingot"), "result": {"id": f"{MOD}:prismatic_{part}"}})
    # worldgen: Omnium ore generates in the Overworld, the Nether, the End and the Quantum Realm
    w(f"data/{MOD}/worldgen/configured_feature/omnium_ore.json", {"type": "minecraft:ore", "config": {"discard_chance_on_air_exposure": 0.0, "size": 5, "targets": [
        {"state": {"Name": f"{MOD}:omnium_ore"}, "target": {"predicate_type": "minecraft:tag_match", "tag": "minecraft:stone_ore_replaceables"}},
        {"state": {"Name": f"{MOD}:deepslate_omnium_ore"}, "target": {"predicate_type": "minecraft:tag_match", "tag": "minecraft:deepslate_ore_replaceables"}},
        {"state": {"Name": f"{MOD}:nether_omnium_ore"}, "target": {"predicate_type": "minecraft:tag_match", "tag": "minecraft:base_stone_nether"}},
        {"state": {"Name": f"{MOD}:end_omnium_ore"}, "target": {"predicate_type": "minecraft:block_match", "block": "minecraft:end_stone"}},
        {"state": {"Name": f"{MOD}:omnium_ore"}, "target": {"predicate_type": "minecraft:block_match", "block": f"{MOD}:quantum_stone"}}]}})
    w(f"data/{MOD}/worldgen/placed_feature/omnium_ore.json", {"feature": f"{MOD}:omnium_ore", "placement": [
        {"type": "minecraft:count", "count": 4}, {"type": "minecraft:in_square"},
        {"type": "minecraft:height_range", "height": {"type": "minecraft:uniform", "min_inclusive": {"absolute": -60}, "max_inclusive": {"absolute": 120}}},
        {"type": "minecraft:biome"}]})
    for loader, t in (("neoforge", "neoforge:add_features"), ("forge", "forge:add_features")):
        for dim in ("overworld", "nether", "end"):
            w(f"data/{MOD}/{loader}/biome_modifier/add_omnium_ore_{dim}.json", {"type": t, "biomes": f"#minecraft:is_{dim}", "features": f"{MOD}:omnium_ore", "step": "underground_ores"})

def altar():
    side = Image.new("RGBA", (16, 16), (0, 0, 0, 255)); px = side.load()
    dark = gradient(vanilla_img("block/obsidian"), [(6, 3, 14), (22, 12, 44), (46, 26, 86), (80, 50, 140)])
    side = dark.copy(); d = ImageDraw.Draw(side)
    for y in (3, 12): d.line([2, y, 13, y], fill=(150, 80, 230, 255))
    for x, y in ((4, 7), (7, 6), (7, 9), (10, 7)): d.point([x, y], fill=(110, 240, 235, 255))
    d.rectangle([5, 5, 10, 10], outline=(110, 60, 190, 255))
    save_tex("block/sovereign_altar_side", side)
    top = dark.copy(); d = ImageDraw.Draw(top)
    d.ellipse([1, 1, 14, 14], outline=(150, 80, 230, 255)); d.ellipse([4, 4, 11, 11], outline=(110, 240, 235, 255))
    for i in range(8):
        a = i * math.pi / 4; d.point([int(7.5 + 6 * math.cos(a)), int(7.5 + 6 * math.sin(a))], fill=(255, 255, 255, 255))
    d.rectangle([6, 6, 9, 9], fill=(190, 90, 255, 255))
    save_tex("block/sovereign_altar_top", top)
    t = {"side": f"{MOD}:block/sovereign_altar_side", "top": f"{MOD}:block/sovereign_altar_top", "particle": f"{MOD}:block/sovereign_altar_side"}
    el = lambda a, b, top_tex="#top": {"from": a, "to": b, "faces": {"down": {"texture": "#top"}, "up": {"texture": "#top"}, "north": {"texture": "#side"}, "south": {"texture": "#side"}, "west": {"texture": "#side"}, "east": {"texture": "#side"}}}
    w(f"assets/{MOD}/models/block/sovereign_altar.json", {"textures": t, "elements": [el([0, 0, 0], [16, 4, 16]), el([4, 4, 4], [12, 12, 12]), el([1, 12, 1], [15, 16, 15])]})
    w(f"assets/{MOD}/blockstates/sovereign_altar.json", {"variants": {"": {"model": f"{MOD}:block/sovereign_altar"}}})
    w(f"assets/{MOD}/models/item/sovereign_altar.json", {"parent": f"{MOD}:block/sovereign_altar"})
    for lg, kv in {"en_us": {"block.quantumrift.sovereign_altar": "Sovereign Altar", "message.quantumrift.altar_silent": "The altar is silent here...",
                             "message.quantumrift.altar_busy": "The Sovereign already stirs.", "message.quantumrift.altar_hint": "It hungers for a sigil."},
                   "fr_fr": {"block.quantumrift.sovereign_altar": "Autel du Souverain", "message.quantumrift.altar_silent": "L'autel reste silencieux ici...",
                             "message.quantumrift.altar_busy": "Le Souverain est deja eveille.", "message.quantumrift.altar_hint": "Il lui faut un sceau."}}.items():
        p = os.path.join(ROOT, f"assets/{MOD}/lang/{lg}.json"); dd = json.load(open(p)); dd.update(kv); w(f"assets/{MOD}/lang/{lg}.json", dd)
    w(f"data/{MOD}/loot_table/blocks/sovereign_altar.json", {"type": "minecraft:block", "pools": []})

if __name__ == "__main__":
    altar(); textures(); gui(); machine_models(); lang(); data(); print("content2 ok")
