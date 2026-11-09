package com.quantumrift.entity;

import com.quantumrift.content.ModSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.Level;

/** A slime whose touch makes the victim levitate. */
public class VoidJelly extends Slime {

    public VoidJelly(EntityType<? extends Slime> type, Level level) {
        super(type, level);
    }

    public static net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes();
    }

    @Override
    protected void dealDamage(LivingEntity target) {
        super.dealDamage(target);
        if (isAlive()) {
            target.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 60));
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.MOB_VOID_JELLY_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.MOB_VOID_JELLY_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.MOB_VOID_JELLY_DEATH.get();
    }
}
