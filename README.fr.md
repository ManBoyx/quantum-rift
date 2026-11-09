[English](README.md) · **Français**

# Quantum Rift

Mod Minecraft **1.21.1** pour **Fabric**, **NeoForge** et **Forge** (un module `common` partagé, trois modules fins par chargeur de mods).
Nécessite **TerraBlender** (4.1.0.1) sur tous les chargeurs, et **Fabric API** sur Fabric.

## Ce qu'il ajoute
- **Minerais et équipement** – Quantite (entre le fer et le diamant), Singularité (au-dessus de la netherite, issue du Royaume quantique), **Omnium** (se génère dans l'Overworld, le Nether, l'End et le Royaume quantique).
- **Équipement prismatique** – le palier le plus fort. Pour le débloquer : fusionner un *module d'amélioration de chaque minerai* (12) et un Cœur du Souverain dans la **Chambre de fusion** pour obtenir un Modèle prismatique, puis améliorer l'équipement en Singularité à l'établi de forge. L'armure complète donne Force II + Résistance.
- **Biomes d'horreur fusionnés** (3) – des mélanges morts et couverts de cendres de biomes vanilla, avec leurs propres monstres et ambiances ; une **porte de faille** se dresse dans chacun. Un cartographe vend une carte vers le plus proche.
- **Porte de faille** – insérez 3 bâtons interdimensionnels dans son cœur pour l'éveiller ; elle mène au **Royaume quantique** (îles flottantes, trou noir dans le ciel, 10 monstres).
- **Autel du Souverain** – structure d'île flottante dans le Royaume quantique. Un Sceau du Souverain appelle le **Souverain du Vide** (boss en 3 phases).
- **2 grottes spéciales** – Fracture cristalline et Creux des murmures (horreur).
- **21 monstres**, chacun avec ses propres sons ; œufs d'apparition pour tous.
- **Machines** – Fourneau quantique (vitesse ×3), Chambre de fusion, Collecteur de matière, Coffre-fort quantique.
- **Charbon de Quantinium** (jamais consommé comme combustible) et **Steak de Quantinium** (jamais mangé en entier).

## Compilation
```
JAVA_HOME=<jdk 21> ./gradlew build          # jars dans <chargeur>/build/libs/
scripts/make-mrpack.sh                      # dist/QuantumRift-<chargeur>-<version>.mrpack
scripts/smoke-test.sh [fabric neoforge forge]   # tests serveur sans interface (blocs, monstres, dimension, structures, machines)
python3 tools/validate_assets.py            # contrôle hors ligne des modèles, textures, langues, sons, recettes
```

## Fichiers générés
La plupart des ressources et données sont générées : modifiez les générateurs, pas les fichiers produits. `tools/gen_all.sh` les lance dans l'ordre :
`gen_resources` (objets, outils, recettes de base) → `gen_world` (dimension, biomes) → `gen_content2` (Omnium, modules, machines, interface) →
`gen_structures` (constructions dessinées à la main, avec contrôles de symétrie et aperçus dans `tools/previews/`) → `gen_boss` → `build_sounds` → `gen_mobs`
(Java des monstres à partir de `tools/mob_table.py`).

## Crédits et licence
Voir `CREDITS.md` (les sons sont en CC0, issus d'OpenGameArt). Le mod est placé sous [CC0 1.0](LICENSE). Les textures sont décrites comme dérivées de textures vanilla de Minecraft ou procédurales : Minecraft reste la propriété de Mojang.
