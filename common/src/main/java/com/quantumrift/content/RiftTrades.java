package com.quantumrift.content;

import com.quantumrift.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/** The cartographer sells a map to the nearest rift gate. Each loader adds it through its own trade hook. */
public final class RiftTrades {

    /** Journeyman cartographer level. */
    public static final int CARTOGRAPHER_LEVEL = 2;

    private static final TagKey<Structure> RIFT_MAP = TagKey.create(Registries.STRUCTURE,
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "on_rift_map"));

    private RiftTrades() {}

    public static VillagerTrades.ItemListing riftMap() {
        return new RiftMapListing();
    }

    private static final class RiftMapListing implements VillagerTrades.ItemListing {

        @Nullable
        @Override
        public MerchantOffer getOffer(Entity trader, RandomSource random) {
            if (!(trader.level() instanceof ServerLevel level)) return null;
            BlockPos pos = level.findNearestMapStructure(RIFT_MAP, trader.blockPosition(), 150, true);
            if (pos == null) return null;
            ItemStack map = MapItem.create(level, pos.getX(), pos.getZ(), (byte) 2, true, true);
            MapItem.renderBiomePreviewMap(level, map);
            MapItemSavedData.addTargetDecoration(map, pos, "+", MapDecorationTypes.RED_X);
            map.set(DataComponents.ITEM_NAME, Component.translatable("filled_map.quantumrift.rift_gate"));
            return new MerchantOffer(new ItemCost(Items.EMERALD, 14), Optional.of(new ItemCost(Items.COMPASS)), map, 6, 15, 0.2F);
        }
    }
}
