#!/usr/bin/env bash
# Dev helper: puts every mob in a row on a flat platform (y=199) so models/textures can be inspected with a client. Needs a running dev server.
cd "$(dirname "$0")/.."
S=scripts/dev-server.sh
$S cmd "kill @e[type=!player]" 1 >/dev/null
i=0
for m in $(python3 -c "import sys; sys.path.insert(0,'tools'); from mob_table import MOBS; print(' '.join(MOBS))"); do
  row=$((i / 11)); col=$((i % 11)); x=$((col * 6)); z=$((10 + row * 12))
  [ "$m" = void_sovereign ] && { x=30; z=36; }
  $S cmd "summon quantumrift:$m $x.5 200 $z.5 {NoAI:1b,Silent:1b,PersistenceRequired:1b,Rotation:[180f,0f]}" 0.4 >/dev/null
  i=$((i + 1))
done
