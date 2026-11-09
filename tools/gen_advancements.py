#!/usr/bin/env python3
"""One recipe advancement per recipe: unlocks the recipe in the recipe book once the player holds any of its ingredients (vanilla behaviour)."""
import json, os, sys
sys.path.insert(0, os.path.dirname(__file__))
from gen_resources import MOD, ROOT, w

def ingredient_items(r):
    out = []
    def take(i):
        if isinstance(i, dict) and "item" in i: out.append(i["item"])
        elif isinstance(i, list):
            for x in i: take(x)
    t = r["type"]
    if t == "minecraft:crafting_shaped":
        for v in r["key"].values(): take(v)
    elif t == "minecraft:crafting_shapeless":
        for v in r["ingredients"]: take(v)
    elif t == "minecraft:smithing_transform":
        for k in ("template", "base", "addition"): take(r[k])
    else:
        take(r.get("ingredient"))
    return list(dict.fromkeys(out))

def main():
    rdir = os.path.join(ROOT, f"data/{MOD}/recipe"); n = 0
    for f in sorted(os.listdir(rdir)):
        name = f[:-5]; r = json.load(open(os.path.join(rdir, f))); items = ingredient_items(r)
        if not items: continue
        crit = {f"has_{i.split(':')[1]}": {"trigger": "minecraft:inventory_changed", "conditions": {"items": [{"items": i}]}} for i in items}
        crit["has_the_recipe"] = {"trigger": "minecraft:recipe_unlocked", "conditions": {"recipe": f"{MOD}:{name}"}}
        w(f"data/{MOD}/advancement/recipes/{name}.json", {"parent": "minecraft:recipes/root", "criteria": crit,
            "requirements": [["has_the_recipe"] + [k for k in crit if k != "has_the_recipe"]], "rewards": {"recipes": [f"{MOD}:{name}"]}})
        n += 1
    print(n, "recipe advancements")

if __name__ == "__main__":
    main()
