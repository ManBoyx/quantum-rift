"""Tiny voxel builder for structure templates: place blocks, mirror-check, write vanilla .nbt, render isometric/top/section previews."""
import gzip, math, os, struct
from PIL import Image, ImageDraw

class Voxels:
    def __init__(self, sx, sy, sz):
        self.size = (sx, sy, sz); self.cells = {}

    def set(self, x, y, z, name, **props):
        sx, sy, sz = self.size
        assert 0 <= x < sx and 0 <= y < sy and 0 <= z < sz, f"out of bounds {(x, y, z)} for {self.size} ({name})"
        self.cells[(x, y, z)] = (name if ":" in name else "minecraft:" + name, tuple(sorted((k, str(v).lower() if isinstance(v, bool) else str(v)) for k, v in props.items())))

    def get(self, x, y, z): return self.cells.get((x, y, z))

    def fill(self, x0, y0, z0, x1, y1, z1, name, **props):
        for x in range(min(x0, x1), max(x0, x1) + 1):
            for y in range(min(y0, y1), max(y0, y1) + 1):
                for z in range(min(z0, z1), max(z0, z1) + 1): self.set(x, y, z, name, **props)

    # ---- verification
    def mirror_errors(self, axis="x", ignore=()):
        """Cells whose mirror image (across the centre of `axis`) differs in block id. Facing/rotation properties are mirrored separately."""
        sx, sy, sz = self.size; bad = []
        for (x, y, z), (n, _) in self.cells.items():
            m = (sx - 1 - x, y, z) if axis == "x" else (x, y, sz - 1 - z)
            o = self.cells.get(m)
            if (o is None or o[0] != n) and n not in ignore and (o is None or o[0] not in ignore): bad.append(((x, y, z), n, o[0] if o else None))
        return bad

    # ---- nbt
    def write_nbt(self, path):
        def s(v): b = v.encode(); return struct.pack(">H", len(b)) + b
        def tag(t, name, payload): return bytes([t]) + s(name) + payload
        def i(v): return struct.pack(">i", v)
        def lst(tid, items): return bytes([tid]) + struct.pack(">i", len(items)) + b"".join(items)
        def comp(entries): return b"".join(entries) + b"\x00"
        palette, index = [], {}
        for key in self.cells.values():
            if key not in index: index[key] = len(palette); palette.append(key)
        blocks = [comp([tag(9, "pos", lst(3, [i(x), i(y), i(z)])), tag(3, "state", i(index[v]))])
                  for (x, y, z), v in sorted(self.cells.items(), key=lambda kv: (kv[0][1], kv[0][2], kv[0][0]))]
        pal = []
        for name, props in palette:
            e = [tag(8, "Name", s(name))]
            if props: e.append(tag(10, "Properties", comp([tag(8, k, s(v)) for k, v in props])))
            pal.append(comp(e))
        sx, sy, sz = self.size
        root = tag(10, "", comp([tag(9, "size", lst(3, [i(sx), i(sy), i(sz)])), tag(9, "entities", lst(0, [])), tag(9, "blocks", lst(10, blocks)),
                                  tag(9, "palette", lst(10, pal)), tag(3, "DataVersion", i(3955))]))
        os.makedirs(os.path.dirname(path), exist_ok=True); open(path, "wb").write(gzip.compress(root))

# ---------------------------------------------------------------- preview rendering
PALETTE = [  # (substring, rgb)
    ("rift_gate_core", (150, 90, 230)), ("sovereign_altar", (160, 60, 220)), ("dormant_rift", (70, 40, 140)), ("rift_portal", (180, 110, 255)), ("rift_crystal", (80, 220, 225)),
    ("quantite_block", (70, 190, 190)), ("singularity_block", (130, 60, 190)), ("omnium_block", (190, 200, 250)), ("quantum_stone", (80, 60, 120)),
    ("crying_obsidian", (70, 20, 140)), ("obsidian", (20, 12, 35)), ("amethyst", (170, 120, 230)), ("gilded", (214, 170, 70)), ("soul_campfire", (80, 210, 220)), ("campfire", (230, 120, 40)),
    ("lantern", (120, 230, 240)), ("candle", (235, 225, 200)), ("sea_lantern", (190, 240, 235)), ("bone", (225, 220, 195)), ("skull", (225, 220, 195)), ("chain", (60, 60, 70)),
    ("cobweb", (230, 230, 235)), ("deepslate", (62, 62, 68)), ("polished_blackstone_brick", (52, 46, 58)), ("polished_blackstone", (60, 54, 66)), ("blackstone", (42, 38, 46)),
    ("chiseled", (66, 58, 78)), ("tuff", (96, 92, 84)), ("dirt", (100, 70, 45)), ("stone", (110, 110, 112)), ("glass", (180, 220, 230)), ("air", None)]

