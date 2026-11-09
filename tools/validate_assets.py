#!/usr/bin/env python3
"""Offline check of everything the client/server would load from our resources. Exit code 1 on any problem."""
import json, os, re, sys, zipfile
ROOT = os.path.join(os.path.dirname(__file__), "..")
RES = os.path.join(ROOT, "common/src/main/resources"); JAVA = os.path.join(ROOT, "common/src/main/java/com/quantumrift")
VAN = zipfile.ZipFile(os.path.expanduser("~/.gradle/caches/fabric-loom/1.21.1/minecraft-merged.jar")); VN = set(VAN.namelist())
MOD = "quantumrift"; errs = []

def err(m): errs.append(m)
def ours(kind, rid, ext): return os.path.join(RES, "assets", rid.split(":")[0], kind, rid.split(":")[1] + ext)
def ns(rid): return rid if ":" in rid else "minecraft:" + rid
def exists_model(rid):
    rid = ns(rid); n, p = rid.split(":")
    return os.path.exists(os.path.join(RES, "assets", n, "models", p + ".json")) if n == MOD else f"assets/minecraft/models/{p}.json" in VN
def exists_tex(rid):
    rid = ns(rid); n, p = rid.split(":")
    return os.path.exists(os.path.join(RES, "assets", n, "textures", p + ".png")) if n == MOD else f"assets/minecraft/textures/{p}.png" in VN
def load(path): return json.load(open(path))

def model_ok(rid, ctx, seen=()):
    rid = ns(rid)
    if not exists_model(rid): err(f"{ctx}: model {rid} missing"); return
    n, p = rid.split(":")
    if n != MOD: return
    m = load(os.path.join(RES, "assets", n, "models", p + ".json"))
    if "parent" in m: model_ok(m["parent"], ctx + f" -> {rid}")
    for k, t in m.get("textures", {}).items():
        if not t.startswith("#") and not exists_tex(t): err(f"{ctx} -> {rid}: texture {t} missing")

lang = {lg: load(os.path.join(RES, f"assets/{MOD}/lang/{lg}.json")) for lg in ("en_us", "fr_fr")}
def need_lang(key, ctx):
    for lg, d in lang.items():
        if key not in d: err(f"{ctx}: lang key {key} missing in {lg}")

src = lambda f: open(os.path.join(JAVA, f)).read()
items = re.findall(r'(?:item|armor|prismatic|module)\("([a-z_0-9]+)"', src("content/ModItems.java")) + re.findall(r'ModItems\.item\("([a-z_0-9]+)"', src("content/ModEggs.java"))
items += [f"{o}_upgrade_module" for o in re.findall(r'"(\w+)"', re.search(r'MODULE_ORES = \{([^}]*)\}', src("content/ModItems.java")).group(1))]
blocks = re.findall(r'block\("([a-z_0-9]+)"', src("content/ModBlocks.java"))
entities = re.findall(r'reg\("([a-z_]+)"', src("entity/ModEntities.java"))
items = sorted(set(items)); block_items = set(blocks)

for i in items:
    mp = os.path.join(RES, f"assets/{MOD}/models/item/{i}.json")
    if not os.path.exists(mp): err(f"item {i}: models/item/{i}.json missing"); continue
    model_ok(f"{MOD}:item/{i}", f"item {i}")
    need_lang(f"{'block' if i in block_items else 'item'}.{MOD}.{i}", f"item {i}")
for b in blocks:
    sp = os.path.join(RES, f"assets/{MOD}/blockstates/{b}.json")
    if not os.path.exists(sp): err(f"block {b}: blockstate missing"); continue
    st = load(sp); variants = st.get("variants", {})
    for k, v in variants.items():
        for mm in (v if isinstance(v, list) else [v]): model_ok(mm["model"], f"block {b}[{k}]")
    lp = os.path.join(RES, f"data/{MOD}/loot_table/blocks/{b}.json")
    if not os.path.exists(lp): err(f"block {b}: loot table missing")
for e in entities:
    need_lang(f"entity.{MOD}.{e}", f"entity {e}")
    if not os.path.exists(os.path.join(RES, f"data/{MOD}/loot_table/entities/{e}.json")): err(f"entity {e}: loot table missing")
for t in re.findall(r'tex\("([a-z_]+)"\)', src("client/ClientRenderers.java")) + ["phase_wraith_charging"]:
    if not os.path.exists(os.path.join(RES, f"assets/{MOD}/textures/entity/{t}.png")): err(f"entity texture {t} missing")
for t in ("void_sovereign", "void_sovereign_glow"):
    if not os.path.exists(os.path.join(RES, f"assets/{MOD}/textures/entity/{t}.png")): err(f"entity texture {t} missing")
for m in ("quantite", "singularity", "prismatic"):
    for n in (1, 2):
        if not os.path.exists(os.path.join(RES, f"assets/{MOD}/textures/models/armor/{m}_layer_{n}.png")): err(f"armor layer {m}_{n} missing")
sj = load(os.path.join(RES, f"assets/{MOD}/sounds.json"))
for ev, d in sj.items():
    for s in d["sounds"]:
        if not os.path.exists(os.path.join(RES, f"assets/{MOD}/sounds", s["name"].split(":")[1] + ".ogg")): err(f"sound {ev}: {s['name']}.ogg missing")
    need_lang(d["subtitle"], f"sound {ev}")
snames = set(re.findall(r'reg\("([a-z_.0-9]+)"\)', src("content/ModSounds.java")))
if snames != set(sj): err(f"sounds.json and ModSounds differ: {sorted(snames ^ set(sj))[:5]}")
# every json under data/ must parse, recipes must reference existing items
ids = {f"{MOD}:{i}" for i in items}
for root, _, files in os.walk(os.path.join(RES, "data")):
    for f in files:
        if f.endswith(".json"):
            p = os.path.join(root, f)
            try: j = load(p)
            except Exception as ex: err(f"{p}: invalid json {ex}"); continue
            if "/recipe/" in p:
                txt = json.dumps(j)
                for ref in re.findall(r'"(?:item|id)": "(quantumrift:[a-z_0-9]+)"', txt):
                    if ref not in ids and ref.split(":")[1] not in blocks: err(f"{os.path.relpath(p, RES)}: unknown item {ref}")
for f in ("structure/rift_gate.nbt", "structure/return_gate.nbt", "structure/sovereign_altar.nbt"):
    if not os.path.exists(os.path.join(RES, "data", MOD, f)): err(f"{f} missing")
print(f"checked {len(items)} items, {len(blocks)} blocks, {len(entities)} entities, {len(sj)} sound events")
for e in errs[:60]: print("  ERROR", e)
print("OK" if not errs else f"{len(errs)} PROBLEMS"); sys.exit(1 if errs else 0)
