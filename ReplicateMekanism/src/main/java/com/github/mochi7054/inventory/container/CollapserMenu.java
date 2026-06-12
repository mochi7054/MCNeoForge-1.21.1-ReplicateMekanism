package com.github.mochi7054.inventory.container;

import com.buuz135.replication.calculation.MatterCompound;
import com.buuz135.replication.calculation.ReplicationCalculation;
import com.github.mochi7054.ReplicateMekanism;
import com.github.mochi7054.block.entity.CollapserBlockEntity;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class CollapserMenu extends MekanismTileContainer<CollapserBlockEntity> {

    public CollapserMenu(int containerId, Inventory inv, CollapserBlockEntity tile) {
        super(getContainerTypeForTile(tile), containerId, inv, tile);
    }

    private static mekanism.common.registration.impl.ContainerTypeRegistryObject<CollapserMenu> getContainerTypeForTile(CollapserBlockEntity tile) {
        if (tile == null) {
            return ReplicateMekanism.COLLAPSER_CONTAINER_TYPE;
        }
        return switch (tile.getTier()) {
            case STANDARD -> ReplicateMekanism.COLLAPSER_CONTAINER_TYPE;
            case BASIC -> ReplicateMekanism.COLLAPSER_BASIC_CONTAINER_TYPE;
            case ADVANCED -> ReplicateMekanism.COLLAPSER_ADVANCED_CONTAINER_TYPE;
            case ELITE -> ReplicateMekanism.COLLAPSER_ELITE_CONTAINER_TYPE;
            case ULTIMATE -> ReplicateMekanism.COLLAPSER_ULTIMATE_CONTAINER_TYPE;
        };
    }

    public CollapserMenu(int containerId, Inventory inv, RegistryFriendlyByteBuf buf) {
        this(containerId, inv, getTileFromBuf(buf, CollapserBlockEntity.class, inv));
    }

    private static <TILE extends mekanism.common.tile.base.TileEntityMekanism> TILE getTileFromBuf(RegistryFriendlyByteBuf buf, Class<TILE> type, Inventory inv) {
        if (buf == null) {
            return null;
        }
        return mekanism.common.util.WorldUtils.getTileEntity(type, inv.player.level(), buf.readBlockPos());
    }

    @Override
    protected void addInventorySlots(Inventory playerInventory) {
        com.github.mochi7054.block.ReplicaTier tier = getTileEntity() != null ? getTileEntity().getTier() : com.github.mochi7054.block.ReplicaTier.STANDARD;
        int yOffset = tier == com.github.mochi7054.block.ReplicaTier.STANDARD ? 94 : 104;
        int xOffset = switch (tier) {
            case STANDARD, BASIC, ADVANCED -> 8;
            case ELITE -> 10;
            case ULTIMATE -> 29;
        };
        
        // Main Inventory (3 rows of 9 slots)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int slotIndex = 9 + col + row * 9;
                int x = xOffset + col * 18;
                int y = yOffset + row * 18;
                this.addSlot(new mekanism.common.inventory.container.slot.MainInventorySlot(playerInventory, slotIndex, x, y));
            }
        }
        
        // Hotbar (9 slots)
        int hotbarY = tier == com.github.mochi7054.block.ReplicaTier.STANDARD ? 152 : 162;
        for (int col = 0; col < 9; col++) {
            int x = xOffset + col * 18;
            this.addSlot(this.createHotBarSlot(playerInventory, col, x, hotbarY));
        }
    }

}
