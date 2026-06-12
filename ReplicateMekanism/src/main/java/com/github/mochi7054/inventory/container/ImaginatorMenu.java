package com.github.mochi7054.inventory.container;

import com.github.mochi7054.ReplicateMekanism;
import com.github.mochi7054.block.entity.ImaginatorBlockEntity;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import com.buuz135.replication.calculation.MatterCompound;
import com.buuz135.replication.calculation.ReplicationCalculation;

public class ImaginatorMenu extends MekanismTileContainer<ImaginatorBlockEntity> {

    public ImaginatorMenu(int containerId, Inventory inv, ImaginatorBlockEntity tile) {
        super(getContainerTypeForTile(tile), containerId, inv, tile);
    }

    private static mekanism.common.registration.impl.ContainerTypeRegistryObject<ImaginatorMenu> getContainerTypeForTile(ImaginatorBlockEntity tile) {
        if (tile == null) {
            return ReplicateMekanism.IMAGINATOR_CONTAINER_TYPE;
        }
        return switch (tile.getTier()) {
            case STANDARD -> ReplicateMekanism.IMAGINATOR_CONTAINER_TYPE;
            case BASIC -> ReplicateMekanism.IMAGINATOR_BASIC_CONTAINER_TYPE;
            case ADVANCED -> ReplicateMekanism.IMAGINATOR_ADVANCED_CONTAINER_TYPE;
            case ELITE -> ReplicateMekanism.IMAGINATOR_ELITE_CONTAINER_TYPE;
            case ULTIMATE -> ReplicateMekanism.IMAGINATOR_ULTIMATE_CONTAINER_TYPE;
        };
    }

    public ImaginatorMenu(int containerId, Inventory inv, RegistryFriendlyByteBuf buf) {
        this(containerId, inv, getTileFromBuf(buf, ImaginatorBlockEntity.class, inv));
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

    private boolean isInputSlot(Slot slot) {
        if (slot instanceof mekanism.common.inventory.container.slot.InventoryContainerSlot containerSlot) {
            ImaginatorBlockEntity tile = getTileEntity();
            if (tile != null) {
                return tile.inputSlots.contains(containerSlot.getInventorySlot());
            }
        }
        return false;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId >= 0 && slotId < this.slots.size()) {
            Slot slot = this.slots.get(slotId);
            if (isInputSlot(slot)) {
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
        }

        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotId) {
        if (slotId >= 0 && slotId < this.slots.size()) {
            Slot slot = this.slots.get(slotId);
            if (isInputSlot(slot)) {
                slot.set(ItemStack.EMPTY);
                this.broadcastChanges();
                return ItemStack.EMPTY;
            }

            if (slot instanceof mekanism.common.inventory.container.slot.MainInventorySlot || 
                slot instanceof mekanism.common.inventory.container.slot.HotBarSlot) {
                
                if (slot.hasItem()) {
                    ItemStack stack = slot.getItem();
                    MatterCompound compound = ReplicationCalculation.getMatterCompound(stack);
                    if (compound != null && !compound.getValues().isEmpty()) {
                        ImaginatorBlockEntity tile = getTileEntity();
                        if (tile != null) {
                            Slot targetSlot = null;
                            for (Slot s : this.slots) {
                                if (isInputSlot(s)) {
                                    ItemStack inputItem = s.getItem();
                                    if (!inputItem.isEmpty() && ItemStack.isSameItemSameComponents(stack, inputItem)) {
                                        targetSlot = s;
                                        break;
                                    }
                                }
                            }
                            if (targetSlot == null) {
                                for (Slot s : this.slots) {
                                    if (isInputSlot(s)) {
                                        if (s.getItem().isEmpty()) {
                                            targetSlot = s;
                                            break;
                                        }
                                    }
                                }
                            }
                            if (targetSlot != null) {
                                ItemStack currentInput = targetSlot.getItem();
                                if (!currentInput.isEmpty() && ItemStack.isSameItemSameComponents(stack, currentInput)) {
                                    targetSlot.set(ItemStack.EMPTY);
                                } else {
                                    ItemStack copy = stack.copy();
                                    copy.setCount(1);
                                    targetSlot.set(copy);
                                }
                                this.broadcastChanges();
                                return ItemStack.EMPTY;
                            }
                        }
                    }
                }
            }
        }

        return super.quickMoveStack(player, slotId);
    }
}
