package com.quantumrift.entity;

import com.quantumrift.content.ModSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

/** Flies through walls like a vex, but hits harder and saps the target's strength. */
public class PhaseWraith extends Vex {

    public PhaseWraith(EntityType<? extends Vex> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Vex.createAttributes().add(Attributes.MAX_HEALTH, 24.0).add(Attributes.ATTACK_DAMAGE, 6.0);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100));
        }
        return hit;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.MOB_PHASE_WRAITH_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.MOB_PHASE_WRAITH_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.MOB_PHASE_WRAITH_DEATH.get();
    }
}
