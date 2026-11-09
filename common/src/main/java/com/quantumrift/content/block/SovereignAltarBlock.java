package com.quantumrift.content.block;

import com.quantumrift.content.ModItems;
import com.quantumrift.content.ModSounds;
import com.quantumrift.entity.ModEntities;
import com.quantumrift.entity.VoidSovereign;
import com.quantumrift.world.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

/** Use a Sovereign's Sigil here, in the Quantum Realm, to call the boss. Only one at a time. */
public class SovereignAltarBlock extends Block {

    public SovereignAltarBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!stack.is(ModItems.SOVEREIGN_SIGIL.get())) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (level instanceof ServerLevel server) {
            if (level.dimension() != ModDimensions.QUANTUM) {
                player.displayClientMessage(Component.translatable("message.quantumrift.altar_silent"), true);
            } else if (!server.getEntitiesOfClass(VoidSovereign.class, new AABB(pos).inflate(96.0)).isEmpty()) {
                player.displayClientMessage(Component.translatable("message.quantumrift.altar_busy"), true);
            } else {
                VoidSovereign boss = ModEntities.VOID_SOVEREIGN.get().spawn(server, pos.above(1), MobSpawnType.EVENT);
                if (boss != null) {
                    boss.setPersistenceRequired();
                    LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(server);
                    if (bolt != null) { bolt.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5); bolt.setVisualOnly(true); server.addFreshEntity(bolt); }
                    server.playSound(null, pos, ModSounds.BLOCK_ALTAR_ACTIVATE.get(), SoundSource.BLOCKS, 3.0F, 1.0F);
                    if (!player.getAbilities().instabuild) stack.shrink(1);
                }
            }
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide) player.displayClientMessage(Component.translatable("message.quantumrift.altar_hint"), true);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
