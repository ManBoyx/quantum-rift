package com.quantumrift.content;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** A food that is never used up: eating it restores hunger and gives the full stack back. */
public class InfiniteFoodItem extends Item {

    public InfiniteFoodItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack keep = stack.copy();
        super.finishUsingItem(stack, level, entity);
        return entity instanceof Player ? keep : stack;
    }
}
