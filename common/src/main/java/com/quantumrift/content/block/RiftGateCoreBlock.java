package com.quantumrift.content.block;

import com.quantumrift.content.ModBlocks;
import com.quantumrift.content.ModItems;
import com.quantumrift.content.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.InteractionHand;

/** Socket of a dormant rift gate. Three interdimensional rods wake the gate up. */
public class RiftGateCoreBlock extends Block {

    public static final int RODS_NEEDED = 3;
    public static final IntegerProperty RODS = IntegerProperty.create("rods", 0, RODS_NEEDED);
    private static final int RADIUS_XZ = 10;
    private static final int RADIUS_Y = 8;

    public RiftGateCoreBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(RODS, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(RODS);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                              InteractionHand hand, BlockHitResult hit) {
        if (!stack.is(ModItems.INTERDIMENSIONAL_ROD.get())) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        int rods = state.getValue(RODS);
        if (rods >= RODS_NEEDED) return ItemInteractionResult.CONSUME;
        if (!level.isClientSide) {
            if (!player.getAbilities().instabuild) stack.shrink(1);
            rods++;
            level.setBlock(pos, state.setValue(RODS, rods), Block.UPDATE_ALL);
            level.playSound(null, pos, ModSounds.BLOCK_GATE_ROD.get(), SoundSource.BLOCKS, 1.0F, 0.8F + 0.2F * rods);
            if (rods >= RODS_NEEDED) activate(level, pos);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide) {
            int rods = state.getValue(RODS);
            player.displayClientMessage(rods >= RODS_NEEDED ? Component.translatable("message.quantumrift.gate_awake")
                    : Component.translatable("message.quantumrift.gate_rods", rods, RODS_NEEDED), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /** Turns every dormant rift block around the core into an active portal. */
    private static void activate(Level level, BlockPos core) {
        BlockPos.betweenClosed(core.offset(-RADIUS_XZ, -RADIUS_Y, -RADIUS_XZ), core.offset(RADIUS_XZ, RADIUS_Y, RADIUS_XZ)).forEach(p -> {
            if (level.getBlockState(p).is(ModBlocks.DORMANT_RIFT.get())) {
                level.setBlock(p, ModBlocks.RIFT_PORTAL.get().defaultBlockState(), Block.UPDATE_ALL);
            }
        });
        level.playSound(null, core, ModSounds.BLOCK_GATE_AWAKEN.get(), SoundSource.BLOCKS, 2.0F, 1.0F);
        level.playSound(null, core, SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 0.6F, 1.0F);
    }
}
