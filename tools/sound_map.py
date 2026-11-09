"""Which downloaded CC0 clip plays for which sound event.
raw dirs (under ~/Projets/Minecraft-QuantumRift/sound-dl/raw): C1=creature80_1 C2=creature80_2 L=monster_pack_lfa Y=monster_pack_yart
S=snarls_* X5=scifi50 X6=scifi60 F=sfx100 D=dark_amb H=ambhorror
Value: (source, pitch, volume[, trim_seconds])"""
RAW = {"C1": "creature80_1", "C2": "creature80_2", "L": "monster_pack_lfa", "Y": "monster_pack_yart", "X5": "scifi50", "X6": "scifi60",
       "F": "sfx100", "D": "dark_amb", "H": "ambhorror", "S1": "snarls_monster-snarl-5.ogg", "S2": "snarls_monster-snarl-attack.ogg",
       "S3": "snarls_monster-snarls_0.ogg", "S4": "snarls_monster-snarls-2_0.ogg"}

MOBS = {  # mob: (ambient, hurt, death)
    # --- Quantum Realm
    "phase_wraith":        (("C1/weird_02.ogg", 0.8, 0.9), ("C2/alien_11.ogg", 1.1, 1.0), ("C1/ooh.ogg", 0.7, 1.0)),
    "quantum_stalker":     (("C1/alien_01.ogg", 0.7, 0.9), ("C2/alien_08.ogg", 0.8, 1.0), ("C2/alien_09.ogg", 0.6, 1.0)),
    "entropy_crawler":     (("C2/bug_05.ogg", 0.8, 0.8), ("C2/bug_08.ogg", 0.9, 1.0), ("C2/bug_12.ogg", 0.7, 1.0)),
    "void_jelly":          (("C2/slime_03.ogg", 0.7, 0.8), ("C2/slime_06.ogg", 0.8, 1.0), ("C2/slime_08.ogg", 0.6, 1.0)),
    "singularity_behemoth": (("C1/grunt_03.ogg", 0.55, 1.0), ("C2/grunt_07.ogg", 0.6, 1.0), ("C2/roar_05.ogg", 0.6, 1.0)),
    "singularity_sentinel": (("F/machine_02.ogg", 0.6, 0.8), ("F/metal_08.ogg", 0.7, 1.0), ("F/slam_05.ogg", 0.6, 1.0)),
    "void_phantom":        (("C1/weird_04.ogg", 0.7, 0.8), ("C1/misc_03.ogg", 0.9, 1.0), ("C2/weird_08.ogg", 0.6, 1.0)),
    "ion_wisp":            (("X5/weird_01.ogg", 1.0, 0.7), ("X6/sfx_10b.ogg", 1.0, 1.0), ("X5/explosion_02.ogg", 1.2, 0.9)),
    "glitch_creeper":      (("X5/weird_03.ogg", 0.9, 0.7), ("X6/sfx_20a.ogg", 0.9, 1.0), ("X5/retro_explosion.ogg", 0.8, 1.0)),
    "shard_mite":          (("C2/bug_10.ogg", 1.2, 0.7), ("C2/bug_13.ogg", 1.2, 1.0), ("C2/bug_09.ogg", 1.1, 1.0)),
    # --- Horror (fused biomes) and caves
    "hollow_walker":       (("L/Monster-4.wav", 0.7, 0.9), ("C1/hurt_01.ogg", 0.7, 1.0), ("C2/die_02.ogg", 0.7, 1.0)),
    "gaunt_watcher":       (("C2/human_03.ogg", 0.6, 0.8), ("C2/human_05.ogg", 0.7, 1.0), ("C2/human_07.ogg", 0.6, 1.0)),
    "flesh_amalgam":       (("L/monster-12.wav", 0.6, 1.0), ("C1/monster_03.ogg", 0.7, 1.0), ("C2/monster_20.ogg", 0.7, 1.0)),
    "bone_choir":          (("C2/human_02.ogg", 0.9, 0.8), ("C1/hurt_04.ogg", 1.1, 1.0), ("C2/die_03.ogg", 0.9, 1.0)),
    "plague_swarm":        (("C1/bug_03.ogg", 1.1, 0.7), ("C1/bug_04.ogg", 1.1, 1.0), ("C2/bug_06.ogg", 1.0, 1.0)),
    "mire_hag":            (("C2/human_06.ogg", 0.8, 0.9), ("C1/hurt_03.ogg", 0.8, 1.0), ("C1/scream_01.ogg", 0.7, 1.0)),
    "scarecrow_effigy":    (("C1/weird_03.ogg", 0.7, 0.8), ("C1/hurt_05.ogg", 0.8, 1.0), ("C1/scream_02.ogg", 0.6, 1.0)),
    "ashen_revenant":      (("L/Monster-6.wav", 0.8, 0.9), ("C2/grunt_06.ogg", 0.7, 1.0), ("L/monster-14.wav", 0.7, 1.0)),
    "geode_beetle":        (("C1/bug_01.ogg", 0.9, 0.7), ("C1/bug_02.ogg", 0.9, 1.0), ("C2/bug_07.ogg", 0.8, 1.0)),
    "abyss_lurker":        (("Y/monster-8.wav", 0.7, 0.9), ("Y/monster-3.wav", 0.8, 1.0), ("Y/monster-6.wav", 0.7, 1.0)),
    # --- boss
    "void_sovereign":      (("S3/monster-snarls_0.ogg", 0.6, 1.0, 3.0), ("C2/roar_06.ogg", 0.55, 1.0), ("S2/monster-snarl-attack.ogg", 0.5, 1.0, 3.5)),
}
EXTRA = {  # generic events (path: source)
    "boss.summon":        ("X6/sfx_16a.ogg", 0.8, 1.0),
    "boss.pull":          ("X6/sfx_06.ogg", 0.7, 1.0),
    "boss.slam":          ("X5/explosion_01.ogg", 0.6, 1.0),
    "boss.phase":         ("X6/sfx_18a.ogg", 0.7, 1.0),
    "block.portal.ambient": ("X5/loop_ambient_01.ogg", 1.0, 0.6),
    "block.portal.travel": ("X5/teleport_02.ogg", 0.9, 1.0),
    "block.gate.rod":     ("F/key_open_01.ogg", 0.7, 1.0),
    "block.gate.awaken":  ("F/gong_02.ogg", 0.6, 1.0),
    "block.altar.activate": ("F/gong_01.ogg", 0.5, 1.0),
    "block.machine.hum":  ("X5/loop_machine_01.ogg", 1.0, 0.6),
    "block.machine.done": ("X5/beep_02.ogg", 0.8, 0.8),
    "block.fusion.complete": ("X6/sfx_18b.ogg", 0.9, 1.0),
    "block.collector.pick": ("X5/misc_03.ogg", 1.0, 0.6),
    "block.crystal.chime": ("F/bell_02.ogg", 1.0, 0.7),
    "block.crystal.break": ("F/glass_04.ogg", 0.8, 0.9),
    "ambient.horror.1":   ("H/ambient_horror_0.ogg", 1.0, 0.9, 11.0),
    "ambient.horror.2":   ("D/ambience-5.wav", 1.0, 0.9),
    "ambient.horror.3":   ("D/ambience-1.wav", 0.8, 0.8),
    "ambient.horror.4":   ("C2/human_08.ogg", 0.5, 0.6),     # distant, pitched-down voice
    "ambient.cave.1":     ("D/ambience-2.wav", 0.7, 0.8),
    "ambient.cave.2":     ("D/ambience-3.wav", 0.7, 0.8),
    "ambient.quantum.1":  ("X5/loop_ambient_weird.ogg", 1.0, 0.8),
    "ambient.quantum.2":  ("D/ambience-4.wav", 0.6, 0.8),
}
