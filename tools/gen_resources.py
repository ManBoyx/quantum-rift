#!/usr/bin/env python3
"""Generates assets/ and data/ of the common module (models, textures recolored from vanilla,
recipes, loot tables, tags, ore worldgen, lang). Re-run after editing the tables below.
Usage: python3 tools/gen_resources.py"""
import io, json, os, re, sys, zipfile
import numpy as np
from PIL import Image

MOD = "quantumrift"
ROOT = os.path.join(os.path.dirname(__file__), "..", "common", "src", "main", "resources")
VANILLA = os.path.expanduser("~/.gradle/caches/fabric-loom/1.21.1/minecraft-merged.jar")
Z = zipfile.ZipFile(VANILLA)

def w(path, data):
    p = os.path.join(ROOT, path); os.makedirs(os.path.dirname(p), exist_ok=True)
    if isinstance(data, (dict, list)): data = json.dumps(data, indent=2, ensure_ascii=False) + "\n"
    open(p, "w", encoding="utf-8").write(data)

# ---------- palettes (dark -> light) ----------
QUANTITE = [(10, 45, 60), (20, 110, 125), (70, 200, 200), (170, 255, 240)]
SINGULARITY = [(8, 0, 20), (50, 10, 90), (140, 50, 210), (230, 150, 255)]
ROD = [(30, 10, 70), (30, 140, 170), (120, 240, 230), (255, 255, 255)]

def vanilla_img(rel):
    return Image.open(io.BytesIO(Z.read("assets/minecraft/textures/" + rel + ".png"))).convert("RGBA")

def gradient(img, palette, mask=None):
    a = np.array(img).astype(np.float32)
    lum = (0.299 * a[..., 0] + 0.587 * a[..., 1] + 0.114 * a[..., 2]) / 255.0
    lum = np.clip((lum - lum[a[..., 3] > 0].min()) / max(1e-3, np.ptp(lum[a[..., 3] > 0])), 0, 1) if (a[..., 3] > 0).any() else lum
    stops = np.linspace(0, 1, len(palette)); pal = np.array(palette, dtype=np.float32)
    out = a.copy()
    for c in range(3): out[..., c] = np.interp(lum, stops, pal[:, c])
    if mask is not None:
        out[~mask, :3] = a[~mask, :3]
    return Image.fromarray(out.clip(0, 255).astype(np.uint8), "RGBA")

def ore_mask(ore, base):
    d = np.abs(np.array(ore).astype(int)[..., :3] - np.array(base).astype(int)[..., :3]).sum(-1)
    return d > 40

def save_tex(rel, img):
    p = os.path.join(ROOT, "assets", MOD, "textures", rel + ".png"); os.makedirs(os.path.dirname(p), exist_ok=True); img.save(p)

# ---------- textures ----------
def tex_all():
    items = {"raw_quantite": ("item/raw_iron", QUANTITE), "quantite_ingot": ("item/iron_ingot", QUANTITE),
             "singularity_shard": ("item/netherite_scrap", SINGULARITY), "singularity_ingot": ("item/netherite_ingot", SINGULARITY),
             "interdimensional_rod": ("item/blaze_rod", ROD)}
    for part in ["sword", "pickaxe", "axe", "shovel", "hoe", "helmet", "chestplate", "leggings", "boots"]:
        items["quantite_" + part] = ("item/iron_" + part, QUANTITE)
        items["singularity_" + part] = ("item/netherite_" + part, SINGULARITY)
    for name, (src, pal) in items.items():
        save_tex("item/" + name, gradient(vanilla_img(src), pal))
    for name, src, base, pal in [("quantite_ore", "iron_ore", "stone", QUANTITE),
                                 ("deepslate_quantite_ore", "deepslate_iron_ore", "deepslate", QUANTITE),
                                 ("singularity_ore", "deepslate_diamond_ore", "deepslate", SINGULARITY)]:
        ore = vanilla_img("block/" + src); save_tex("block/" + name, gradient(ore, pal, ore_mask(ore, vanilla_img("block/" + base))))
    for name, src, pal in [("raw_quantite_block", "raw_iron_block", QUANTITE), ("quantite_block", "iron_block", QUANTITE),
                           ("singularity_block", "netherite_block", SINGULARITY)]:
        save_tex("block/" + name, gradient(vanilla_img("block/" + src), pal))
    for name, src, pal in [("quantite", "iron", QUANTITE), ("singularity", "netherite", SINGULARITY)]:
        for n in (1, 2): save_tex("models/armor/%s_layer_%d" % (name, n), gradient(vanilla_img("models/armor/%s_layer_%d" % (src, n)), pal))

