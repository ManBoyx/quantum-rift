package com.quantumrift.machine;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/** Shared behaviour of the container machines: opens its menu on use, drops its contents when broken, ticks server side. */
public class SimpleMachineBlock extends BaseEntityBlock {

    public interface Factory { BlockEntity create(BlockPos pos, BlockState state); }

    public interface Ticker { void tick(Level level, BlockPos pos, BlockState state, BlockEntity be); }

    private final Factory factory;
    private final Supplier<? extends BlockEntityType<?>> type;
    @Nullable private final Ticker ticker;

    public SimpleMachineBlock(Properties properties, Factory factory, Supplier<? extends BlockEntityType<?>> type, @Nullable Ticker ticker) {
        super(properties);
        this.factory = factory;
        this.type = type;
        this.ticker = ticker;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        // Not data-driven; blocks are created in code only.
        return simpleCodec(p -> new SimpleMachineBlock(p, factory, type, ticker));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return factory.create(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> given) {
        if (level.isClientSide || ticker == null || given != type.get()) return null;
        return (l, p, s, be) -> ticker.tick(l, p, s, be);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof net.minecraft.world.MenuProvider provider) {
            player.openMenu(provider);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof Container container) {
                Containers.dropContents(level, pos, container);
                level.updateNeighbourForOutputSignal(pos, this);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
