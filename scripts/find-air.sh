#!/usr/bin/env bash
# Dev helper: scans a box around a point for air cells inside a biome and prints the first hits. usage: find-air.sh <biome> x y z [radius]
cd "$(dirname "$0")/.."; S=scripts/dev-server.sh; b=$1; cx=$2; cy=$3; cz=$4; r=${5:-8}
for ((x=cx-r; x<=cx+r; x+=2)); do for ((y=cy-r; y<=cy+r; y+=2)); do for ((z=cz-r; z<=cz+r; z+=2)); do
  echo "execute positioned $x $y $z if biome ~ ~ ~ $b if block ~ ~ ~ minecraft:air if block ~ ~-1 ~ #minecraft:base_stone_overworld run say HIT $x $y $z" > /tmp/qr-overworld.in 2>/dev/null
  echo "execute positioned $x $y $z if biome ~ ~ ~ $b if block ~ ~ ~ minecraft:air if block ~ ~-1 ~ #minecraft:base_stone_overworld run say HIT $x $y $z" > "/tmp/qr-${LOADER:-fabric}.in"; sleep 0.05
done; done; done