# ---------- content tables ----------
BLOCKS = {  # id: (en, fr)
    "quantite_ore": ("Quantite Ore", "Minerai de quantite"), "deepslate_quantite_ore": ("Deepslate Quantite Ore", "Minerai de quantite des abysses"),
    "raw_quantite_block": ("Block of Raw Quantite", "Bloc de quantite brute"), "quantite_block": ("Block of Quantite", "Bloc de quantite"),
    "singularity_ore": ("Singularity Ore", "Minerai de singularite"), "singularity_block": ("Block of Singularity", "Bloc de singularite"),
    "omnium_ore": ("Omnium Ore", "Minerai d'omnium"), "deepslate_omnium_ore": ("Deepslate Omnium Ore", "Minerai d'omnium des abysses"),
    "nether_omnium_ore": ("Nether Omnium Ore", "Minerai d'omnium du Nether"), "end_omnium_ore": ("End Omnium Ore", "Minerai d'omnium de l'End"),
    "omnium_block": ("Block of Omnium", "Bloc d'omnium"), "rift_crystal": ("Rift Crystal", "Cristal de faille")}
SIMPLE_ITEMS = {"raw_quantite": ("Raw Quantite", "Quantite brute"), "quantite_ingot": ("Quantite Ingot", "Lingot de quantite"),
                "singularity_shard": ("Singularity Shard", "Eclat de singularite"), "singularity_ingot": ("Singularity Ingot", "Lingot de singularite"),
                "interdimensional_rod": ("Interdimensional Rod", "Barre interdimensionnelle"),
                "raw_omnium": ("Raw Omnium", "Omnium brut"), "omnium_ingot": ("Omnium Ingot", "Lingot d'omnium"),
                "upgrade_module_core": ("Module Core", "Noyau de module"), "sovereign_sigil": ("Sovereign's Sigil", "Sceau du Souverain"),
                "sovereign_heart": ("Sovereign's Heart", "Coeur du Souverain"), "prismatic_template": ("Prismatic Upgrade Template", "Modele d'amelioration prismatique"),
                "quantinium_coal": ("Quantinium Coal", "Charbon de quantinium"), "quantinium_steak": ("Quantinium Steak", "Steak de quantinium"),
                **{f"{o}_upgrade_module": (f"{o.title()} Upgrade Module", f"Module d'amelioration : {o}") for o in
                   ("coal", "copper", "iron", "gold", "redstone", "lapis", "diamond", "emerald", "netherite", "quantite", "singularity", "omnium")}}
PARTS = {"sword": ("Sword", "Epee"), "pickaxe": ("Pickaxe", "Pioche"), "axe": ("Axe", "Hache"), "shovel": ("Shovel", "Pelle"), "hoe": ("Hoe", "Houe"),
         "helmet": ("Helmet", "Casque"), "chestplate": ("Chestplate", "Plastron"), "leggings": ("Leggings", "Jambieres"), "boots": ("Boots", "Bottes")}
MATS = {"quantite": ("Quantite", "en quantite"), "singularity": ("Singularity", "en singularite"), "prismatic": ("Prismatic", "prismatique")}
TOOLS = ["sword", "pickaxe", "axe", "shovel", "hoe"]; ARMOR = ["helmet", "chestplate", "leggings", "boots"]

def models():
    for b in BLOCKS:
        w(f"assets/{MOD}/blockstates/{b}.json", {"variants": {"": {"model": f"{MOD}:block/{b}"}}})
        w(f"assets/{MOD}/models/block/{b}.json", {"parent": "minecraft:block/cube_all", "textures": {"all": f"{MOD}:block/{b}"}})
        w(f"assets/{MOD}/models/item/{b}.json", {"parent": f"{MOD}:block/{b}"})
    items = list(SIMPLE_ITEMS) + [f"{m}_{p}" for m in MATS for p in PARTS]
    for i in items:
        handheld = i.split("_")[-1] in ("sword", "pickaxe", "axe", "shovel", "hoe") or i == "interdimensional_rod"
        w(f"assets/{MOD}/models/item/{i}.json", {"parent": "minecraft:item/handheld" if handheld else "minecraft:item/generated", "textures": {"layer0": f"{MOD}:item/{i}"}})

