package com.github.mochi7054.client.gui;

import com.github.mochi7054.block.entity.ImaginatorBlockEntity;
import com.github.mochi7054.inventory.container.ImaginatorMenu;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.GuiUpArrow;
import mekanism.client.gui.element.bar.GuiFluidBar;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.common.inventory.container.slot.InventoryContainerSlot;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.tile.component.config.DataType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class ImaginatorScreen extends GuiConfigurableTile<ImaginatorBlockEntity, ImaginatorMenu> {

    public ImaginatorScreen(ImaginatorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 162;
        this.inventoryLabelY = 68;
        this.titleLabelY = 10;
        this.dynamicSlots = true;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        ImaginatorBlockEntity tile = menu.getTileEntity();
        com.github.mochi7054.block.ReplicaTier tier = tile.getTier();

        if (tier == com.github.mochi7054.block.ReplicaTier.STANDARD) {
            // Fluid Tank Bars on the left for each of the 8 matter types
            this.addElement(new GuiFluidBar(this, GuiFluidBar.getProvider(tile.earthTank, tile.getFluidTanks(null)), 8, 20, 5, 42, false));
            this.addElement(new GuiFluidBar(this, GuiFluidBar.getProvider(tile.netherTank, tile.getFluidTanks(null)), 16, 20, 5, 42, false));
            this.addElement(new GuiFluidBar(this, GuiFluidBar.getProvider(tile.organicTank, tile.getFluidTanks(null)), 24, 20, 5, 42, false));
            this.addElement(new GuiFluidBar(this, GuiFluidBar.getProvider(tile.enderTank, tile.getFluidTanks(null)), 32, 20, 5, 42, false));
            this.addElement(new GuiFluidBar(this, GuiFluidBar.getProvider(tile.metallicTank, tile.getFluidTanks(null)), 40, 20, 5, 42, false));
            this.addElement(new GuiFluidBar(this, GuiFluidBar.getProvider(tile.preciousTank, tile.getFluidTanks(null)), 48, 20, 5, 42, false));
            this.addElement(new GuiFluidBar(this, GuiFluidBar.getProvider(tile.livingTank, tile.getFluidTanks(null)), 56, 20, 5, 42, false));
            this.addElement(new GuiFluidBar(this, GuiFluidBar.getProvider(tile.quantumTank, tile.getFluidTanks(null)), 64, 20, 5, 42, false));

            // Energy Bar on the right
            this.addElement(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), 162, 20, 42));

            // Progress Bar
            this.addElement(new ReplicationGuiProgress(() -> tile.getScaledProgress(), this, 95, 36));
        } else {
            // Shrunk fluid slots (8 slots) at Y=10
            this.addElement(new GuiSlot(SlotType.NORMAL, this, 8, 10));
            this.addElement(new GuiFluidBar(this, GuiFluidBar.getProvider(tile.earthTank, tile.getFluidTanks(null)), 9, 11, 16, 16, false));

            this.addElement(new GuiSlot(SlotType.NORMAL, this, 26, 10));
            this.addElement(new GuiFluidBar(this, GuiFluidBar.getProvider(tile.netherTank, tile.getFluidTanks(null)), 27, 11, 16, 16, false));

            this.addElement(new GuiSlot(SlotType.NORMAL, this, 44, 10));
            this.addElement(new GuiFluidBar(this, GuiFluidBar.getProvider(tile.organicTank, tile.getFluidTanks(null)), 45, 11, 16, 16, false));

            this.addElement(new GuiSlot(SlotType.NORMAL, this, 62, 10));
            this.addElement(new GuiFluidBar(this, GuiFluidBar.getProvider(tile.enderTank, tile.getFluidTanks(null)), 63, 11, 16, 16, false));

            this.addElement(new GuiSlot(SlotType.NORMAL, this, 80, 10));
            this.addElement(new GuiFluidBar(this, GuiFluidBar.getProvider(tile.metallicTank, tile.getFluidTanks(null)), 81, 11, 16, 16, false));

            this.addElement(new GuiSlot(SlotType.NORMAL, this, 98, 10));
            this.addElement(new GuiFluidBar(this, GuiFluidBar.getProvider(tile.preciousTank, tile.getFluidTanks(null)), 99, 11, 16, 16, false));

            this.addElement(new GuiSlot(SlotType.NORMAL, this, 116, 10));
            this.addElement(new GuiFluidBar(this, GuiFluidBar.getProvider(tile.livingTank, tile.getFluidTanks(null)), 117, 11, 16, 16, false));

            this.addElement(new GuiSlot(SlotType.NORMAL, this, 134, 10));
            this.addElement(new GuiFluidBar(this, GuiFluidBar.getProvider(tile.quantumTank, tile.getFluidTanks(null)), 135, 11, 16, 16, false));

            // Energy Bar on the right (shifted to X=170, aligned to Y=10 with height 74)
            this.addElement(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), 170, 10, 74));

            // Progress Bar (Downward arrow at X=84, Y=47)
            this.addElement(new GuiProgress(tile::getScaledProgress, ProgressType.DOWN, this, 84, 47));
        }

        // Energy Tab on the left
        this.addElement(new GuiEnergyTab(this, tile.getEnergyContainer(), () -> true));

        // Hide the top-left machine icon/preview holder, leave other tabs at default Mekanism positions
        for (net.minecraft.client.gui.components.events.GuiEventListener listener : this.children()) {
            if (listener instanceof mekanism.client.gui.element.GuiElement element) {
                if (element.getClass().getName().contains("GuiSideHolder") && element.getRelativeX() < 0 && element.getRelativeY() < 10) {
                    element.visible = false;
                }
            }
        }
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
                } else if (slotType == ContainerSlotType.NORMAL ||
                           slotType == ContainerSlotType.VALIDITY) {
                    type = SlotType.NORMAL;
                } else {
                    type = SlotType.NORMAL;
                }

                GuiSlot guiSlot = new ReplicationGuiSlot(type, this, slot.x - 1, slot.y - 1, containerSlot);

                if (slotType == ContainerSlotType.IGNORED ||
                    containerSlot instanceof mekanism.common.inventory.container.slot.VirtualInventoryContainerSlot) {
                    guiSlot.visible = false;
                }

                containerSlot.addWarnings(guiSlot);
                mekanism.common.inventory.container.slot.SlotOverlay overlay = containerSlot.getSlotOverlay();
                if (overlay != null) {
                    guiSlot.with(overlay);
                }
                
                this.addRenderableWidget(guiSlot);
            }
        }
    }

    private static class ReplicationGuiSlot extends GuiSlot {
        private final InventoryContainerSlot containerSlot;

        public ReplicationGuiSlot(SlotType type, mekanism.client.gui.IGuiWrapper gui, int x, int y, InventoryContainerSlot containerSlot) {
            super(type, gui, x, y);
            this.containerSlot = containerSlot;
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
            
            mekanism.common.inventory.container.slot.SlotOverlay overlay = containerSlot.getSlotOverlay();
            if (overlay != null) {
                guiGraphics.blit(overlay.getTexture(), this.relativeX, this.relativeY, 0.0F, 0.0F, overlay.getWidth(), overlay.getHeight(), overlay.getWidth(), overlay.getHeight());
            }
            
            this.drawContents(guiGraphics);
        }
    }

    private static class ReplicationGuiProgress extends mekanism.client.gui.element.GuiElement {
        private final java.util.function.DoubleSupplier progressSupplier;

        public ReplicationGuiProgress(java.util.function.DoubleSupplier progressSupplier, mekanism.client.gui.IGuiWrapper gui, int x, int y) {
            super(gui, x, y, 22, 15);
            this.progressSupplier = progressSupplier;
        }

        @Override
        public void renderWidget(net.minecraft.client.gui.GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
            // Background arrow
            guiGraphics.blit(REPLICATION_BACKGROUND, this.relativeX, this.relativeY, 177, 61, 22, 15, 256, 256);

            // Foreground green arrow
            double progress = progressSupplier.getAsDouble();
            if (progress > 0) {
                int width = (int) (progress * 22);
                if (width > 0) {
                    guiGraphics.blit(REPLICATION_BACKGROUND, this.relativeX, this.relativeY, 177, 77, width, 15, 256, 256);
                }
            }
        }

        @Override
        public void drawBackground(net.minecraft.client.gui.GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
            this.renderWidget(guiGraphics, mouseX, mouseY, partialTicks);
        }
    }

    private static final net.minecraft.resources.ResourceLocation REPLICATION_BACKGROUND = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("replication", "textures/gui/background.png");
    private static final net.minecraft.resources.ResourceLocation CUSTOM_SLOT_TEXTURE = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("replicatemekanism", "textures/gui/slot.png");

    @Override
    protected void renderBg(net.minecraft.client.gui.GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        mekanism.client.render.MekanismRenderer.resetColor(guiGraphics);
        if (this.getXSize() < 8 || this.getYSize() < 8) {
            return;
        }
        // Draw upper half (machine area): texture 0..74 -> screen 0..74
        guiGraphics.blit(REPLICATION_BACKGROUND, this.leftPos, this.topPos, 0, 0, this.imageWidth, 74);
        // Draw lower half (inventory area): texture 96..184 (height 88) -> screen 74..162
        guiGraphics.blit(REPLICATION_BACKGROUND, this.leftPos, this.topPos + 74, 0, 96, this.imageWidth, 88);
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
