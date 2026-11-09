package com.quantumrift.mixin;

import com.quantumrift.content.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Quantinium Coal is never consumed: put the fuel stack back after each furnace tick that used it up. */
@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityMixin {

    @Unique
    private static final ThreadLocal<ItemStack> QR_FUEL = new ThreadLocal<>();

    @Inject(method = "serverTick", at = @At("HEAD"))
    private static void quantumrift$rememberFuel(Level level, BlockPos pos, BlockState state, AbstractFurnaceBlockEntity furnace, CallbackInfo ci) {
        ItemStack fuel = furnace.getItem(1);
        QR_FUEL.set(fuel.is(ModItems.QUANTINIUM_COAL.get()) ? fuel.copy() : null);
    }

    @Inject(method = "serverTick", at = @At("RETURN"))
    private static void quantumrift$restoreFuel(Level level, BlockPos pos, BlockState state, AbstractFurnaceBlockEntity furnace, CallbackInfo ci) {
        ItemStack before = QR_FUEL.get();
        QR_FUEL.remove();
        if (before == null) return;
        ItemStack now = furnace.getItem(1);
        if (now.isEmpty() || now.getCount() < before.getCount()) furnace.setItem(1, before);
    }
}
