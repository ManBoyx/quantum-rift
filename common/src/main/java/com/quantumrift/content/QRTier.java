package com.quantumrift.content;

import com.quantumrift.Constants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

/** Quantite sits between iron and diamond; Singularity is stronger than netherite. */
public enum QRTier implements Tier {
    // uses, speed, attack bonus, enchantability, blocks it can NOT mine, repair item
    QUANTITE(900, 7.0F, 2.5F, 12, "incorrect_for_quantite_tool", () -> Ingredient.of(ModItems.QUANTITE_INGOT.get())),
    SINGULARITY(3500, 10.0F, 5.0F, 18, "incorrect_for_singularity_tool", () -> Ingredient.of(ModItems.SINGULARITY_INGOT.get())),
    PRISMATIC(7000, 14.0F, 7.5F, 25, "incorrect_for_prismatic_tool", () -> Ingredient.of(ModItems.OMNIUM_INGOT.get()));

    private final int uses;
    private final float speed;
    private final float damage;
    private final int enchantability;
    private final TagKey<Block> incorrect;
    private final Supplier<Ingredient> repair;

    QRTier(int uses, float speed, float damage, int enchantability, String incorrectTag, Supplier<Ingredient> repair) {
        this.uses = uses;
        this.speed = speed;
        this.damage = damage;
        this.enchantability = enchantability;
        this.incorrect = TagKey.create(net.minecraft.core.registries.Registries.BLOCK,
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, incorrectTag));
        this.repair = repair;
    }

    @Override public int getUses() { return uses; }
    @Override public float getSpeed() { return speed; }
    @Override public float getAttackDamageBonus() { return damage; }
    @Override public TagKey<Block> getIncorrectBlocksForDrops() { return incorrect; }
    @Override public int getEnchantmentValue() { return enchantability; }
    @Override public Ingredient getRepairIngredient() { return repair.get(); }
}
