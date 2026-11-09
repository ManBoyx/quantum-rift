#!/usr/bin/env bash
# Builds dist/QuantumRift-<loader>-<version>.mrpack for fabric, neoforge and forge (Modrinth format, importable in Prism Launcher).
# Jars (mod + dependencies) are embedded in overrides/mods, so nothing is downloaded at import time. Run ./gradlew build first.
set -euo pipefail
cd "$(dirname "$0")/.."
MC=1.21.1; FABRIC_LOADER=0.16.14; FABRIC_API=0.116.17+1.21.1; NEO=21.1.265; FORGE=52.1.20; TB=1.21.1-4.1.0.1
VER=$(grep '^version=' gradle.properties | cut -d= -f2)
mkdir -p dist .cache
dl() { [ -s ".cache/$2" ] || curl -fsSL -o ".cache/$2" "$1"; echo ".cache/$2"; }
tb() { dl "https://maven.minecraftforge.net/com/github/glitchfiend/TerraBlender-$1/$TB/TerraBlender-$1-$TB.jar" "TerraBlender-$1-$TB.jar"; }
FAPI=$(dl "https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/$FABRIC_API/fabric-api-$FABRIC_API.jar" "fabric-api-$FABRIC_API.jar")
pack() { # loader, dependency-json, extra jars...
  local l=$1 deps=$2; shift 2
  python3 - "$VER" "$MC" "$l" "$deps" "$l/build/libs/quantumrift-$l-$MC-$VER.jar" "$@" <<'PY'
import json, os, sys, zipfile
ver, mc, loader, deps, *jars = sys.argv[1:]
out = f"dist/QuantumRift-{loader}-{ver}.mrpack"
index = {"formatVersion": 1, "game": "minecraft", "versionId": ver, "name": f"Quantum Rift ({loader.capitalize()})",
         "summary": "Fused horror biomes, rift gates, the Quantum Realm and its boss, 21 mobs, machines, and three tiers of gear up to Prismatic.",
         "files": [], "dependencies": {"minecraft": mc, **json.loads(deps)}}
with zipfile.ZipFile(out, "w", zipfile.ZIP_DEFLATED) as z:
    z.writestr("modrinth.index.json", json.dumps(index, indent=2))
    for j in jars: z.write(j, "overrides/mods/" + os.path.basename(j))
print(out, os.path.getsize(out) // 1024, "KB")
PY
}
pack fabric "{\"fabric-loader\": \"$FABRIC_LOADER\"}" "$FAPI" "$(tb fabric)"
pack neoforge "{\"neoforge\": \"$NEO\"}" "$(tb neoforge)"
pack forge "{\"forge\": \"$FORGE\"}" "$(tb forge)"
