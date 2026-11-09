package com.quantumrift.entity;

import com.quantumrift.content.ModSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** It only moves while nobody is looking at it. Looking away is when it gets close. */
public class GauntWatcher extends Monster implements RangedAttackMob {

    private static final double WATCH_RANGE = 48.0;

    public GauntWatcher(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 40.0).add(Attributes.ATTACK_DAMAGE, 9.0)
                .add(Attributes.MOVEMENT_SPEED, 0.34).add(Attributes.FOLLOW_RANGE, 40.0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide) {
            boolean observed = isObserved();
            if (observed != isNoAi()) {
                setNoAi(observed);
                if (observed) getNavigation().stop();
            }
        }
    }

    /** Same test as an enderman's gaze, but inverted: being seen freezes it. */
    private boolean isObserved() {
        for (Player player : level().players()) {
            if (player.isSpectator() || player.isCreative() || distanceToSqr(player) > WATCH_RANGE * WATCH_RANGE) continue;
            Vec3 look = player.getViewVector(1.0F).normalize();
            Vec3 toMe = new Vec3(getX() - player.getX(), getEyeY() - player.getEyeY(), getZ() - player.getZ());
            double distance = toMe.length();
            if (distance < 0.001) return true;
            if (look.dot(toMe.normalize()) > 1.0 - 0.025 / distance && player.hasLineOfSight(this)) return true;
        }
        return false;
    }

    /** Required only so the vanilla skeleton model can be reused; the watcher never shoots. */
    @Override
    public void performRangedAttack(LivingEntity target, float velocity) {}

    @Override
    public boolean isSunBurnTick() {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.MOB_GAUNT_WATCHER_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.MOB_GAUNT_WATCHER_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.MOB_GAUNT_WATCHER_DEATH.get();
    }
}
