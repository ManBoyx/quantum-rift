package com.quantumrift.entity;

import com.quantumrift.content.ModSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.Level;

/** Spider-like crawler whose bite slows the victim down. */
public class EntropyCrawler extends Spider {

    public EntropyCrawler(EntityType<? extends Spider> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Spider.createAttributes().add(Attributes.MAX_HEALTH, 24.0).add(Attributes.ATTACK_DAMAGE, 4.0);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
        }
        return hit;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.MOB_ENTROPY_CRAWLER_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.MOB_ENTROPY_CRAWLER_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.MOB_ENTROPY_CRAWLER_DEATH.get();
    }
}
