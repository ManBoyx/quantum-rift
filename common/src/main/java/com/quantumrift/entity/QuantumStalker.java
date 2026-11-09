package com.quantumrift.entity;

import com.quantumrift.content.ModSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.Level;

/** An enderman-like teleporter that lives in the Quantum Realm; tougher and harder-hitting. */
public class QuantumStalker extends EnderMan {

    public QuantumStalker(EntityType<? extends EnderMan> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return EnderMan.createAttributes().add(Attributes.MAX_HEALTH, 45.0).add(Attributes.ATTACK_DAMAGE, 9.0);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.MOB_QUANTUM_STALKER_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.MOB_QUANTUM_STALKER_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.MOB_QUANTUM_STALKER_DEATH.get();
    }
}
