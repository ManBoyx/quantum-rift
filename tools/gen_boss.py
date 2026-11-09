#!/usr/bin/env python3
"""Paints the Void Sovereign texture (128x128) and its glow overlay procedurally. UV layout must match VoidSovereignModel.java."""
import hashlib, math, os, sys
sys.path.insert(0, os.path.dirname(__file__))
from PIL import Image
from gen_resources import MOD, ROOT

def hsh(*a):
    return int(hashlib.md5(",".join(map(str, a)).encode()).hexdigest()[:6], 16) / 0xFFFFFF

def mix(a, b, t): return tuple(int(a[i] + (b[i] - a[i]) * t) for i in range(3))

DARK, MID, LIGHT = (14, 8, 28), (40, 22, 74), (96, 62, 160)
BONE = (196, 178, 214)
GLOW_A, GLOW_B = (90, 240, 235), (220, 90, 255)

def faces(u, v, w, h, d):
    """Minecraft box UV layout -> {face: (x0, y0, width, height)}"""
    return {"top": (u + d, v, w, d), "bottom": (u + d + w, v, w, d), "right": (u, v + d, d, h), "front": (u + d, v + d, w, h),
            "left": (u + d + w, v + d, d, h), "back": (u + 2 * d + w, v + d, w, h)}

def paint(img, glow, u, v, w, h, d, fn, mirror=False):
    for face, (x0, y0, fw, fh) in faces(u, v, w, h, d).items():
        for fy in range(fh):
            for fx in range(fw):
                base, g = fn(face, fx, fy, fw, fh)
                img.putpixel((x0 + fx, y0 + fy), base + (255,))
                if g: glow.putpixel((x0 + fx, y0 + fy), g + (255,))

def plate(face, fx, fy, fw, fh, salt, light=1.0, band=6):
    """Armour plating: dark noise, brighter edges, horizontal seams."""
    n = hsh(salt, face, fx, fy)
    c = mix(DARK, MID, n * 0.9)
    if fx in (0, fw - 1) or fy in (0, fh - 1): c = mix(c, LIGHT, 0.55 * light)
    elif fy % band == 0: c = mix(c, DARK, 0.6)
    elif fy % band == 1: c = mix(c, LIGHT, 0.25 * light)
    return c

def crack(salt, face, fx, fy, density=0.07):
    """A few connected glowing cracks, deterministic."""
    cx = int(hsh(salt, face, "c") * 40) % 7
    if (fx + fy * 3 + cx) % 11 == 0 and hsh(salt, face, fx, fy, "k") < density * 6: return mix(GLOW_B, GLOW_A, hsh(face, fx, fy))
    return None

def head_fn(face, fx, fy, fw, fh):
    c = plate(face, fx, fy, fw, fh, "head", band=7); g = None
    if face == "front":
        for (ex0, ey) in ((2, 6), (9, 6)):                     # slanted glowing eyes
            for i in range(3):
                if ex0 + i == fx and ey + (i if ex0 < 7 else 2 - i) // 2 == fy: c, g = (255, 255, 255), GLOW_A
            if ex0 <= fx <= ex0 + 2 and fy == ey + 1: c, g = (230, 255, 255), GLOW_A
        if fy == 10 and 3 <= fx <= 10 and fx % 2 == 1: c, g = GLOW_B, GLOW_B       # jagged mouth
        if fy == 11 and 3 <= fx <= 10 and fx % 2 == 0: c, g = GLOW_B, GLOW_B
    else:
        k = crack("head", face, fx, fy)
        if k: c, g = k, k
    return c, g