def lang():
    en = {"itemGroup." + MOD: "Quantum Rift"}; fr = {"itemGroup." + MOD: "Quantum Rift"}
    for b, (e, f) in BLOCKS.items(): en[f"block.{MOD}.{b}"] = e; fr[f"block.{MOD}.{b}"] = f
    for i, (e, f) in SIMPLE_ITEMS.items(): en[f"item.{MOD}.{i}"] = e; fr[f"item.{MOD}.{i}"] = f
    for m, (me, mf) in MATS.items():
        for p, (pe, pf) in PARTS.items():
            en[f"item.{MOD}.{m}_{p}"] = f"{me} {pe}"; fr[f"item.{MOD}.{m}_{p}"] = f"{pf} {mf}"
    w(f"assets/{MOD}/lang/en_us.json", en); w(f"assets/{MOD}/lang/fr_fr.json", fr)

def item(i): return {"item": f"{MOD}:{i}"}
def recipes():
    R = f"data/{MOD}/recipe/"
    def shaped(name, pattern, key, result, count=1):
        w(R + name + ".json", {"type": "minecraft:crafting_shaped", "pattern": pattern, "key": key, "result": {"id": f"{MOD}:{result}", "count": count}})
    shapes = {"sword": ["X", "X", "S"], "pickaxe": ["XXX", " S ", " S "], "axe": ["XX", "XS", " S"], "shovel": ["X", "S", "S"], "hoe": ["XX", " S", " S"],
              "helmet": ["XXX", "X X"], "chestplate": ["X X", "XXX", "XXX"], "leggings": ["XXX", "X X", "X X"], "boots": ["X X", "X X"]}
    for m, ingot in (("quantite", "quantite_ingot"), ("singularity", "singularity_ingot")):
        for p, pat in shapes.items():
            key = {"X": item(ingot)}
            if p in TOOLS: key["S"] = {"item": "minecraft:stick"}
            shaped(f"{m}_{p}", pat, key, f"{m}_{p}")
    for m in ("quantite", "singularity"):
        shaped(f"{m}_block", ["XXX"] * 3, {"X": item(f"{m}_ingot")}, f"{m}_block")
        w(R + f"{m}_ingot_from_{m}_block.json", {"type": "minecraft:crafting_shapeless", "ingredients": [item(f"{m}_block")], "result": {"id": f"{MOD}:{m}_ingot", "count": 9}})
    shaped("raw_quantite_block", ["XXX"] * 3, {"X": item("raw_quantite")}, "raw_quantite_block")
    w(R + "raw_quantite_from_block.json", {"type": "minecraft:crafting_shapeless", "ingredients": [item("raw_quantite_block")], "result": {"id": f"{MOD}:raw_quantite", "count": 9}})
    for kind, t in (("smelting", 200), ("blasting", 100)):
        for src, tag in (("raw_quantite", None), ("quantite_ore", None), ("deepslate_quantite_ore", None)):
            w(R + f"quantite_ingot_from_{kind}_{src}.json", {"type": f"minecraft:{kind}", "category": "misc", "ingredient": item(src), "result": {"id": f"{MOD}:quantite_ingot"}, "experience": 1.0, "cookingtime": t})
    # technical part: the ingot needs 4 shards AND a netherite ingot
    w(R + "singularity_ingot.json", {"type": "minecraft:crafting_shapeless", "ingredients": [item("singularity_shard")] * 4 + [{"item": "minecraft:netherite_ingot"}],
                                    "result": {"id": f"{MOD}:singularity_ingot"}})
    # interdimensional rod
    shaped("interdimensional_rod", [" EA", " QE", "Q  "], {"E": {"item": "minecraft:ender_pearl"}, "A": {"item": "minecraft:amethyst_shard"}, "Q": item("quantite_ingot")}, "interdimensional_rod")

def loot():
    for b, src, repl in [("quantite_ore", "iron_ore", {"minecraft:iron_ore": f"{MOD}:quantite_ore", "minecraft:raw_iron": f"{MOD}:raw_quantite"}),
                         ("deepslate_quantite_ore", "deepslate_iron_ore", {"minecraft:deepslate_iron_ore": f"{MOD}:deepslate_quantite_ore", "minecraft:raw_iron": f"{MOD}:raw_quantite"}),
                         ("singularity_ore", "diamond_ore", {"minecraft:diamond_ore": f"{MOD}:singularity_ore", "minecraft:diamond": f"{MOD}:singularity_shard"}),
                         ("raw_quantite_block", "raw_iron_block", {"minecraft:raw_iron_block": f"{MOD}:raw_quantite_block"}),
                         ("quantite_block", "iron_block", {"minecraft:iron_block": f"{MOD}:quantite_block"}),
                         ("singularity_block", "netherite_block", {"minecraft:netherite_block": f"{MOD}:singularity_block"})]:
        s = Z.read(f"data/minecraft/loot_table/blocks/{src}.json").decode()
        for a, c in repl.items(): s = s.replace(f'"{a}"', f'"{c}"')
        w(f"data/{MOD}/loot_table/blocks/{b}.json", s)

