package com.github.mochi7054.forensic;

import com.github.mochi7054.client.gui.ReplicationGuiVerticalPowerBar;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.container.slot.InventoryContainerSlot;
import mekanism.common.tile.component.config.DataType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class ForensicChamberScreen extends GuiConfigurableTile<ForensicChamberBlockEntity, ForensicChamberMenu> {

    private static final ResourceLocation REPLICATION_BACKGROUND = ResourceLocation.fromNamespaceAndPath("replication", "textures/gui/background.png");
    private static final ResourceLocation CUSTOM_SLOT_TEXTURE = ResourceLocation.fromNamespaceAndPath("replicatemekanism", "textures/gui/slot.png");
    private static final ResourceLocation MEMORY_CHIP_GHOST_TEXTURE = ResourceLocation.fromNamespaceAndPath("replicatemekanism", "textures/gui/memory_chip_ghost.png");

    public ForensicChamberScreen(ForensicChamberMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 174;
        this.imageHeight = 174;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 84;
        this.titleLabelY = 10;
        this.dynamicSlots = true;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        ForensicChamberBlockEntity tile = menu.getTileEntity();

        // Energy Bar on the right
        this.addElement(new ReplicationGuiVerticalPowerBar(this, tile.energyContainer, 162, 22, 52));
        this.addElement(new GuiEnergyTab(this, tile.energyContainer, () -> true));
    }

    @Override
    protected void addSlots() {
        for (int i = 0; i < menu.slots.size(); i++) {
            Slot slot = menu.slots.get(i);
            
            if (slot instanceof InventoryContainerSlot containerSlot) {
                ContainerSlotType slotType = containerSlot.getSlotType();
                
                DataType dataType = findDataType(containerSlot);
                SlotType type;
                if (dataType != null) {
                    type = SlotType.get(dataType);
                } else if (slotType == ContainerSlotType.INPUT ||
                           slotType == ContainerSlotType.OUTPUT ||
                           slotType == ContainerSlotType.EXTRA) {
                    type = SlotType.NORMAL;
                } else if (slotType == ContainerSlotType.POWER) {
                    type = SlotType.POWER;
                } else {
                    type = SlotType.NORMAL;
                }

                GuiSlot guiSlot = new ReplicationGuiSlot(type, this, slot.x - 1, slot.y - 1, containerSlot);

                boolean isPlayerSlot = slot instanceof mekanism.common.inventory.container.slot.MainInventorySlot ||
                                       slot instanceof mekanism.common.inventory.container.slot.HotBarSlot;

                if (!isPlayerSlot && (slotType == ContainerSlotType.IGNORED ||
                    containerSlot instanceof mekanism.common.inventory.container.slot.VirtualInventoryContainerSlot)) {
                    guiSlot.visible = false;
                }

                containerSlot.addWarnings(guiSlot);
                mekanism.common.inventory.container.slot.SlotOverlay overlay = containerSlot.getSlotOverlay();
                if (overlay != null) {
                    guiSlot.with(overlay);
                }
                
                this.addRenderableWidget(guiSlot);
            } else {
                GuiSlot guiSlot = new ReplicationGuiSlot(SlotType.NORMAL, this, slot.x - 1, slot.y - 1, slot);
                this.addRenderableWidget(guiSlot);
            }
        }
    }

    private static class ReplicationGuiSlot extends GuiSlot {
        private final Slot slot;

        public ReplicationGuiSlot(SlotType type, mekanism.client.gui.IGuiWrapper gui, int x, int y, Slot slot) {
            super(type, gui, x, y);
            this.slot = slot;
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
            if (!this.isRenderAboveSlots()) {
                this.customDraw(guiGraphics);
            }
        }

        @Override
        public void drawBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
            if (this.isRenderAboveSlots()) {
                this.customDraw(guiGraphics);
            }
        }

        private boolean isRenderAboveSlots() {
            try {
                java.lang.reflect.Field field = GuiSlot.class.getDeclaredField("renderAboveSlots");
                field.setAccessible(true);
                return field.getBoolean(this);
            } catch (Exception e) {
                return false;
            }
        }

        private void customDraw(GuiGraphics guiGraphics) {
            guiGraphics.blit(CUSTOM_SLOT_TEXTURE, this.relativeX, this.relativeY, 0, 0, 18, 18, 18, 18);
            
            if (slot instanceof InventoryContainerSlot containerSlot) {
                mekanism.common.inventory.container.slot.SlotOverlay overlay = containerSlot.getSlotOverlay();
                if (overlay != null) {
                    guiGraphics.blit(overlay.getTexture(), this.relativeX, this.relativeY, 0.0F, 0.0F, overlay.getWidth(), overlay.getHeight(), overlay.getWidth(), overlay.getHeight());
                }
            }

            // Ghost icon for Memory Chip if chip input slot or chip output slot is empty
            boolean isChipInput = (this.relativeX == 73 && this.relativeY == 41);
            boolean isChipOutput = (this.relativeX == 115 && this.relativeY == 41);
            if ((isChipInput || isChipOutput) && !slot.hasItem()) {
                com.mojang.blaze3d.systems.RenderSystem.enableBlend();
                com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
                guiGraphics.setColor(1.0F, 1.0F, 1.0F, 0.4F);
                guiGraphics.blit(MEMORY_CHIP_GHOST_TEXTURE, this.relativeX + 1, this.relativeY + 1, 0, 0, 16, 16, 16, 16);
                guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
                com.mojang.blaze3d.systems.RenderSystem.disableBlend();
            }
            
            this.drawContents(guiGraphics);
        }
    }

    private void drawMachineArea(GuiGraphics guiGraphics, int x, int y, int width) {
        guiGraphics.blit(REPLICATION_BACKGROUND, x, y, 0, 0, width, 88);
    }

    private void drawInventoryArea(GuiGraphics guiGraphics, int left, int top, int width, int xOffset) {
        // 1. Draw top 4px of inventory area (with top border)
        guiGraphics.blit(REPLICATION_BACKGROUND, left, top, 0, 96, 6, 4);
        for (int x = 6; x < width - 6; x++) {
            guiGraphics.blit(REPLICATION_BACKGROUND, left + x, top, 150, 96, 1, 4);
        }
        guiGraphics.blit(REPLICATION_BACKGROUND, left + width - 6, top, 168, 96, 6, 4);

        // 2. Draw middle 80px
        int midY = top + 4;
        guiGraphics.blit(REPLICATION_BACKGROUND, left, midY, 0, 100, 6, 80);
        guiGraphics.fill(left + 6, midY, left + width - 6, midY + 76, 0xFF252A37);
        guiGraphics.blit(REPLICATION_BACKGROUND, left + width - 6, midY, 168, 100, 6, 80);

        // 3. Draw bottom 6px
        int botY = top + 80;
        guiGraphics.blit(REPLICATION_BACKGROUND, left, botY, 0, 176, 6, 6);
        for (int x = 6; x < width - 6; x++) {
            guiGraphics.blit(REPLICATION_BACKGROUND, left + x, botY, 150, 176, 1, 6);
        }
        guiGraphics.blit(REPLICATION_BACKGROUND, left + width - 6, botY, 168, 176, 6, 6);

        if (xOffset > 6) {
            guiGraphics.fill(left + 6, botY, left + xOffset, botY + 2, 0xFF252A37);
        }
        if (left + xOffset + 162 < left + width - 6) {
            guiGraphics.fill(left + xOffset + 161, botY, left + width - 5, botY + 2, 0xFF252A37);
            guiGraphics.fill(left + width - 6, top, left + width - 5, botY, 0xFF252A37);
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        mekanism.client.render.MekanismRenderer.resetColor(guiGraphics);
        if (this.getXSize() < 8 || this.getYSize() < 8) {
            return;
        }
        drawMachineArea(guiGraphics, this.leftPos, this.topPos, this.imageWidth);
        drawInventoryArea(guiGraphics, this.leftPos, this.topPos + 88, this.imageWidth, 8);
    }

    @Override
    protected void drawForegroundText(GuiGraphics graphics, int mouseX, int mouseY) {
        int titleWidth = this.font.width(this.title);
        int titleX = (this.imageWidth - titleWidth) / 2;
        graphics.drawString(this.font, this.title, titleX, this.titleLabelY, 0xFF38FF70, false);
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0xFF38FF70, false);
        super.drawForegroundText(graphics, mouseX, mouseY);
    }
}