def torso_fn(face, fx, fy, fw, fh):
    c = plate(face, fx, fy, fw, fh, "torso", band=7); g = None
    if face == "front":
        cx, cy = fw / 2 - 0.5, fh * 0.42
        dx, dy = fx - cx, fy - cy; r = math.hypot(dx, dy); ang = math.degrees(math.atan2(dy, dx)) % 360
        if abs(r - 6.5) < 0.6 or r < 1.2 or (r < 6.5 and min(ang % 45, 45 - ang % 45) < 4):   # sigil: ring + eight spokes + core
            c = mix(GLOW_B, GLOW_A, min(1, r / 7)); g = c
        if fy in (fh - 4,) and 2 <= fx <= fw - 3 and fx % 3 == 0: c = g = GLOW_A    # belt studs
    else:
        k = crack("torso", face, fx, fy)
        if k: c, g = k, k
    return c, g

def arm_fn(face, fx, fy, fw, fh):
    c = plate(face, fx, fy, fw, fh, "arm", band=6); g = None
    if fy >= fh - 5:                                            # glowing knuckles
        if (fx + fy) % 3 == 0: c = g = mix(GLOW_B, GLOW_A, hsh(face, fx))
    else:
        k = crack("arm", face, fx, fy, 0.05)
        if k: c, g = k, k
    return c, g

def leg_fn(face, fx, fy, fw, fh):
    c = plate(face, fx, fy, fw, fh, "leg", band=8); g = None
    if fy >= fh - 3: c = mix(c, DARK, 0.5)
    k = crack("leg", face, fx, fy, 0.03)
    if k: c, g = k, k
    return c, g

def horn_fn(face, fx, fy, fw, fh):
    t = fy / max(1, fh - 1)
    return mix(BONE, (60, 40, 100), t * 0.9), None

def cloak_fn(face, fx, fy, fw, fh):
    t = fy / max(1, fh - 1)
    c = mix(MID, DARK, t); g = None
    if face in ("front", "back"):
        if fy >= fh - 3: c = mix(c, LIGHT, 0.5)
        if hsh("cloak", face, fx, fy) > 0.985: c = g = GLOW_A                  # stars caught in the cloth
    return c, g

def shard_fn(face, fx, fy, fw, fh):
    t = (fx + fy) / (fw + fh)
    c = mix(GLOW_A, GLOW_B, t); return c, mix(GLOW_A, GLOW_B, t)

def main():
    img = Image.new("RGBA", (128, 128), (0, 0, 0, 0)); glow = Image.new("RGBA", (128, 128), (0, 0, 0, 0))
    paint(img, glow, 0, 0, 14, 14, 14, head_fn)
    paint(img, glow, 0, 30, 24, 28, 12, torso_fn)
    paint(img, glow, 74, 0, 8, 30, 8, arm_fn)
    paint(img, glow, 0, 72, 8, 24, 8, leg_fn)
    paint(img, glow, 74, 40, 3, 11, 3, horn_fn)
    paint(img, glow, 32, 72, 22, 34, 2, cloak_fn)
    paint(img, glow, 88, 40, 4, 8, 4, shard_fn)
    d = os.path.join(ROOT, f"assets/{MOD}/textures/entity"); os.makedirs(d, exist_ok=True)
    img.save(os.path.join(d, "void_sovereign.png")); glow.save(os.path.join(d, "void_sovereign_glow.png"))
    prev = Image.new("RGBA", (128 * 4 + 20, 128 * 2 + 10), (50, 50, 50, 255))
    prev.paste(img.resize((512, 512), Image.NEAREST).crop((0, 0, 512, 512)).resize((256, 256), Image.NEAREST), (5, 5), img.resize((256, 256), Image.NEAREST))
    prev = Image.new("RGBA", (532, 266), (50, 50, 50, 255)); prev.paste(img.resize((256, 256), Image.NEAREST), (5, 5), img.resize((256, 256), Image.NEAREST)); prev.paste(glow.resize((256, 256), Image.NEAREST), (271, 5), glow.resize((256, 256), Image.NEAREST))
    prev.save(os.path.join(os.path.dirname(__file__), "previews", "void_sovereign_texture.png"))
    print("boss texture ok")

if __name__ == "__main__":
    main()
