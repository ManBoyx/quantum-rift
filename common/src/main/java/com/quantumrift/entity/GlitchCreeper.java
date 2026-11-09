package com.quantumrift.entity;

import com.quantumrift.content.ModSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;

/** A creeper whose body flickers out of phase: when hurt it often blinks a few blocks away. */
public class GlitchCreeper extends Creeper {

    public GlitchCreeper(EntityType<? extends Creeper> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Creeper.createAttributes().add(Attributes.MAX_HEALTH, 26.0).add(Attributes.MOVEMENT_SPEED, 0.28);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && !level().isClientSide && isAlive() && random.nextFloat() < 0.35F) {
            double x = getX() + (random.nextDouble() - 0.5) * 12.0;
            double z = getZ() + (random.nextDouble() - 0.5) * 12.0;
            randomTeleport(x, getY(), z, true);
        }
        return hurt;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.MOB_GLITCH_CREEPER_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.MOB_GLITCH_CREEPER_DEATH.get();
    }
}
