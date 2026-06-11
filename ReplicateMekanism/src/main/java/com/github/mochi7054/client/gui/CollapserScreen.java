package com.github.mochi7054.client.gui;

import com.github.mochi7054.block.entity.CollapserBlockEntity;
import com.github.mochi7054.inventory.container.CollapserMenu;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.bar.GuiFluidBar;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
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

public class CollapserScreen extends GuiConfigurableTile<CollapserBlockEntity, CollapserMenu> {

    public CollapserScreen(CollapserMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = menu.getTileEntity().getTier() == com.github.mochi7054.block.ReplicaTier.ULTIMATE ? 210 : 176;
        this.imageHeight = 176;
        this.inventoryLabelX = menu.getTileEntity().getTier() == com.github.mochi7054.block.ReplicaTier.ULTIMATE ? 26 : 8;
        this.inventoryLabelY = 82;
        this.titleLabelY = 10;
        this.dynamicSlots = true;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        CollapserBlockEntity tile = menu.getTileEntity();
        com.github.mochi7054.block.ReplicaTier tier = tile.getTier();

        if (tier == com.github.mochi7054.block.ReplicaTier.STANDARD) {
            // Fluid Tank Bars on the right for each of the 8 matter types
            this.addElement(new GuiFluidBar(this, GuiFluidBar.getProvider(tile.earthTank, tile.getFluidTanks(null)), 70, 25, 5, 42, false));
            this.addElement(new GuiFluidBar(this, GuiFluidBar.getProvider(tile.netherTank, tile.getFluidTanks(null)), 78, 25, 5, 42, false));
            this.addElement(new GuiFluidBar(this, GuiFluidBar.getProvider(tile.organicTank, tile.getFluidTanks(null)), 86, 25, 5, 42, false));
            this.addElement(new GuiFluidBar(this, GuiFluidBar.getProvider(tile.enderTank, tile.getFluidTanks(null)), 94, 25, 5, 42, false));
            this.addElement(new GuiFluidBar(this, GuiFluidBar.getProvider(tile.metallicTank, tile.getFluidTanks(null)), 102, 25, 5, 42, false));
            this.addElement(new GuiFluidBar(this, GuiFluidBar.getProvider(tile.preciousTank, tile.getFluidTanks(null)), 110, 25, 5, 42, false));
            this.addElement(new GuiFluidBar(this, GuiFluidBar.getProvider(tile.livingTank, tile.getFluidTanks(null)), 118, 25, 5, 42, false));
            this.addElement(new GuiFluidBar(this, GuiFluidBar.getProvider(tile.quantumTank, tile.getFluidTanks(null)), 126, 25, 5, 42, false));

            // Energy Bar on the right
            this.addElement(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), 162, 25, 42));

