package com.quantumrift.content;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;

/** Minimal spawn egg that resolves its entity lazily (entity types may be registered after items on some loaders). */
public class QRSpawnEggItem extends Item {

    private final Supplier<? extends EntityType<?>> type;

    public QRSpawnEggItem(Supplier<? extends EntityType<?>> type, Properties properties) {
        super(properties);
        this.type = type;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (!(level instanceof ServerLevel server)) return InteractionResult.SUCCESS;
        BlockPos pos = context.getClickedPos();
        BlockPos spawnAt = level.getBlockState(pos).getCollisionShape(level, pos).isEmpty() ? pos : pos.relative(context.getClickedFace());
        if (type.get().spawn(server, context.getItemInHand(), context.getPlayer(), spawnAt, MobSpawnType.SPAWN_EGG, true, !pos.equals(spawnAt)) != null) {
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.CONSUME;
    }
}
