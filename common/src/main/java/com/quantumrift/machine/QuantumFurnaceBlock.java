package com.quantumrift.machine;

import com.mojang.serialization.MapCodec;
import com.quantumrift.content.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class QuantumFurnaceBlock extends AbstractFurnaceBlock {

    public static final MapCodec<QuantumFurnaceBlock> CODEC = simpleCodec(QuantumFurnaceBlock::new);
    /** Ticks run per game tick: fuel burns and items cook this many times faster. */
    public static final int SPEED = 3;

    public QuantumFurnaceBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends AbstractFurnaceBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new QuantumFurnaceBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return createTickerHelper(type, ModBlockEntities.QUANTUM_FURNACE.get(), (l, p, s, be) -> {
            for (int i = 0; i < SPEED; i++) AbstractFurnaceBlockEntity.serverTick(l, p, s, be);
        });
    }

    @Override
    protected void openContainer(Level level, BlockPos pos, Player player) {
        if (level.getBlockEntity(pos) instanceof QuantumFurnaceBlockEntity furnace) {
            player.openMenu(furnace);
            player.awardStat(Stats.INTERACT_WITH_FURNACE);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) return;
        double x = pos.getX() + 0.5, y = pos.getY(), z = pos.getZ() + 0.5;
        if (random.nextDouble() < 0.15) level.playLocalSound(x, y, z, net.minecraft.sounds.SoundEvents.FURNACE_FIRE_CRACKLE, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F, false);
        Direction facing = state.getValue(FACING);
        double front = 0.52, side = random.nextDouble() * 0.6 - 0.3;
        double dx = facing.getAxis() == Direction.Axis.X ? facing.getStepX() * front : side;
        double dz = facing.getAxis() == Direction.Axis.Z ? facing.getStepZ() * front : side;
        level.addParticle(ParticleTypes.REVERSE_PORTAL, x + dx, y + random.nextDouble() * 6.0 / 16.0, z + dz, 0.0, 0.02, 0.0);
        level.addParticle(ParticleTypes.FLAME, x + dx, y + random.nextDouble() * 6.0 / 16.0, z + dz, 0.0, 0.0, 0.0);
    }
}
