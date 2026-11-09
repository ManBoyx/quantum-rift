package com.quantumrift.content;

import com.quantumrift.registry.Registrar;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import com.quantumrift.entity.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class ModItems {

    /** Every item, in creative-tab order. */
    public static final List<Supplier<? extends Item>> ALL = new ArrayList<>();

    public static final Supplier<Item> RAW_QUANTITE = item("raw_quantite", () -> new Item(new Item.Properties()));
    public static final Supplier<Item> QUANTITE_INGOT = item("quantite_ingot", () -> new Item(new Item.Properties()));
    public static final Supplier<Item> SINGULARITY_SHARD = item("singularity_shard", () -> new Item(new Item.Properties().fireResistant()));
    public static final Supplier<Item> SINGULARITY_INGOT = item("singularity_ingot", () -> new Item(new Item.Properties().fireResistant()));
    /** Key item used to wake up a dormant rift gate. */
    public static final Supplier<Item> INTERDIMENSIONAL_ROD = item("interdimensional_rod", () -> new Item(new Item.Properties().stacksTo(16).rarity(Rarity.RARE)));

    public static final Supplier<Item> QUANTITE_SWORD = item("quantite_sword", () -> new SwordItem(QRTier.QUANTITE,
            new Item.Properties().attributes(SwordItem.createAttributes(QRTier.QUANTITE, 3, -2.4F))));
    public static final Supplier<Item> QUANTITE_PICKAXE = item("quantite_pickaxe", () -> new PickaxeItem(QRTier.QUANTITE,
            new Item.Properties().attributes(PickaxeItem.createAttributes(QRTier.QUANTITE, 1.0F, -2.8F))));
    public static final Supplier<Item> QUANTITE_AXE = item("quantite_axe", () -> new AxeItem(QRTier.QUANTITE,
            new Item.Properties().attributes(AxeItem.createAttributes(QRTier.QUANTITE, 5.0F, -3.0F))));
    public static final Supplier<Item> QUANTITE_SHOVEL = item("quantite_shovel", () -> new ShovelItem(QRTier.QUANTITE,
            new Item.Properties().attributes(ShovelItem.createAttributes(QRTier.QUANTITE, 1.5F, -3.0F))));
    public static final Supplier<Item> QUANTITE_HOE = item("quantite_hoe", () -> new HoeItem(QRTier.QUANTITE,
            new Item.Properties().attributes(HoeItem.createAttributes(QRTier.QUANTITE, -2.5F, -1.5F))));
    public static final Supplier<Item> QUANTITE_HELMET = armor("quantite_helmet", ModArmorMaterials.QUANTITE, ArmorItem.Type.HELMET, 25, false);
    public static final Supplier<Item> QUANTITE_CHESTPLATE = armor("quantite_chestplate", ModArmorMaterials.QUANTITE, ArmorItem.Type.CHESTPLATE, 25, false);
    public static final Supplier<Item> QUANTITE_LEGGINGS = armor("quantite_leggings", ModArmorMaterials.QUANTITE, ArmorItem.Type.LEGGINGS, 25, false);
    public static final Supplier<Item> QUANTITE_BOOTS = armor("quantite_boots", ModArmorMaterials.QUANTITE, ArmorItem.Type.BOOTS, 25, false);

    public static final Supplier<Item> SINGULARITY_SWORD = item("singularity_sword", () -> new SwordItem(QRTier.SINGULARITY,
            new Item.Properties().fireResistant().attributes(SwordItem.createAttributes(QRTier.SINGULARITY, 3, -2.4F))));
    public static final Supplier<Item> SINGULARITY_PICKAXE = item("singularity_pickaxe", () -> new PickaxeItem(QRTier.SINGULARITY,
            new Item.Properties().fireResistant().attributes(PickaxeItem.createAttributes(QRTier.SINGULARITY, 1.0F, -2.8F))));
    public static final Supplier<Item> SINGULARITY_AXE = item("singularity_axe", () -> new AxeItem(QRTier.SINGULARITY,
            new Item.Properties().fireResistant().attributes(AxeItem.createAttributes(QRTier.SINGULARITY, 5.0F, -3.0F))));
    public static final Supplier<Item> SINGULARITY_SHOVEL = item("singularity_shovel", () -> new ShovelItem(QRTier.SINGULARITY,
            new Item.Properties().fireResistant().attributes(ShovelItem.createAttributes(QRTier.SINGULARITY, 1.5F, -3.0F))));
    public static final Supplier<Item> SINGULARITY_HOE = item("singularity_hoe", () -> new HoeItem(QRTier.SINGULARITY,
            new Item.Properties().fireResistant().attributes(HoeItem.createAttributes(QRTier.SINGULARITY, -5.0F, 0.0F))));
    public static final Supplier<Item> SINGULARITY_HELMET = armor("singularity_helmet", ModArmorMaterials.SINGULARITY, ArmorItem.Type.HELMET, 45, true);
    public static final Supplier<Item> SINGULARITY_CHESTPLATE = armor("singularity_chestplate", ModArmorMaterials.SINGULARITY, ArmorItem.Type.CHESTPLATE, 45, true);
    public static final Supplier<Item> SINGULARITY_LEGGINGS = armor("singularity_leggings", ModArmorMaterials.SINGULARITY, ArmorItem.Type.LEGGINGS, 45, true);
    public static final Supplier<Item> SINGULARITY_BOOTS = armor("singularity_boots", ModArmorMaterials.SINGULARITY, ArmorItem.Type.BOOTS, 45, true);


    // ---- Omnium (found everywhere), modules, boss drops, Quantinium
    public static final Supplier<Item> RAW_OMNIUM = item("raw_omnium", () -> new Item(new Item.Properties()));
    public static final Supplier<Item> OMNIUM_INGOT = item("omnium_ingot", () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final Supplier<Item> UPGRADE_MODULE_CORE = item("upgrade_module_core", () -> new Item(new Item.Properties()));

    /** One upgrade module per ore, in the slot order of the Fusion Chamber. */
    public static final List<Supplier<Item>> MODULES = new ArrayList<>();
    public static final String[] MODULE_ORES = {"coal", "copper", "iron", "gold", "redstone", "lapis", "diamond", "emerald", "netherite", "quantite", "singularity", "omnium"};
    static {
        for (String ore : MODULE_ORES) {
            MODULES.add(item(ore + "_upgrade_module", () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON))));
        }
    }

    public static final Supplier<Item> SOVEREIGN_SIGIL = item("sovereign_sigil", () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
    public static final Supplier<Item> SOVEREIGN_HEART = item("sovereign_heart", () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant()));
    public static final Supplier<Item> PRISMATIC_TEMPLATE = item("prismatic_template", () -> new Item(new Item.Properties().rarity(Rarity.EPIC).fireResistant()));

    /** Never used up as a furnace fuel (see AbstractFurnaceBlockEntityMixin); the burn time is registered per loader. */
    public static final Supplier<Item> QUANTINIUM_COAL = item("quantinium_coal", () -> new Item(new Item.Properties().rarity(Rarity.RARE).fireResistant()));
    public static final Supplier<Item> QUANTINIUM_STEAK = item("quantinium_steak", () -> new InfiniteFoodItem(new Item.Properties().rarity(Rarity.RARE)
            .food(new FoodProperties.Builder().nutrition(10).saturationModifier(1.2F).alwaysEdible().build())));

    // ---- Prismatic gear (made by smithing from Singularity gear + Prismatic Template)
    public static final Supplier<Item> PRISMATIC_SWORD = item("prismatic_sword", () -> new SwordItem(QRTier.PRISMATIC,
            new Item.Properties().fireResistant().rarity(Rarity.EPIC).attributes(SwordItem.createAttributes(QRTier.PRISMATIC, 3, -2.4F))));
    public static final Supplier<Item> PRISMATIC_PICKAXE = item("prismatic_pickaxe", () -> new PickaxeItem(QRTier.PRISMATIC,
            new Item.Properties().fireResistant().rarity(Rarity.EPIC).attributes(PickaxeItem.createAttributes(QRTier.PRISMATIC, 1.0F, -2.8F))));
    public static final Supplier<Item> PRISMATIC_AXE = item("prismatic_axe", () -> new AxeItem(QRTier.PRISMATIC,
            new Item.Properties().fireResistant().rarity(Rarity.EPIC).attributes(AxeItem.createAttributes(QRTier.PRISMATIC, 5.0F, -3.0F))));
    public static final Supplier<Item> PRISMATIC_SHOVEL = item("prismatic_shovel", () -> new ShovelItem(QRTier.PRISMATIC,
            new Item.Properties().fireResistant().rarity(Rarity.EPIC).attributes(ShovelItem.createAttributes(QRTier.PRISMATIC, 1.5F, -3.0F))));
    public static final Supplier<Item> PRISMATIC_HOE = item("prismatic_hoe", () -> new HoeItem(QRTier.PRISMATIC,
            new Item.Properties().fireResistant().rarity(Rarity.EPIC).attributes(HoeItem.createAttributes(QRTier.PRISMATIC, -7.5F, 0.0F))));
    public static final Supplier<Item> PRISMATIC_HELMET = prismatic("prismatic_helmet", ArmorItem.Type.HELMET);
    public static final Supplier<Item> PRISMATIC_CHESTPLATE = prismatic("prismatic_chestplate", ArmorItem.Type.CHESTPLATE);
    public static final Supplier<Item> PRISMATIC_LEGGINGS = prismatic("prismatic_leggings", ArmorItem.Type.LEGGINGS);
    public static final Supplier<Item> PRISMATIC_BOOTS = prismatic("prismatic_boots", ArmorItem.Type.BOOTS);

    private ModItems() {}

    private static Supplier<Item> prismatic(String name, ArmorItem.Type type) {
        return item(name, () -> new PrismaticArmorItem(ModArmorMaterials.PRISMATIC, type,
                new Item.Properties().durability(type.getDurability(70)).fireResistant().rarity(Rarity.EPIC)));
    }


    public static Supplier<Item> item(String name, Supplier<? extends Item> factory) {
        Supplier<Item> s = Registrar.register(BuiltInRegistries.ITEM, name, factory);
        ALL.add(s);
        return s;
    }

    private static Supplier<Item> armor(String name, Holder<ArmorMaterial> material, ArmorItem.Type type, int durabilityFactor, boolean fireproof) {
        return item(name, () -> {
            Item.Properties p = new Item.Properties().durability(type.getDurability(durabilityFactor));
            return new ArmorItem(material, type, fireproof ? p.fireResistant() : p);
        });
    }

    public static void init() {}
}
