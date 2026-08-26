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
    protected int getInventoryYOffset() {
        return 84;
    }
}
