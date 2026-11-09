package com.quantumrift.entity;

import com.quantumrift.content.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * The boss of the Quantum Realm, called up at a Sovereign Altar.
 * Phase 1 (>66% health): melee and summoned phase wraiths.
 * Phase 2 (>33%): adds a gravity pull that drags players in and bursts.
 * Phase 3: adds a leaping slam with a shockwave, and blinks around its target.
 */
public class VoidSovereign extends Monster {

    private static final EntityDataAccessor<Integer> DATA_PHASE = SynchedEntityData.defineId(VoidSovereign.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_CAST = SynchedEntityData.defineId(VoidSovereign.class, EntityDataSerializers.INT);

    private static final int SPAWN_TICKS = 120;
    private static final double ARENA = 28.0;

    private final ServerBossEvent bossEvent = new ServerBossEvent(getDisplayName(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.NOTCHED_10);
    private int spawnTicks = SPAWN_TICKS;
    private int summonCooldown = 200, pullCooldown = 260, slamCooldown = 200, blinkCooldown = 240;
    private int pullBurst;
    private boolean slamming;

    public VoidSovereign(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 250;
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 400.0).add(Attributes.ARMOR, 10.0).add(Attributes.ATTACK_DAMAGE, 14.0)
                .add(Attributes.MOVEMENT_SPEED, 0.27).add(Attributes.FOLLOW_RANGE, 64.0).add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_PHASE, 1);
        builder.define(DATA_CAST, 0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, true));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.6));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 24.0F));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public int getPhase() { return entityData.get(DATA_PHASE); }

    /** Ticks left of the "arms raised" casting pose (client animation). */
    public int getCast() { return entityData.get(DATA_CAST); }

    public int getSpawnTicks() { return spawnTicks; }

    @Override
    protected boolean isImmobile() {
        return spawnTicks > 0 || super.isImmobile();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (spawnTicks > 0 && !source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) return false;
        return super.hurt(source, amount);
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean canUsePortal(boolean allowPassengers) {
        return false;
    }

    @Override
    public boolean shouldDespawnInPeaceful() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        bossEvent.setProgress(getHealth() / getMaxHealth());
        if (getCast() > 0) entityData.set(DATA_CAST, getCast() - 1);
        if (spawnTicks > 0) {
            tickSpawn();
            return;
        }
        float fraction = getHealth() / getMaxHealth();
        int phase = fraction > 0.66F ? 1 : fraction > 0.33F ? 2 : 3;
        if (phase != getPhase()) {
            entityData.set(DATA_PHASE, phase);
            playSound(ModSounds.BOSS_PHASE.get(), 3.0F, 1.0F);
            if (level() instanceof ServerLevel sl) sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), getY() + 2, getZ(), 1, 0, 0, 0, 0);
            summon(3);
        }
        LivingEntity target = getTarget();
        if (target == null) return;
        if (--summonCooldown <= 0) { summon(phase == 1 ? 2 : 3); summonCooldown = phase == 1 ? 420 : 300; }
        if (phase >= 2) {
            if (--pullCooldown <= 0 && distanceToSqr(target) < ARENA * ARENA) { startPull(); pullCooldown = phase == 2 ? 300 : 240; }
            if (pullBurst > 0 && --pullBurst == 0) burst();
        }
        if (phase >= 3) {
            if (--slamCooldown <= 0 && onGround()) { startSlam(); slamCooldown = 220; }
            if (slamming && onGround() && getDeltaMovement().y <= 0.0 && tickCount % 2 == 0 && fallDistance > 0) { shockwave(); slamming = false; }
            if (--blinkCooldown <= 0) { blink(target); blinkCooldown = 200; }
        }
    }

    private void tickSpawn() {
        spawnTicks--;
        if (level() instanceof ServerLevel sl && spawnTicks % 4 == 0) {
            double a = spawnTicks * 0.35;
            sl.sendParticles(ParticleTypes.REVERSE_PORTAL, getX() + Math.cos(a) * 2.5, getY() + (SPAWN_TICKS - spawnTicks) / 30.0, getZ() + Math.sin(a) * 2.5, 2, 0, 0.1, 0, 0.02);
        }
        if (spawnTicks == SPAWN_TICKS - 1) playSound(ModSounds.BOSS_SUMMON.get(), 4.0F, 1.0F);
        if (spawnTicks == 0 && level() instanceof ServerLevel sl) sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), getY() + 2, getZ(), 1, 0, 0, 0, 0);
    }

    private void summon(int count) {
        if (!(level() instanceof ServerLevel sl)) return;
        AABB area = getBoundingBox().inflate(ARENA);
        if (sl.getEntitiesOfClass(PhaseWraith.class, area).size() >= 6) return;
        for (int i = 0; i < count; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            PhaseWraith wraith = ModEntities.PHASE_WRAITH.get().create(sl);
            if (wraith == null) continue;
            wraith.moveTo(getX() + Math.cos(angle) * 4, getY() + 2.5, getZ() + Math.sin(angle) * 4, random.nextFloat() * 360F, 0);
            wraith.finalizeSpawn(sl, sl.getCurrentDifficultyAt(blockPosition()), MobSpawnType.MOB_SUMMONED, null);
            wraith.setTarget(getTarget());
            sl.addFreshEntity(wraith);
        }
        sl.sendParticles(ParticleTypes.PORTAL, getX(), getY() + 2, getZ(), 40, 1.5, 1.5, 1.5, 0.3);
    }

    private void startPull() {
        entityData.set(DATA_CAST, 40);
        playSound(ModSounds.BOSS_PULL.get(), 4.0F, 1.0F);
        for (Player p : level().getEntitiesOfClass(Player.class, getBoundingBox().inflate(ARENA), pl -> !pl.isSpectator())) {
            Vec3 pullVec = position().add(0, 1, 0).subtract(p.position()).normalize();
            p.setDeltaMovement(p.getDeltaMovement().add(pullVec.x * 1.3, 0.35, pullVec.z * 1.3));
            p.hurtMarked = true;
            p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1));
        }
        pullBurst = 30;
    }

    private void burst() {
        if (level() instanceof ServerLevel sl) sl.sendParticles(ParticleTypes.EXPLOSION, getX(), getY() + 1, getZ(), 6, 2, 1, 2, 0);
        for (Player p : level().getEntitiesOfClass(Player.class, getBoundingBox().inflate(6.0), pl -> !pl.isSpectator())) {
            p.hurt(damageSources().mobAttack(this), 10.0F);
            Vec3 away = p.position().subtract(position()).normalize();
            p.setDeltaMovement(p.getDeltaMovement().add(away.x * 1.1, 0.5, away.z * 1.1));
            p.hurtMarked = true;
        }
    }

    private void startSlam() {
        entityData.set(DATA_CAST, 30);
        setDeltaMovement(getDeltaMovement().add(0, 1.15, 0));
        slamming = true;
        fallDistance = 0.0F;
    }

    private void shockwave() {
        playSound(ModSounds.BOSS_SLAM.get(), 4.0F, 1.0F);
        if (level() instanceof ServerLevel sl) sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), getY(), getZ(), 1, 0, 0, 0, 0);
        for (Player p : level().getEntitiesOfClass(Player.class, getBoundingBox().inflate(9.0), pl -> !pl.isSpectator())) {
            p.hurt(damageSources().mobAttack(this), 14.0F);
            Vec3 away = p.position().subtract(position()).normalize();
            p.setDeltaMovement(p.getDeltaMovement().add(away.x * 1.6, 0.9, away.z * 1.6));
            p.hurtMarked = true;
            p.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 30));
        }
    }

    private void blink(LivingEntity target) {
        double angle = random.nextDouble() * Math.PI * 2;
        if (randomTeleport(target.getX() + Math.cos(angle) * 6, target.getY(), target.getZ() + Math.sin(angle) * 6, true)) {
            playSound(ModSounds.BOSS_PHASE.get(), 2.0F, 1.4F);
        }
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossEvent.removePlayer(player);
    }

    @Override
    public void setCustomName(@org.jetbrains.annotations.Nullable Component name) {
        super.setCustomName(name);
        bossEvent.setName(getDisplayName());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("SpawnTicks", spawnTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        spawnTicks = tag.contains("SpawnTicks") ? tag.getInt("SpawnTicks") : 0;
        if (hasCustomName()) bossEvent.setName(getDisplayName());
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.MOB_VOID_SOVEREIGN_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.MOB_VOID_SOVEREIGN_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.MOB_VOID_SOVEREIGN_DEATH.get();
    }

    @Override
    protected void playStepSound(net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
        playSound(SoundEvents.IRON_GOLEM_STEP, 1.0F, 0.6F);
    }
}
