#!/usr/bin/env bash
# Regenerates every generated resource and source, in dependency order. Run from the project root.
set -euo pipefail
cd "$(dirname "$0")/.."
python3 tools/gen_resources.py
python3 tools/gen_world.py
python3 tools/gen_content2.py
python3 tools/gen_advancements.py
python3 tools/gen_structures.py
python3 tools/gen_boss.py
python3 tools/build_sounds.py
python3 tools/gen_mobs.py