def color_of(name):
    for k, c in PALETTE:
        if k in name: return c
    return (140, 90, 90)

def render_iso(v, path, scale=9, title=None, rot=0):
    sx, sy, sz = v.size
    cells = {}
    for (x, y, z), val in v.cells.items():
        for _ in range(rot): x, z = z, sx - 1 - x
        cells[(x, y, z)] = val
    W = int((sx + sz) * scale * 0.9) + 40; H = int((sx + sz) * scale * 0.45 + sy * scale + 60)
    img = Image.new("RGB", (W, H), (24, 24, 32)); d = ImageDraw.Draw(img)
    ox, oy = W // 2, H - 30
    def proj(x, y, z): return (ox + (x - z) * scale * 0.9, oy - (x + z) * scale * 0.45 - y * scale)
    for (x, y, z), (n, props) in sorted(cells.items(), key=lambda kv: (kv[0][0] + kv[0][2], kv[0][1])):
        c = color_of(n)
        if c is None: continue
        half = n.endswith("_slab") or "candle" in n or "lantern" in n or "campfire" in n or "skull" in n or "cobweb" in n or "chain" in n
        top = 0.5 if (half and "slab" in n) else 1.0
        if "candle" in n or "lantern" in n or "skull" in n or "chain" in n: top = 0.55
        p = lambda dx, dy, dz: proj(x + dx, y + dy, z + dz)
        faces = [([p(0, top, 0), p(1, top, 0), p(1, top, 1), p(0, top, 1)], 1.15), ([p(0, 0, 1), p(1, 0, 1), p(1, top, 1), p(0, top, 1)], 0.8), ([p(1, 0, 0), p(1, 0, 1), p(1, top, 1), p(1, top, 0)], 0.62)]
        for pts, k in faces: d.polygon(pts, fill=tuple(min(255, int(ch * k)) for ch in c), outline=tuple(int(ch * k * 0.7) for ch in c))
    if title: d.text((8, 6), title, fill=(220, 220, 230))
    img.save(path)

def render_top(v, path, scale=14):
    sx, sy, sz = v.size
    img = Image.new("RGB", (sx * scale, sz * scale), (24, 24, 32)); d = ImageDraw.Draw(img)
    for x in range(sx):
        for z in range(sz):
            for y in range(sy - 1, -1, -1):
                c = v.cells.get((x, y, z))
                if c and color_of(c[0]):
                    col = color_of(c[0]); shade = 0.6 + 0.4 * y / max(1, sy - 1)
                    d.rectangle([x * scale, z * scale, (x + 1) * scale - 1, (z + 1) * scale - 1], fill=tuple(int(ch * shade) for ch in col)); break
    img.save(path)

def render_section(v, path, z=None, scale=18):
    """Front elevation: the cells of one z slice (default: centre), looking along z."""
    sx, sy, sz = v.size; z = sz // 2 if z is None else z
    img = Image.new("RGB", (sx * scale, sy * scale), (24, 24, 32)); d = ImageDraw.Draw(img)
    for x in range(sx):
        for y in range(sy):
            c = v.cells.get((x, y, z))
            if c and color_of(c[0]):
                d.rectangle([x * scale, (sy - 1 - y) * scale, (x + 1) * scale - 1, (sy - y) * scale - 1], fill=color_of(c[0]), outline=(10, 10, 14))
    img.save(path)
