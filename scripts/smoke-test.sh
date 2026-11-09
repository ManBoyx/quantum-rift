#!/usr/bin/env bash
# Démarre un serveur headless par loader, exécute quelques commandes de contrôle puis l'arrête.
# Usage: scripts/smoke-test.sh [fabric|neoforge|forge ...]   (défaut: les trois)
cd "$(dirname "$0")/.."
export JAVA_HOME="${JAVA_HOME:-$HOME/Outils/JDK/jdk-21}"
loaders=("$@"); [ ${#loaders[@]} -eq 0 ] && loaders=(fabric neoforge forge)
MOBS=$(python3 -c "import sys; sys.path.insert(0,'tools'); from mob_table import MOBS; print(' '.join(k for k,v in MOBS.items() if not v.get('boss')))")
BOSS=$(python3 -c "import sys,os; sys.path.insert(0,'tools'); from mob_table import MOBS; print(' '.join(k for k,v in MOBS.items() if v.get('boss')) if os.path.exists('common/src/main/java/com/quantumrift/entity/VoidSovereign.java') else '')")
CMDS=(
  "forceload add 0 0"
  "setblock 0 100 0 quantumrift:quantite_ore"
  "setblock 1 100 0 quantumrift:singularity_ore"
  "setblock 2 100 0 quantumrift:omnium_ore"
  "setblock 3 100 0 quantumrift:quantum_furnace"
  "setblock 4 100 0 quantumrift:fusion_chamber"
  "setblock 5 100 0 quantumrift:matter_collector"
  "setblock 6 100 0 quantumrift:quantum_vault"
  "execute in quantumrift:quantum_realm run setblock 0 100 0 quantumrift:quantum_stone"
)
x=0; for m in $MOBS $BOSS; do CMDS+=("execute in quantumrift:quantum_realm run summon quantumrift:$m $((x%40)) 101 $((x/40*3))"); x=$((x+2)); done
CMDS+=(
  'data merge block 4 100 0 {Items:[{Slot:0b,id:"quantumrift:coal_upgrade_module",count:1},{Slot:1b,id:"quantumrift:copper_upgrade_module",count:1},{Slot:2b,id:"quantumrift:iron_upgrade_module",count:1},{Slot:3b,id:"quantumrift:gold_upgrade_module",count:1},{Slot:4b,id:"quantumrift:redstone_upgrade_module",count:1},{Slot:5b,id:"quantumrift:lapis_upgrade_module",count:1},{Slot:6b,id:"quantumrift:diamond_upgrade_module",count:1},{Slot:7b,id:"quantumrift:emerald_upgrade_module",count:1},{Slot:8b,id:"quantumrift:netherite_upgrade_module",count:1},{Slot:9b,id:"quantumrift:quantite_upgrade_module",count:1},{Slot:10b,id:"quantumrift:singularity_upgrade_module",count:1},{Slot:11b,id:"quantumrift:omnium_upgrade_module",count:1},{Slot:12b,id:"quantumrift:sovereign_heart",count:1}]}'
  "item replace block 3 100 0 container.0 with minecraft:raw_iron 8"
  "item replace block 3 100 0 container.1 with quantumrift:quantinium_coal 1"
  "summon item 5 101 0 {Item:{id:\"minecraft:diamond\",count:3},PickupDelay:0s}"
  "#sleep 12"
  "data get block 4 100 0 Items"
  "data get block 3 100 0 Items"
  "data get block 5 100 0 Items"
)
# ore generation: scan generated chunks and count ore blocks by replacing them (marker lines separate the groups)
orescan() { # marker dimension y0 y1 block [x/z base]
  local base=${6:-200}
  CMDS+=("execute in $2 run forceload add $base $base $((base+31)) $((base+31))" "#sleep 7" "say MARK:$1")
  for ((cx=0; cx<2; cx++)); do for ((cz=0; cz<2; cz++)); do
    y=$3; while [ $y -lt $4 ]; do ye=$((y+30)); [ $ye -gt $4 ] && ye=$4
      CMDS+=("execute in $2 run fill $((cx*16+base)) $y $((cz*16+base)) $((cx*16+base+15)) $ye $((cz*16+base+15)) minecraft:stone replace $5"); y=$((ye+1)); done; done; done
}
orescan Q minecraft:overworld -60 60 quantumrift:quantite_ore
orescan Qd minecraft:overworld -60 0 quantumrift:deepslate_quantite_ore
orescan OW minecraft:overworld -60 60 quantumrift:omnium_ore
orescan ON minecraft:the_nether 5 120 quantumrift:nether_omnium_ore
orescan OE minecraft:the_end 30 80 quantumrift:end_omnium_ore 30
orescan S quantumrift:quantum_realm 0 220 quantumrift:singularity_ore
CMDS+=("say MARK:END")
CMDS+=(
  "data get block 3 100 0"
  "data get block 4 100 0"
  "data get block 5 100 0"
  "data get block 6 100 0"
  "execute in quantumrift:quantum_realm positioned 0 100 0 run locate structure quantumrift:sovereign_altar"
  "execute positioned 0 -20 0 run locate biome quantumrift:quantum_crystal_cavern"
  "execute positioned 0 -20 0 run locate biome quantumrift:whispering_hollows"
  "locate biome quantumrift:fused_frostbloom"
  "locate biome quantumrift:fused_scorchwild"
  "locate biome quantumrift:fused_mirewood"
  "locate structure quantumrift:rift_gate"
  "datapack list"
)
rc=0
for l in "${loaders[@]}"; do
  dir="$l/runs/server"; mkdir -p "$dir"; echo "eula=true" > "$dir/eula.txt"
  printf 'online-mode=false\nlevel-seed=424242\nspawn-protection=0\nview-distance=4\n' > "$dir/server.properties"; rm -rf "$dir/world"
  log="$l/runs/smoke.log"; : > "$log"
  echo "== $l"
  ( until grep -q 'Done (' "$log"; do sleep 2; done; for c in "${CMDS[@]}"; do case "$c" in "#sleep "*) sleep "${c#\#sleep }";; *) echo "$c"; sleep 1;; esac; done; sleep 2; echo stop ) \
    | timeout 900 ./gradlew ":$l:runServer" --console=plain >> "$log" 2>&1
  ok=1
  chk() { grep -qE "$1" "$log" || { echo "   FAIL: $2"; ok=0; }; }
  chk 'Done \(' "serveur non démarré"
  chk 'Changed the block at 0, 100, 0' "bloc quantite_ore non enregistré"
  chk 'Changed the block at 1, 100, 0' "bloc singularity_ore non enregistré"
  n=$(grep -c 'Summoned new' "$log"); want=$(echo $MOBS $BOSS | wc -w); [ "$n" -ge "$want" ] || { echo "   FAIL: mobs invoqués $n/$want"; ok=0; }
  for b in 'block at 2, 100, 0' 'block at 3, 100, 0' 'block at 4, 100, 0' 'block at 5, 100, 0' 'block at 6, 100, 0'; do chk "Changed the $b" "bloc $b"; done
  chk 'nearest quantumrift:fused_frostbloom' "biome fused_frostbloom introuvable (TerraBlender?)"
  chk 'nearest quantumrift:rift_gate' "structure rift_gate introuvable"
  chk 'nearest quantumrift:sovereign_altar' "autel du boss introuvable"
  chk 'nearest quantumrift:quantum_crystal_cavern' "grotte cristalline introuvable"
  chk 'nearest quantumrift:whispering_hollows' "creux murmurants introuvable"
  chk 'prismatic_template' "chambre de fusion: modele prismatique non cree"
  chk 'minecraft:iron_ingot' "four quantique: lingots non produits"
  chk 'quantumrift:quantinium_coal' "charbon quantinium absent"
  chk 'minecraft:diamond' "collecteur: diamants non ramasses"
  [ "$(grep -c 'has the following block data' "$log")" -ge 4 ] || { echo "   FAIL: block entities des machines"; ok=0; }
  python3 - "$log" <<'PY' || ok=0
import re, sys
cur, tot = None, {}
for line in open(sys.argv[1], errors="ignore"):
    m = re.search(r"MARK:(\w+)", line)
    if m: cur = m.group(1); tot.setdefault(cur, 0); continue
    m = re.search(r"Successfully filled (\d+) block", line)
    if m and cur: tot[cur] += int(m.group(1))
bad = [k for k in ("Q", "Qd", "OW", "ON", "OE", "S") if tot.get(k, 0) == 0]
print("   ore counts:", {k: v for k, v in tot.items() if k != "END"})
if bad: print("   FAIL: no ore generated for", bad); sys.exit(1)
PY
  # erreurs liées au mod
  if grep -E 'ERROR|Exception|Failed to|Couldn.t parse|Unknown|Missing' "$log" | grep -v 'No data fixer' | grep -iE 'quantumrift|quantum_realm|rift_gate|fused_|terrablender' ; then ok=0; fi
  [ $ok = 1 ] && echo "   PASS" || { echo "   (voir $log)"; rc=1; }
done
exit $rc