def tags():
    def add(path, values, ns="minecraft"): w(f"data/{ns}/tags/{path}.json", {"replace": False, "values": values})
    pick = [f"{MOD}:{b}" for b in BLOCKS]
    add("block/mineable/pickaxe", pick)
    add("block/needs_iron_tool", [f"{MOD}:{b}" for b in ("quantite_ore", "deepslate_quantite_ore", "raw_quantite_block", "quantite_block")])
    add("block/needs_diamond_tool", [f"{MOD}:{b}" for b in ("singularity_ore", "singularity_block")])
    w(f"data/{MOD}/tags/block/incorrect_for_quantite_tool.json", {"values": ["#minecraft:needs_diamond_tool"]})  # between iron and diamond
    w(f"data/{MOD}/tags/block/incorrect_for_singularity_tool.json", {"values": []})
    groups = {"swords": ["sword"], "pickaxes": ["pickaxe"], "axes": ["axe"], "shovels": ["shovel"], "hoes": ["hoe"],
              "head_armor": ["helmet"], "chest_armor": ["chestplate"], "leg_armor": ["leggings"], "foot_armor": ["boots"]}
    sets = {k: [f"{MOD}:{m}_{p}" for m in MATS for p in v] for k, v in groups.items()}
    for k, v in sets.items(): add(f"item/{k}", v)
    armor = sum((sets[k] for k in ("head_armor", "chest_armor", "leg_armor", "foot_armor")), [])
    tools = sum((sets[k] for k in ("swords", "pickaxes", "axes", "shovels", "hoes")), [])
    add("item/trimmable_armor", armor)
    ench = {"armor": armor, "head_armor": sets["head_armor"], "chest_armor": sets["chest_armor"], "leg_armor": sets["leg_armor"], "foot_armor": sets["foot_armor"],
            "durability": armor + tools, "sword": sets["swords"], "sharp_weapon": sets["swords"] + sets["axes"], "weapon": sets["swords"] + sets["axes"],
            "fire_aspect": sets["swords"], "mining": sets["pickaxes"] + sets["axes"] + sets["shovels"] + sets["hoes"],
            "mining_loot": sets["pickaxes"] + sets["axes"] + sets["shovels"] + sets["hoes"], "equippable": armor, "vanishing": armor + tools}
    for k, v in ench.items(): add(f"item/enchantable/{k}", v)

def worldgen():
    c = {"type": "minecraft:ore", "config": {"discard_chance_on_air_exposure": 0.0, "size": 5, "targets": [
        {"state": {"Name": f"{MOD}:quantite_ore"}, "target": {"predicate_type": "minecraft:tag_match", "tag": "minecraft:stone_ore_replaceables"}},
        {"state": {"Name": f"{MOD}:deepslate_quantite_ore"}, "target": {"predicate_type": "minecraft:tag_match", "tag": "minecraft:deepslate_ore_replaceables"}}]}}
    w(f"data/{MOD}/worldgen/configured_feature/quantite_ore.json", c)
    w(f"data/{MOD}/worldgen/placed_feature/quantite_ore.json", {"feature": f"{MOD}:quantite_ore", "placement": [
        {"type": "minecraft:count", "count": 5}, {"type": "minecraft:in_square"},
        {"type": "minecraft:height_range", "height": {"type": "minecraft:trapezoid", "min_inclusive": {"absolute": -50}, "max_inclusive": {"absolute": 50}}},
        {"type": "minecraft:biome"}]})
    w(f"data/{MOD}/neoforge/biome_modifier/add_quantite_ore.json", {"type": "neoforge:add_features", "biomes": "#minecraft:is_overworld", "features": f"{MOD}:quantite_ore", "step": "underground_ores"})
    w(f"data/{MOD}/forge/biome_modifier/add_quantite_ore.json", {"type": "forge:add_features", "biomes": "#minecraft:is_overworld", "features": f"{MOD}:quantite_ore", "step": "underground_ores"})

if __name__ == "__main__":
    tex_all(); models(); lang(); recipes(); loot(); tags(); worldgen()
    w("pack.mcmeta", {"pack": {"description": "Quantum Rift", "pack_format": 34}})
    print("ok")
