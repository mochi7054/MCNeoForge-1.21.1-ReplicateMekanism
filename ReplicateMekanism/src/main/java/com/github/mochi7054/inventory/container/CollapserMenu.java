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
        super(ReplicateMekanism.COLLAPSER_CONTAINER_TYPE, containerId, inv, tile);
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
        int yOffset = 80;
        int xOffset = 8;
        
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
        int hotbarY = 138;
        for (int col = 0; col < 9; col++) {
            int x = xOffset + col * 18;
            this.addSlot(this.createHotBarSlot(playerInventory, col, x, hotbarY));
        }
    }

    private int getInputSlotId() {
        for (int i = 0; i < this.slots.size(); i++) {
            Slot slot = this.slots.get(i);
            if (slot instanceof mekanism.common.inventory.container.slot.InventoryContainerSlot containerSlot) {
                if (containerSlot.getInventorySlot() == getTileEntity().getInputSlot()) {
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        int inputSlotId = getInputSlotId();
        if (inputSlotId != -1 && slotId == inputSlotId) {
            Slot slot = this.slots.get(slotId);
            ItemStack carried = this.getCarried();

            if (clickType == ClickType.QUICK_MOVE) {
                slot.set(ItemStack.EMPTY);
                this.broadcastChanges();
                return;
            }

            if (clickType == ClickType.PICKUP) {
                ItemStack currentStack = slot.getItem();
                if (carried.isEmpty()) {
                    slot.set(ItemStack.EMPTY);
                } else {
                    MatterCompound compound = ReplicationCalculation.getMatterCompound(carried);
                    if (compound != null && !compound.getValues().isEmpty()) {
                        if (!currentStack.isEmpty() && ItemStack.isSameItemSameComponents(carried, currentStack)) {
                            slot.set(ItemStack.EMPTY);
                        } else {
                            ItemStack copy = carried.copy();
                            copy.setCount(1);
                            slot.set(copy);
                        }
                    }
                }
                this.broadcastChanges();
                return;
            }

            if (clickType == ClickType.THROW) {
                slot.set(ItemStack.EMPTY);
                this.broadcastChanges();
                return;
            }

            return;
        }

        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotId) {
        int inputSlotId = getInputSlotId();
        if (slotId == inputSlotId) {
            Slot slot = this.slots.get(slotId);
            slot.set(ItemStack.EMPTY);
            this.broadcastChanges();
            return ItemStack.EMPTY;
        }

        if (slotId >= 0 && slotId < this.slots.size()) {
            Slot slot = this.slots.get(slotId);
            if (slot instanceof mekanism.common.inventory.container.slot.MainInventorySlot || 
                slot instanceof mekanism.common.inventory.container.slot.HotBarSlot) {
                
                if (slot.hasItem()) {
                    ItemStack stack = slot.getItem();
                    MatterCompound compound = ReplicationCalculation.getMatterCompound(stack);
                    if (compound != null && !compound.getValues().isEmpty()) {
                        if (inputSlotId != -1) {
                            Slot inputSlot = this.slots.get(inputSlotId);
                            ItemStack currentInput = inputSlot.getItem();
                            if (!currentInput.isEmpty() && ItemStack.isSameItemSameComponents(stack, currentInput)) {
                                inputSlot.set(ItemStack.EMPTY);
                            } else {
                                ItemStack copy = stack.copy();
                                copy.setCount(1);
                                inputSlot.set(copy);
                            }
                            this.broadcastChanges();
                            return ItemStack.EMPTY;
                        }
                    }
                }
            }
        }

        return super.quickMoveStack(player, slotId);
    }
}