            // Progress Bar
            this.addElement(new CollapserGuiProgress(() -> tile.getScaledProgress(), this, 41, 41));
        } else {
            // Shrunk fluid slots (8 slots) at Y=6
            int fluidStartX = tile.getTier() == com.github.mochi7054.block.ReplicaTier.ULTIMATE ? 25 : 8;
            this.addElement(new GuiSlot(SlotType.NORMAL, this, fluidStartX, 6));
            this.addElement(new GuiFluidBar(this, GuiFluidBar.getProvider(tile.earthTank, tile.getFluidTanks(null)), fluidStartX + 1, 7, 16, 16, false));

            this.addElement(new GuiSlot(SlotType.NORMAL, this, fluidStartX + 18, 6));
            this.addElement(new GuiFluidBar(this, GuiFluidBar.getProvider(tile.netherTank, tile.getFluidTanks(null)), fluidStartX + 19, 7, 16, 16, false));

            this.addElement(new GuiSlot(SlotType.NORMAL, this, fluidStartX + 36, 6));
            this.addElement(new GuiFluidBar(this, GuiFluidBar.getProvider(tile.organicTank, tile.getFluidTanks(null)), fluidStartX + 37, 7, 16, 16, false));

            this.addElement(new GuiSlot(SlotType.NORMAL, this, fluidStartX + 54, 6));
            this.addElement(new GuiFluidBar(this, GuiFluidBar.getProvider(tile.enderTank, tile.getFluidTanks(null)), fluidStartX + 55, 7, 16, 16, false));

            this.addElement(new GuiSlot(SlotType.NORMAL, this, fluidStartX + 72, 6));
            this.addElement(new GuiFluidBar(this, GuiFluidBar.getProvider(tile.metallicTank, tile.getFluidTanks(null)), fluidStartX + 73, 7, 16, 16, false));

            this.addElement(new GuiSlot(SlotType.NORMAL, this, fluidStartX + 90, 6));
            this.addElement(new GuiFluidBar(this, GuiFluidBar.getProvider(tile.preciousTank, tile.getFluidTanks(null)), fluidStartX + 91, 7, 16, 16, false));

            this.addElement(new GuiSlot(SlotType.NORMAL, this, fluidStartX + 108, 6));
            this.addElement(new GuiFluidBar(this, GuiFluidBar.getProvider(tile.livingTank, tile.getFluidTanks(null)), fluidStartX + 109, 7, 16, 16, false));

            this.addElement(new GuiSlot(SlotType.NORMAL, this, fluidStartX + 126, 6));
            this.addElement(new GuiFluidBar(this, GuiFluidBar.getProvider(tile.quantumTank, tile.getFluidTanks(null)), fluidStartX + 127, 7, 16, 16, false));

            // Energy Bar on the right (shifted to X=204 for Ultimate, X=170 otherwise)
            this.addElement(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), tile.getTier() == com.github.mochi7054.block.ReplicaTier.ULTIMATE ? 204 : 170, 6, 75));

            // Progress Bars (Downward arrows)
            int center = tile.getTier() == com.github.mochi7054.block.ReplicaTier.ULTIMATE ? 105 : 88;
            int startX = center - (18 * tile.getTier().getSlotCount()) / 2 + 1;
            for (int i = 0; i < tile.getTier().getSlotCount(); i++) {
                final int idx = i;
                this.addElement(new mekanism.client.gui.element.progress.GuiProgress(() -> tile.getScaledProgress(idx), mekanism.client.gui.element.progress.ProgressType.DOWN, this, startX + idx * 18 + 4, 43));
            }
        }

        // Energy Tab on the left
        this.addElement(new GuiEnergyTab(this, tile.getEnergyContainer(), () -> true));

        // 不要なサイドホルダーを非表示
        for (net.minecraft.client.gui.components.events.GuiEventListener listener : this.children()) {
            if (listener instanceof mekanism.client.gui.element.GuiElement element) {
                if (element.getClass().getName().contains("GuiSideHolder")
                        && element.getRelativeX() < 0 && element.getRelativeY() < 10) {
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
                } else if (slotType == ContainerSlotType.INPUT || slotType == ContainerSlotType.OUTPUT
                        || slotType == ContainerSlotType.EXTRA) {
                    type = SlotType.NORMAL;
                } else if (slotType == ContainerSlotType.POWER) {
                    type = SlotType.POWER;
                } else {
                    type = SlotType.NORMAL;
                }

                GuiSlot guiSlot = new CollapserGuiSlot(type, this, slot.x - 1, slot.y - 1, containerSlot);

                if (slotType == ContainerSlotType.IGNORED
                        || containerSlot instanceof mekanism.common.inventory.container.slot.VirtualInventoryContainerSlot) {
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

    // ---- カスタムスロット描画 ----
    private static class CollapserGuiSlot extends GuiSlot {
        private final InventoryContainerSlot containerSlot;

        public CollapserGuiSlot(SlotType type, mekanism.client.gui.IGuiWrapper gui, int x, int y,
                InventoryContainerSlot containerSlot) {
            super(type, gui, x, y);
            this.containerSlot = containerSlot;
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
            if (!isRenderAboveSlots()) customDraw(guiGraphics);
        }

        @Override
        public void drawBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
            if (isRenderAboveSlots()) customDraw(guiGraphics);
        }

        private boolean isRenderAboveSlots() {
            try {
                java.lang.reflect.Field f = GuiSlot.class.getDeclaredField("renderAboveSlots");
                f.setAccessible(true);
                return f.getBoolean(this);
            } catch (Exception e) {
                return false;
            }
        }

        private void customDraw(GuiGraphics guiGraphics) {
            guiGraphics.blit(CUSTOM_SLOT_TEXTURE, this.relativeX, this.relativeY, 0, 0, 18, 18, 18, 18);
            mekanism.common.inventory.container.slot.SlotOverlay overlay = containerSlot.getSlotOverlay();
            if (overlay != null) {
                guiGraphics.blit(overlay.getTexture(), this.relativeX, this.relativeY,
                        0f, 0f, overlay.getWidth(), overlay.getHeight(),
                        overlay.getWidth(), overlay.getHeight());
            }
            this.drawContents(guiGraphics);
        }
    }

    // ---- 変換進捗矢印 ----
    private static class CollapserGuiProgress extends mekanism.client.gui.element.GuiElement {
        private final java.util.function.DoubleSupplier progressSupplier;

        public CollapserGuiProgress(java.util.function.DoubleSupplier progressSupplier,
                mekanism.client.gui.IGuiWrapper gui, int x, int y) {
            super(gui, x, y, 22, 15);
            this.progressSupplier = progressSupplier;
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
            // 背景矢印
            guiGraphics.blit(REPLICATION_BACKGROUND, this.relativeX, this.relativeY, 177, 61, 22, 15, 256, 256);
            double progress = progressSupplier.getAsDouble();
            if (progress > 0) {
                int width = (int) (progress * 22);
                if (width > 0) {
                    guiGraphics.blit(REPLICATION_BACKGROUND, this.relativeX, this.relativeY, 177, 77, width, 15, 256, 256);
                }
            }
        }

        @Override
        public void drawBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
            this.renderWidget(guiGraphics, mouseX, mouseY, partialTicks);
        }
    }

    private static final net.minecraft.resources.ResourceLocation REPLICATION_BACKGROUND =
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("replication", "textures/gui/background.png");
    private static final net.minecraft.resources.ResourceLocation CUSTOM_SLOT_TEXTURE =
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("replicatemekanism", "textures/gui/slot.png");

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        mekanism.client.render.MekanismRenderer.resetColor(guiGraphics);
        if (this.getXSize() < 8 || this.getYSize() < 8) return;
        if (this.imageWidth > 176) {
            // Draw upper half (machine area) extended
            guiGraphics.blit(REPLICATION_BACKGROUND, this.leftPos, this.topPos, 0, 0, 170, 88);
            for (int x = 170; x < 204; x++) {
                guiGraphics.blit(REPLICATION_BACKGROUND, this.leftPos + x, this.topPos, 170, 0, 1, 88);
            }
            guiGraphics.blit(REPLICATION_BACKGROUND, this.leftPos + 204, this.topPos, 170, 0, 6, 88);

            // Draw lower half (inventory area) extended
            guiGraphics.blit(REPLICATION_BACKGROUND, this.leftPos, this.topPos + 88, 0, 96, 170, 88);
            for (int x = 170; x < 204; x++) {
                guiGraphics.blit(REPLICATION_BACKGROUND, this.leftPos + x, this.topPos + 88, 170, 96, 1, 88);
            }
            guiGraphics.blit(REPLICATION_BACKGROUND, this.leftPos + 204, this.topPos + 88, 170, 96, 6, 88);
        } else {
            guiGraphics.blit(REPLICATION_BACKGROUND, this.leftPos, this.topPos, 0, 0, this.imageWidth, 88);
            guiGraphics.blit(REPLICATION_BACKGROUND, this.leftPos, this.topPos + 88, 0, 96, this.imageWidth, 88);
        }
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
