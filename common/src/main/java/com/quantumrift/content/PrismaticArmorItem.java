package com.quantumrift.content;

import com.quantumrift.Constants;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;

/** Strongest armor: bonus attack damage per piece, and Strength II + Resistance while the full set is worn. */
public class PrismaticArmorItem extends ArmorItem {

    private static final double DAMAGE_PER_PIECE = 1.5;

    public PrismaticArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers() {
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        for (ItemAttributeModifiers.Entry e : super.getDefaultAttributeModifiers().modifiers()) {
            builder.add(e.attribute(), e.modifier(), e.slot());
        }
        builder.add(Attributes.ATTACK_DAMAGE,
                new AttributeModifier(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "prismatic_" + type.getName()), DAMAGE_PER_PIECE,
                        AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.bySlot(type.getSlot()));
        return builder.build();
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean selected) {
        super.inventoryTick(stack, level, entity, slotId, selected);
        if (level.isClientSide || !(entity instanceof LivingEntity living) || level.getGameTime() % 20 != 0) return;
        if (!(stack.getItem() instanceof ArmorItem armor) || !living.getItemBySlot(armor.getType().getSlot()).equals(stack)) return;
        if (isFullSet(living)) {
            living.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 60, 1, true, false, true));
            living.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 0, true, false, true));
        }
    }

    public static boolean isFullSet(LivingEntity entity) {
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            if (!(entity.getItemBySlot(slot).getItem() instanceof PrismaticArmorItem)) return false;
        }
        return true;
    }
}
