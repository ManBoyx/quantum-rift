#!/usr/bin/env bash
# Dev helper: lays out every item (big item displays), every block and armor stands wearing each armor set on the platform at z=60..90.
cd "$(dirname "$0")/.."
S=scripts/dev-server.sh
$S cmd "fill -10 199 50 70 199 100 minecraft:smooth_stone" 4 >/dev/null; $S cmd "fill -10 200 50 70 215 100 minecraft:air" 4 >/dev/null; $S cmd "kill @e[type=!player]" 1 >/dev/null
$S cmd "forceload add -10 50 70 100" 2 >/dev/null
J="$(dirname "$0")/.."
ITEMS=$(python3 - <<'PY'
import re
src = open("common/src/main/java/com/quantumrift/content/ModItems.java").read()
ids = re.findall(r'(?:item|armor|prismatic)\("([a-z_0-9]+)"', src)
ids += [o + "_upgrade_module" for o in re.findall(r'"(\w+)"', re.search(r'MODULE_ORES = \{([^}]*)\}', src).group(1))]
print(" ".join(dict.fromkeys(ids)))
PY
)
BLOCKS=$(python3 - <<'PY'
import re
print(" ".join(re.findall(r'block\("([a-z_0-9]+)"', open("common/src/main/java/com/quantumrift/content/ModBlocks.java").read())))
PY
)
i=0; for it in $ITEMS; do row=$((i / 18)); col=$((i % 18)); x=$((col * 3)); z=$((60 + row * 4))
  $S cmd "summon item_display $x.5 202 $z.5 {item:{id:\"quantumrift:$it\",count:1},billboard:\"center\",transformation:{left_translation:[0f,0f,0f],left_rotation:[0f,0f,0f,1f],right_rotation:[0f,0f,0f,1f],scale:[1.6f,1.6f,1.6f]}}" 0.25 >/dev/null; i=$((i + 1)); done
i=0; for b in $BLOCKS; do x=$((i * 3)); $S cmd "setblock $x 200 80 quantumrift:$b" 0.25 >/dev/null; i=$((i + 1)); done
x=0; for set in quantite singularity prismatic; do
  $S cmd "summon armor_stand $((x * 4)).5 200 90.5 {ShowArms:1b,Rotation:[180f,0f],HandItems:[{id:\"quantumrift:${set}_sword\",count:1},{}],ArmorItems:[{id:\"quantumrift:${set}_boots\",count:1},{id:\"quantumrift:${set}_leggings\",count:1},{id:\"quantumrift:${set}_chestplate\",count:1},{id:\"quantumrift:${set}_helmet\",count:1}]}" 0.3 >/dev/null; x=$((x + 1)); done
