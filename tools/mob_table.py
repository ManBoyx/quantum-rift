"""Single source of truth for the mobs: Java generation (gen_mobs.py) and assets/data (gen_world.py) both read this.
Fields: cls, parent, attrs (java expr, None => hand-written class provides createAttributes()), size (w, h), spawn ('ground'|'air'),
tex [(vanilla texture, our name)], palette, egg (colour, colour), loot [(item, min, max)], gen (generate the class?), extra (java members),
renderer (vanilla renderer simple name), scale (renderer scale or None), ambient (has ambient sound)."""
QUANTITE = [(5, 40, 50), (20, 110, 120), (80, 210, 200), (200, 255, 245)]
SINGULARITY = [(8, 0, 20), (50, 10, 90), (140, 50, 210), (230, 150, 255)]

SUNLESS_ZOMBIE = """
    @Override protected boolean isSunSensitive() { return false; }
    @Override protected boolean convertsInWater() { return false; }
"""
SUNLESS_SKELETON = """
    @Override public boolean isSunBurnTick() { return false; }
"""
def on_hit(effect, ticks, amp=0):
    return f"""
    @Override
    public boolean doHurtTarget(Entity target) {{
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity living) living.addEffect(new MobEffectInstance({effect}, {ticks}, {amp}));
        return hit;
    }}
"""

