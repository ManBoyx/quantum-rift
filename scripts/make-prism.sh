#!/usr/bin/env bash
# Génère des instances Prism Launcher importables (dist/QuantumRift-<loader>.zip).
# Prérequis: ./gradlew build déjà exécuté. Usage: scripts/make-prism.sh
set -euo pipefail
cd "$(dirname "$0")/.."
MC=1.21.1; FABRIC_LOADER=0.16.14; FABRIC_API=0.116.17+1.21.1
NEO=21.1.265; FORGE=52.1.20; TB=1.21.1-4.1.0.1
VER=$(grep '^version=' gradle.properties | cut -d= -f2)
mkdir -p dist .cache; rm -rf dist/work; mkdir -p dist/work
dl() { [ -s ".cache/$(basename "$2")" ] || curl -fsSL -o ".cache/$(basename "$2")" "$1"; echo ".cache/$(basename "$2")"; }
mk() { # loader name, components json fragment, extra jars...
  local l=$1 comp=$2; shift 2
  local d=dist/work/QuantumRift-$l; mkdir -p "$d/minecraft/mods"
  printf '[General]\nConfigVersion=1.2\nInstanceType=OneSix\nname=Quantum Rift (%s)\nJoinServerOnLaunch=false\nOverrideMemory=true\nMaxMemAlloc=4096\nMinMemAlloc=1024\nOverrideJavaVersion=false\n' "$l" > "$d/instance.cfg"
  printf '{"components":[{"uid":"net.minecraft","version":"%s","important":true}%s],"formatVersion":1}\n' "$MC" "$comp" > "$d/mmc-pack.json"
  cp "$l/build/libs/quantumrift-$l-$MC-$VER.jar" "$d/minecraft/mods/"
  for j in "$@"; do cp "$j" "$d/minecraft/mods/"; done
  (cd dist/work && zip -qr "../QuantumRift-$l.zip" "QuantumRift-$l")
}
FAPI=$(dl "https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/$FABRIC_API/fabric-api-$FABRIC_API.jar" "fabric-api-$FABRIC_API.jar")
tb() { dl "https://maven.minecraftforge.net/com/github/glitchfiend/TerraBlender-$1/$TB/TerraBlender-$1-$TB.jar" "TerraBlender-$1-$TB.jar"; }
mk fabric ",{\"uid\":\"net.fabricmc.fabric-loader\",\"version\":\"$FABRIC_LOADER\"}" "$FAPI" "$(tb fabric)"
mk neoforge ",{\"uid\":\"net.neoforged\",\"version\":\"$NEO\"}" "$(tb neoforge)"
mk forge ",{\"uid\":\"net.minecraftforge\",\"version\":\"$FORGE\"}" "$(tb forge)"
rm -rf dist/work; ls -la dist/*.zip
