**English** · [Français](README.fr.md)

# Quantum Rift

Minecraft **1.21.1** mod for **Fabric**, **NeoForge** and **Forge** (one shared `common` module, three thin loader modules).
Requires **TerraBlender** (4.1.0.1) on all loaders, plus **Fabric API** on Fabric.

## What it adds
- **Ores & gear** – Quantite (between iron and diamond), Singularity (above netherite, from the Quantum Realm), **Omnium** (generates in the Overworld, Nether, End and Quantum Realm).
- **Prismatic gear** – the strongest tier. Unlock: fuse one *upgrade module of every ore* (12) plus a Sovereign's Heart in the **Fusion Chamber** to get a Prismatic Template, then upgrade Singularity gear at a smithing table. Full set grants Strength II + Resistance.
- **Fused horror biomes** (3) – dead, ash-filled mixes of vanilla biomes with their own mobs and ambience; a **rift gate** stands in each. A cartographer sells a map to the nearest one.
- **Rift gate** – insert 3 interdimensional rods into its core to wake it; it leads to the **Quantum Realm** (floating islands, black hole in the sky, 10 mobs).
- **Sovereign Altar** – floating island structure in the Quantum Realm. A Sovereign's Sigil calls the **Void Sovereign** (3-phase boss).
- **2 special caves** – Crystalline Fracture and Whispering Hollows (horror).
- **21 mobs**, each with its own sounds; spawn eggs for all.
- **Machines** – Quantum Furnace (3x speed), Fusion Chamber, Matter Collector, Quantum Vault.
- **Quantinium Coal** (never used up as fuel) and **Quantinium Steak** (never eaten up).

## Building
```
JAVA_HOME=<jdk 21> ./gradlew build          # jars in <loader>/build/libs/
scripts/make-mrpack.sh                      # dist/QuantumRift-<loader>-<version>.mrpack
scripts/smoke-test.sh [fabric neoforge forge]   # headless server tests (blocks, mobs, dimension, structures, machines)
python3 tools/validate_assets.py            # offline check of models, textures, lang, sounds, recipes
```

## Generated files
Most assets and data are generated; edit the generators, not the output. `tools/gen_all.sh` runs them in order:
`gen_resources` (items, tools, base recipes) → `gen_world` (dimension, biomes) → `gen_content2` (Omnium, modules, machines, GUI) →
`gen_structures` (hand-designed builds, with symmetry checks and previews in `tools/previews/`) → `gen_boss` → `build_sounds` → `gen_mobs`
(Java for mobs from `tools/mob_table.py`).

## Credits
See `CREDITS.md` (sounds are CC0 from OpenGameArt).