MOBS = {
 # ---------------- hand-written classes (kept as they are, only sounds added by hand)
 "phase_wraith": dict(en="Phase Wraith", fr="Spectre de phase", cls="PhaseWraith", gen=False, size=(0.4, 0.8), spawn="air", renderer="VexRenderer",
     tex=[("entity/illager/vex", "phase_wraith"), ("entity/illager/vex_charging", "phase_wraith_charging")], palette=[(20, 5, 50), (60, 40, 150), (120, 200, 255), (255, 255, 255)],
     egg=((60, 40, 150), (120, 230, 255)), loot=[("minecraft:amethyst_shard", 1, 3)], realm="quantum"),
 "quantum_stalker": dict(en="Quantum Stalker", fr="Rodeur quantique", cls="QuantumStalker", gen=False, size=(0.6, 2.9), spawn="ground", renderer="EndermanRenderer",
     tex=[("entity/enderman/enderman", "quantum_stalker")], palette=[(5, 0, 15), (30, 10, 70), (110, 40, 200), (240, 140, 255)], egg=((30, 10, 70), (200, 90, 255)),
     loot=[("minecraft:ender_pearl", 0, 2), ("quantumrift:raw_quantite", 0, 1)], realm="quantum"),
 "entropy_crawler": dict(en="Entropy Crawler", fr="Rampant d'entropie", cls="EntropyCrawler", gen=False, size=(1.4, 0.9), spawn="ground", renderer="SpiderRenderer",
     tex=[("entity/spider/spider", "entropy_crawler")], palette=[(10, 25, 30), (25, 90, 100), (60, 200, 190), (200, 255, 240)], egg=((25, 90, 100), (110, 255, 220)),
     loot=[("minecraft:string", 0, 2), ("minecraft:spider_eye", 0, 1)], realm="quantum"),
 "void_jelly": dict(en="Void Jelly", fr="Gelee du vide", cls="VoidJelly", gen=False, size=(0.52, 0.52), spawn="ground", renderer="SlimeRenderer",
     tex=[("entity/slime/slime", "void_jelly")], palette=[(15, 0, 35), (80, 20, 150), (170, 70, 230), (255, 180, 255)], egg=((80, 20, 150), (255, 160, 255)),
     loot=[("minecraft:slime_ball", 1, 3), ("minecraft:ender_pearl", 0, 1)], realm="quantum"),
 # ---------------- Quantum Realm
 "singularity_behemoth": dict(en="Singularity Behemoth", fr="Behemoth de singularite", cls="SingularityBehemoth", parent="Ravager", pkg="monster",
     attrs="Ravager.createAttributes().add(Attributes.MAX_HEALTH, 110.0).add(Attributes.ATTACK_DAMAGE, 14.0)", size=(1.95, 2.2), spawn="ground", renderer="RavagerRenderer",
     tex=[("entity/illager/ravager", "singularity_behemoth")], palette=SINGULARITY, egg=((50, 10, 90), (230, 150, 255)),
     loot=[("quantumrift:singularity_shard", 1, 3)], realm="quantum"),
 "singularity_sentinel": dict(en="Singularity Sentinel", fr="Sentinelle de singularite", cls="SingularitySentinel", parent="IronGolem", pkg="animal",
     attrs="IronGolem.createAttributes().add(Attributes.MAX_HEALTH, 120.0).add(Attributes.ATTACK_DAMAGE, 16.0)", size=(1.4, 2.7), spawn="ground", renderer="IronGolemRenderer",
     tex=[("entity/iron_golem/iron_golem", "singularity_sentinel")], palette=[(15, 10, 30), (60, 40, 100), (130, 100, 200), (220, 200, 255)], egg=((60, 40, 100), (220, 200, 255)),
     loot=[("quantumrift:quantite_ingot", 1, 3)], realm="quantum",
     extra="""
    @Override
    protected void registerGoals() {
        super.registerGoals();
        // unlike the village golem, these hunt players
        this.targetSelector.addGoal(1, new net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<>(this, net.minecraft.world.entity.player.Player.class, true));
    }
"""),
 "void_phantom": dict(en="Void Phantom", fr="Phantome du vide", cls="VoidPhantom", parent="Phantom", pkg="monster",
     attrs="Monster.createMonsterAttributes().add(Attributes.ATTACK_DAMAGE, 5.0).add(Attributes.MAX_HEALTH, 28.0)", size=(0.9, 0.5), spawn="air", renderer="PhantomRenderer",
     tex=[("entity/phantom", "void_phantom")], palette=[(5, 0, 15), (40, 15, 80), (110, 60, 190), (220, 160, 255)], egg=((40, 15, 80), (220, 160, 255)),
     loot=[("minecraft:phantom_membrane", 0, 2)], realm="quantum", ambient=True),
 "ion_wisp": dict(en="Ion Wisp", fr="Feu follet ionique", cls="IonWisp", parent="Blaze", pkg="monster",
     attrs="Blaze.createAttributes().add(Attributes.MAX_HEALTH, 26.0)", size=(0.6, 1.8), spawn="air", renderer="BlazeRenderer",
     tex=[("entity/blaze", "ion_wisp")], palette=[(40, 0, 60), (150, 20, 200), (255, 90, 255), (255, 230, 255)], egg=((150, 20, 200), (255, 230, 255)),
     loot=[("minecraft:blaze_rod", 0, 1), ("minecraft:amethyst_shard", 0, 2)], realm="quantum"),
 "shard_mite": dict(en="Shard Mite", fr="Mite d'eclat", cls="ShardMite", parent="Endermite", pkg="monster",
     attrs="Endermite.createAttributes()", size=(0.4, 0.3), spawn="ground", renderer="EndermiteRenderer",
     tex=[("entity/endermite", "shard_mite")], palette=QUANTITE, egg=((20, 110, 120), (200, 255, 245)),
     loot=[("minecraft:amethyst_shard", 0, 1)], realm="quantum"),
 "glitch_creeper": dict(en="Glitch Creeper", fr="Creeper glitche", cls="GlitchCreeper", gen=False, size=(0.6, 1.7), spawn="ground", renderer="CreeperRenderer",
     tex=[("entity/creeper/creeper", "glitch_creeper")], palette=[(0, 20, 20), (0, 130, 110), (255, 60, 200), (255, 255, 255)], egg=((0, 130, 110), (255, 60, 200)),
     loot=[("minecraft:gunpowder", 0, 2), ("minecraft:ender_pearl", 0, 1)], realm="quantum"),
 # ---------------- Horror: fused biomes
 "hollow_walker": dict(en="Hollow Walker", fr="Marcheur creux", cls="HollowWalker", parent="Zombie", pkg="monster",
     attrs="Zombie.createAttributes().add(Attributes.MAX_HEALTH, 38.0).add(Attributes.ATTACK_DAMAGE, 7.0).add(Attributes.MOVEMENT_SPEED, 0.19)", size=(0.6, 1.95), spawn="ground", renderer="ZombieRenderer",
     tex=[("entity/zombie/zombie", "hollow_walker")], palette=[(10, 10, 12), (60, 60, 64), (150, 150, 150), (235, 235, 225)], egg=((60, 60, 64), (235, 235, 225)),
     loot=[("minecraft:bone", 1, 3), ("minecraft:rotten_flesh", 0, 2)], realm="horror", extra=SUNLESS_ZOMBIE),
 "gaunt_watcher": dict(en="Gaunt Watcher", fr="Guetteur decharne", cls="GauntWatcher", gen=False, size=(0.6, 2.2), spawn="ground", renderer="HumanoidMobRenderer",
     tex=[("entity/skeleton/skeleton", "gaunt_watcher")], palette=[(8, 8, 8), (50, 48, 40), (150, 145, 120), (235, 230, 200)], egg=((50, 48, 40), (235, 230, 200)),
     loot=[("minecraft:bone", 1, 2), ("minecraft:ender_pearl", 0, 1)], realm="horror"),
 "flesh_amalgam": dict(en="Flesh Amalgam", fr="Amalgame de chair", cls="FleshAmalgam", parent="Zombie", pkg="monster",
     attrs="Zombie.createAttributes().add(Attributes.MAX_HEALTH, 75.0).add(Attributes.ATTACK_DAMAGE, 10.0).add(Attributes.MOVEMENT_SPEED, 0.2).add(Attributes.KNOCKBACK_RESISTANCE, 0.5)",
     size=(0.9, 2.6), spawn="ground", renderer="ZombieRenderer", scale=1.35,
     tex=[("entity/zombie/zombie", "flesh_amalgam")], palette=[(40, 5, 10), (110, 20, 30), (190, 70, 80), (240, 160, 150)], egg=((110, 20, 30), (240, 160, 150)),
     loot=[("minecraft:rotten_flesh", 2, 5), ("minecraft:bone", 1, 3)], realm="horror", extra=SUNLESS_ZOMBIE),
 "bone_choir": dict(en="Bone Choir", fr="Choeur d'os", cls="BoneChoir", parent="Skeleton", pkg="monster",
     attrs="AbstractSkeleton.createAttributes().add(Attributes.MAX_HEALTH, 24.0)", size=(0.6, 1.99), spawn="ground", renderer="SkeletonRenderer",
     tex=[("entity/skeleton/skeleton", "bone_choir")], palette=[(25, 20, 15), (90, 80, 60), (200, 190, 160), (250, 245, 225)], egg=((90, 80, 60), (250, 245, 225)),
     loot=[("minecraft:bone", 1, 3), ("minecraft:arrow", 0, 2)], realm="horror", extra=SUNLESS_SKELETON),
 "plague_swarm": dict(en="Plague Swarm", fr="Essaim de peste", cls="PlagueSwarm", parent="Silverfish", pkg="monster",
     attrs="Silverfish.createAttributes().add(Attributes.ATTACK_DAMAGE, 2.0)", size=(0.4, 0.3), spawn="ground", renderer="SilverfishRenderer",
     tex=[("entity/silverfish", "plague_swarm")], palette=[(20, 25, 5), (80, 100, 20), (150, 170, 50), (220, 230, 120)], egg=((80, 100, 20), (220, 230, 120)),
     loot=[("minecraft:spider_eye", 0, 1)], realm="horror", extra=on_hit("MobEffects.POISON", 100)),
 "mire_hag": dict(en="Mire Hag", fr="Sorciere des marais", cls="MireHag", parent="Witch", pkg="monster",
     attrs="Witch.createAttributes().add(Attributes.MAX_HEALTH, 36.0)", size=(0.6, 1.95), spawn="ground", renderer="WitchRenderer",
     tex=[("entity/witch", "mire_hag")], palette=[(10, 20, 10), (30, 70, 40), (90, 140, 70), (200, 230, 160)], egg=((30, 70, 40), (200, 230, 160)),
     loot=[("minecraft:glass_bottle", 0, 2), ("minecraft:fermented_spider_eye", 0, 1)], realm="horror"),
 "scarecrow_effigy": dict(en="Scarecrow Effigy", fr="Effigie d'epouvantail", cls="ScarecrowEffigy", parent="Pillager", pkg="monster",
     attrs="Pillager.createAttributes().add(Attributes.MAX_HEALTH, 34.0)", size=(0.6, 1.95), spawn="ground", renderer="PillagerRenderer",
     tex=[("entity/illager/pillager", "scarecrow_effigy")], palette=[(25, 15, 5), (100, 70, 25), (190, 150, 60), (240, 220, 130)], egg=((100, 70, 25), (240, 220, 130)),
     loot=[("minecraft:wheat", 1, 3), ("minecraft:arrow", 0, 3)], realm="horror"),
 "ashen_revenant": dict(en="Ashen Revenant", fr="Revenant cendreux", cls="AshenRevenant", parent="WitherSkeleton", pkg="monster",
     attrs="AbstractSkeleton.createAttributes().add(Attributes.MAX_HEALTH, 34.0).add(Attributes.ATTACK_DAMAGE, 8.0)", size=(0.7, 2.4), spawn="ground", renderer="WitherSkeletonRenderer",
     tex=[("entity/skeleton/wither_skeleton", "ashen_revenant")], palette=[(10, 10, 10), (50, 50, 55), (110, 110, 115), (190, 190, 195)], egg=((50, 50, 55), (190, 190, 195)),
     loot=[("minecraft:coal", 1, 3), ("minecraft:bone", 0, 2)], realm="horror"),
 # ---------------- Caves
 "geode_beetle": dict(en="Geode Beetle", fr="Scarabee de geode", cls="GeodeBeetle", parent="Silverfish", pkg="monster",
     attrs="Silverfish.createAttributes().add(Attributes.MAX_HEALTH, 14.0).add(Attributes.ATTACK_DAMAGE, 3.0)", size=(0.5, 0.35), spawn="ground", renderer="SilverfishRenderer",
     tex=[("entity/silverfish", "geode_beetle")], palette=QUANTITE, egg=((20, 110, 120), (200, 255, 245)),
     loot=[("quantumrift:rift_crystal", 0, 1), ("minecraft:amethyst_shard", 0, 2)], realm="cave", extra=on_hit("MobEffects.DIG_SLOWDOWN", 120)),
 "abyss_lurker": dict(en="Abyss Lurker", fr="Rodeur des abysses", cls="AbyssLurker", parent="Spider", pkg="monster",
     attrs="Spider.createAttributes().add(Attributes.MAX_HEALTH, 34.0).add(Attributes.ATTACK_DAMAGE, 7.0)", size=(2.0, 1.1), spawn="ground", renderer="SpiderRenderer", scale=1.6,
     tex=[("entity/spider/spider", "abyss_lurker")], palette=[(2, 5, 15), (10, 25, 60), (40, 80, 150), (140, 200, 255)], egg=((10, 25, 60), (140, 200, 255)),
     loot=[("minecraft:string", 1, 3), ("minecraft:fermented_spider_eye", 0, 1)], realm="cave", extra=on_hit("MobEffects.BLINDNESS", 80)),
 # ---------------- Boss
 "void_sovereign": dict(en="Void Sovereign", fr="Souverain du vide", cls="VoidSovereign", gen=False, size=(2.2, 4.4), spawn="ground", renderer=None,
     tex=[], palette=SINGULARITY, egg=((20, 5, 40), (200, 90, 255)), loot=[("quantumrift:sovereign_heart", 1, 1), ("quantumrift:singularity_ingot", 3, 6), ("quantumrift:omnium_ingot", 4, 8)], realm="boss", boss=True),
}
