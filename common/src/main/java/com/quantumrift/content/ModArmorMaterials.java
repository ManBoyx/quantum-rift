package com.quantumrift.content;

import com.quantumrift.Constants;
import com.quantumrift.registry.Registrar;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.EnumMap;
import java.util.List;

public final class ModArmorMaterials {

    public static final Holder<ArmorMaterial> QUANTITE = Registrar.registerHolder(BuiltInRegistries.ARMOR_MATERIAL, "quantite",
            () -> new ArmorMaterial(defense(3, 6, 7, 2, 9), 12, SoundEvents.ARMOR_EQUIP_DIAMOND,
                    () -> Ingredient.of(ModItems.QUANTITE_INGOT.get()), layers("quantite"), 1.0F, 0.0F));

    public static final Holder<ArmorMaterial> SINGULARITY = Registrar.registerHolder(BuiltInRegistries.ARMOR_MATERIAL, "singularity",
            () -> new ArmorMaterial(defense(4, 7, 9, 4, 13), 18, SoundEvents.ARMOR_EQUIP_NETHERITE,
                    () -> Ingredient.of(ModItems.SINGULARITY_INGOT.get()), layers("singularity"), 4.0F, 0.15F));

    public static final Holder<ArmorMaterial> PRISMATIC = Registrar.registerHolder(BuiltInRegistries.ARMOR_MATERIAL, "prismatic",
            () -> new ArmorMaterial(defense(5, 9, 12, 5, 16), 25, SoundEvents.ARMOR_EQUIP_NETHERITE,
                    () -> Ingredient.of(ModItems.OMNIUM_INGOT.get()), layers("prismatic"), 6.0F, 0.2F));

    private ModArmorMaterials() {}

    private static EnumMap<ArmorItem.Type, Integer> defense(int boots, int leggings, int chestplate, int helmet, int body) {
        return Util.make(new EnumMap<>(ArmorItem.Type.class), m -> {
            m.put(ArmorItem.Type.BOOTS, boots);
            m.put(ArmorItem.Type.LEGGINGS, leggings);
            m.put(ArmorItem.Type.CHESTPLATE, chestplate);
            m.put(ArmorItem.Type.HELMET, helmet);
            m.put(ArmorItem.Type.BODY, body);
        });
    }

    private static List<ArmorMaterial.Layer> layers(String name) {
        return List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name)));
    }

    public static void init() {}
}
