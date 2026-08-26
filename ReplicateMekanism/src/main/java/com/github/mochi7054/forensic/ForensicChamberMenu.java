package com.github.mochi7054.forensic;

import com.github.mochi7054.ReplicateMekanism;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class ForensicChamberMenu extends MekanismTileContainer<ForensicChamberBlockEntity> {

    public ForensicChamberMenu(int containerId, Inventory inv, ForensicChamberBlockEntity tile) {
        super(ReplicateMekanism.FORENSIC_CHAMBER_CONTAINER_TYPE, containerId, inv, tile);
    }

    public ForensicChamberMenu(int containerId, Inventory inv, RegistryFriendlyByteBuf buf) {
        this(containerId, inv, getTileFromBuf(buf, ForensicChamberBlockEntity.class, inv));
    }

    private static <TILE extends mekanism.common.tile.base.TileEntityMekanism> TILE getTileFromBuf(RegistryFriendlyByteBuf buf, Class<TILE> type, Inventory inv) {
        if (buf == null) {
            return null;
        }
        return mekanism.common.util.WorldUtils.getTileEntity(type, inv.player.level(), buf.readBlockPos());
    }

    @Override
    protected void addInventorySlots(Inventory playerInventory) {
        int xOffset = 8;
        int yOffset = 94;

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
        int hotbarY = 152;
        for (int col = 0; col < 9; col++) {
            int x = xOffset + col * 18;
            this.addSlot(this.createHotBarSlot(playerInventory, col, x, hotbarY));
        }
    }
}