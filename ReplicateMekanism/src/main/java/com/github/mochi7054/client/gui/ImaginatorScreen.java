package com.github.mochi7054.client.gui;

import com.github.mochi7054.block.entity.ImaginatorBlockEntity;
import com.github.mochi7054.inventory.container.ImaginatorMenu;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.GuiUpArrow;
import com.github.mochi7054.client.gui.ReplicationGuiFluidBar;
import com.github.mochi7054.client.gui.ReplicationGuiVerticalPowerBar;
import mekanism.client.gui.element.bar.GuiFluidBar;
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
        com.github.mochi7054.block.ReplicaTier tier = menu.getTileEntity().getTier();
        this.imageWidth = switch (tier) {
            case STANDARD, BASIC, ADVANCED -> 174;
            case ELITE -> 180;
            case ULTIMATE -> 218;
        };
        this.imageHeight = tier == com.github.mochi7054.block.ReplicaTier.STANDARD ? 174 : 184;
        this.inventoryLabelX = switch (tier) {
            case STANDARD, BASIC, ADVANCED -> 8;
            case ELITE -> 10;
            case ULTIMATE -> 29;
        };
        this.inventoryLabelY = tier == com.github.mochi7054.block.ReplicaTier.STANDARD ? 82 : 92;
        this.titleLabelY = tier == com.github.mochi7054.block.ReplicaTier.STANDARD ? 10 : 7;
        this.dynamicSlots = true;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        ImaginatorBlockEntity tile = menu.getTileEntity();
        com.github.mochi7054.block.ReplicaTier tier = tile.getTier();

        if (tier == com.github.mochi7054.block.ReplicaTier.STANDARD) {
            // Fluid Tank Bars on the left for each of the 8 matter types
            this.addElement(new ReplicationGuiFluidBar(this, GuiFluidBar.getProvider(tile.earthTank, tile.getFluidTanks(null)), 8, 25, 5, 42, false));
            this.addElement(new ReplicationGuiFluidBar(this, GuiFluidBar.getProvider(tile.netherTank, tile.getFluidTanks(null)), 16, 25, 5, 42, false));
            this.addElement(new ReplicationGuiFluidBar(this, GuiFluidBar.getProvider(tile.organicTank, tile.getFluidTanks(null)), 24, 25, 5, 42, false));
            this.addElement(new ReplicationGuiFluidBar(this, GuiFluidBar.getProvider(tile.enderTank, tile.getFluidTanks(null)), 32, 25, 5, 42, false));
            this.addElement(new ReplicationGuiFluidBar(this, GuiFluidBar.getProvider(tile.metallicTank, tile.getFluidTanks(null)), 40, 25, 5, 42, false));
            this.addElement(new ReplicationGuiFluidBar(this, GuiFluidBar.getProvider(tile.preciousTank, tile.getFluidTanks(null)), 48, 25, 5, 42, false));
            this.addElement(new ReplicationGuiFluidBar(this, GuiFluidBar.getProvider(tile.livingTank, tile.getFluidTanks(null)), 56, 25, 5, 42, false));
            this.addElement(new ReplicationGuiFluidBar(this, GuiFluidBar.getProvider(tile.quantumTank, tile.getFluidTanks(null)), 64, 25, 5, 42, false));

            // Energy Bar on the right
            this.addElement(new ReplicationGuiVerticalPowerBar(this, tile.getEnergyContainer(), 162, 25, 42));

            // Progress Bar
            this.addElement(new ReplicationGuiProgress(() -> tile.getScaledProgress(), this, 95, 41));
        } else {
            // 8 fluid tanks, centered horizontally, no slot background borders
            int fluidStartX = (this.imageWidth - 142) / 2;
            this.addElement(new ReplicationGuiFluidBar(this, GuiFluidBar.getProvider(tile.earthTank, tile.getFluidTanks(null)), fluidStartX, 84, 16, 5, true));
            this.addElement(new ReplicationGuiFluidBar(this, GuiFluidBar.getProvider(tile.netherTank, tile.getFluidTanks(null)), fluidStartX + 18, 84, 16, 5, true));
            this.addElement(new ReplicationGuiFluidBar(this, GuiFluidBar.getProvider(tile.organicTank, tile.getFluidTanks(null)), fluidStartX + 36, 84, 16, 5, true));
            this.addElement(new ReplicationGuiFluidBar(this, GuiFluidBar.getProvider(tile.enderTank, tile.getFluidTanks(null)), fluidStartX + 54, 84, 16, 5, true));
            this.addElement(new ReplicationGuiFluidBar(this, GuiFluidBar.getProvider(tile.metallicTank, tile.getFluidTanks(null)), fluidStartX + 72, 84, 16, 5, true));
            this.addElement(new ReplicationGuiFluidBar(this, GuiFluidBar.getProvider(tile.preciousTank, tile.getFluidTanks(null)), fluidStartX + 90, 84, 16, 5, true));
            this.addElement(new ReplicationGuiFluidBar(this, GuiFluidBar.getProvider(tile.livingTank, tile.getFluidTanks(null)), fluidStartX + 108, 84, 16, 5, true));
            this.addElement(new ReplicationGuiFluidBar(this, GuiFluidBar.getProvider(tile.quantumTank, tile.getFluidTanks(null)), fluidStartX + 126, 84, 16, 5, true));

            // Energy Bar on the right (always 12px from the right edge)
            this.addElement(new ReplicationGuiVerticalPowerBar(this, tile.getEnergyContainer(), this.imageWidth - 12, 25, 42));

            // Progress Bars (Downward arrows) at Y=42 using Replication style
            int startX;
            int gap;
            if (tier == com.github.mochi7054.block.ReplicaTier.BASIC) {
                startX = 55;
                gap = 38;
            } else if (tier == com.github.mochi7054.block.ReplicaTier.ADVANCED) {
                startX = 35;
                gap = 26;
            } else if (tier == com.github.mochi7054.block.ReplicaTier.ELITE) {
                startX = 32;
                gap = 19;
            } else { // ULTIMATE
                startX = 30;
                gap = 19;
            }
            for (int i = 0; i < tile.getTier().getSlotCount(); i++) {
                final int idx = i;
                this.addElement(new ReplicationGuiProgressDown(() -> tile.getScaledProgress(idx), this, startX + idx * gap + 5, 39));
            }
        }

        // Energy Tab on the left
        this.addElement(new GuiEnergyTab(this, tile.getEnergyContainer(), () -> true));

        if (tier != com.github.mochi7054.block.ReplicaTier.STANDARD) {
            this.addElement(new GuiImaginatorSortingTab(this, tile));
        }

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

    private static class ReplicationGuiProgressDown extends mekanism.client.gui.element.GuiElement {
        private final java.util.function.DoubleSupplier progressSupplier;

        public ReplicationGuiProgressDown(java.util.function.DoubleSupplier progressSupplier, mekanism.client.gui.IGuiWrapper gui, int x, int y) {
            super(gui, x, y, 8, 15);
            this.progressSupplier = progressSupplier;
        }

        @Override
        public void renderWidget(net.minecraft.client.gui.GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
            // Background arrow (left half of progress_down.png, X=0..7)
            guiGraphics.blit(PROGRESS_DOWN_TEXTURE, this.relativeX, this.relativeY, 0, 0, 8, 15, 16, 15);

            // Foreground green arrow (right half of progress_down.png, X=8..15)
            double progress = progressSupplier.getAsDouble();
            if (progress > 0) {
                int height = (int) (progress * 15);
                if (height > 0) {
                    guiGraphics.blit(PROGRESS_DOWN_TEXTURE, this.relativeX, this.relativeY, 8, 0, 8, height, 16, 15);
                }
            }
        }

        @Override
        public void drawBackground(net.minecraft.client.gui.GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
            this.renderWidget(guiGraphics, mouseX, mouseY, partialTicks);
        }
    }

    private static final net.minecraft.resources.ResourceLocation REPLICATION_BACKGROUND = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("replication", "textures/gui/background.png");
    private static final net.minecraft.resources.ResourceLocation PROGRESS_DOWN_TEXTURE = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("replicatemekanism", "textures/gui/progress_down.png");
    private static final net.minecraft.resources.ResourceLocation CUSTOM_SLOT_TEXTURE = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("replicatemekanism", "textures/gui/slot.png");

    private void drawMachineArea(net.minecraft.client.gui.GuiGraphics guiGraphics, int x, int y, int width, boolean standard) {
        if (standard) {
            guiGraphics.blit(REPLICATION_BACKGROUND, x, y, 0, 0, width, 88);
        } else {
            // Draw top 80px
            guiGraphics.blit(REPLICATION_BACKGROUND, x, y, 0, 0, 8, 80);
            for (int dx = 8; dx < width - 6; dx++) {
                guiGraphics.blit(REPLICATION_BACKGROUND, x + dx, y, 150, 0, 1, 80);
            }
            guiGraphics.blit(REPLICATION_BACKGROUND, x + width - 6, y, 168, 0, 6, 80);

            // Draw stretched vertical middle 10px
            for (int dy = 0; dy < 10; dy++) {
                int curY = y + 80 + dy;
                guiGraphics.blit(REPLICATION_BACKGROUND, x, curY, 0, 80, 8, 1);
                for (int dx = 8; dx < width - 6; dx++) {
                    guiGraphics.blit(REPLICATION_BACKGROUND, x + dx, curY, 150, 80, 1, 1);
                }
                guiGraphics.blit(REPLICATION_BACKGROUND, x + width - 6, curY, 168, 80, 6, 1);
            }

            // Draw bottom 8px
            int curY = y + 90;
            guiGraphics.blit(REPLICATION_BACKGROUND, x, curY, 0, 80, 8, 8);
            for (int dx = 8; dx < width - 6; dx++) {
                guiGraphics.blit(REPLICATION_BACKGROUND, x + dx, curY, 150, 80, 1, 8);
            }
            guiGraphics.blit(REPLICATION_BACKGROUND, x + width - 6, curY, 168, 80, 6, 8);
        }
    }

    private void drawInventoryArea(net.minecraft.client.gui.GuiGraphics guiGraphics, int left, int top, int width, int xOffset) {
        // 1. Draw top 4px of inventory area (with top border)
        guiGraphics.blit(REPLICATION_BACKGROUND, left, top, 0, 96, 6, 4);
        for (int x = 6; x < width - 6; x++) {
            guiGraphics.blit(REPLICATION_BACKGROUND, left + x, top, 150, 96, 1, 4);
        }
        guiGraphics.blit(REPLICATION_BACKGROUND, left + width - 6, top, 168, 96, 6, 4);

        // 2. Draw middle 80px (with left border, solid margins, grid, and right border)
        int midY = top + 4;
        // Left border
        guiGraphics.blit(REPLICATION_BACKGROUND, left, midY, 0, 100, 6, 80);
        // Draw solid background color for the entire inner width
        guiGraphics.fill(left + 6, midY, left + width - 6, midY + 76, 0xFF252A37);
        // Right border
        guiGraphics.blit(REPLICATION_BACKGROUND, left + width - 6, midY, 168, 100, 6, 80);

        // 3. Draw bottom 6px of inventory area (with bottom border)
        int botY = top + 80;
        guiGraphics.blit(REPLICATION_BACKGROUND, left, botY, 0, 176, 6, 6);
        for (int x = 6; x < width - 6; x++) {
            guiGraphics.blit(REPLICATION_BACKGROUND, left + x, botY, 150, 176, 1, 6);
        }
        guiGraphics.blit(REPLICATION_BACKGROUND, left + width - 6, botY, 168, 176, 6, 6);

        // Clear the extra slot border green line (texture Y=176 -> screen botY) in the empty spaces
        if (xOffset > 6) {
            guiGraphics.fill(left + 6, botY, left + xOffset, botY + 2, 0xFF252A37);
        }
        if (left + xOffset + 162 < left + width - 6) {
            // Fill up to left + width - 5 (erasing column relative X = width - 6 at botY and botY + 1)
            guiGraphics.fill(left + xOffset + 161, botY, left + width - 5, botY + 2, 0xFF252A37);
            // Also erase the column relative X = width - 6 in the middle and top sections to prevent texture bleeding of cyan slot lines
            guiGraphics.fill(left + width - 6, top, left + width - 5, botY, 0xFF252A37);
        }
    }

    @Override
    protected void renderBg(net.minecraft.client.gui.GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        mekanism.client.render.MekanismRenderer.resetColor(guiGraphics);
        if (this.getXSize() < 8 || this.getYSize() < 8) {
            return;
        }
        com.github.mochi7054.block.ReplicaTier tier = menu.getTileEntity().getTier();
        boolean standard = tier == com.github.mochi7054.block.ReplicaTier.STANDARD;
        
        int xOffset = switch (tier) {
            case STANDARD, BASIC, ADVANCED -> 8;
            case ELITE -> 10;
            case ULTIMATE -> 29;
        };
        
        drawMachineArea(guiGraphics, this.leftPos, this.topPos, this.imageWidth, standard);
        drawInventoryArea(guiGraphics, this.leftPos, this.topPos + (standard ? 88 : 98), this.imageWidth, xOffset);
    }

    @Override
    protected void drawForegroundText(GuiGraphics graphics, int mouseX, int mouseY) {
        int titleWidth = this.font.width(this.title);
        int titleX = (this.imageWidth - titleWidth) / 2;
        graphics.drawString(this.font, this.title, titleX, this.titleLabelY, 0xFF38FF70, false);
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0xFF38FF70, false);
        super.drawForegroundText(graphics, mouseX, mouseY);
    }

    private static final net.minecraft.resources.ResourceLocation SORTING_ICON =
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("mekanism", "gui/sorting.png");

    private static class GuiImaginatorSortingTab extends mekanism.client.gui.element.GuiInsetElement<ImaginatorBlockEntity> {
        public GuiImaginatorSortingTab(mekanism.client.gui.IGuiWrapper gui, ImaginatorBlockEntity tile) {
            super(SORTING_ICON, gui, tile, -26, 62, 35, 18, true);
            this.setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.translatable("gui.replicatemekanism.task_sharing")));
        }

        @Override
        protected void colorTab(GuiGraphics guiGraphics) {
            mekanism.client.render.MekanismRenderer.color(guiGraphics, mekanism.client.SpecialColors.TAB_FACTORY_SORT);
        }

        @Override
        public void drawBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
            super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
            Component stateText = mekanism.common.util.text.BooleanStateDisplay.OnOff.of(dataSource.sorting).getTextComponent();
            this.drawScrollingString(guiGraphics, stateText, 0, 24, mekanism.client.render.IFancyFontRenderer.TextAlignment.CENTER, this.titleTextColor(), 3, false);
        }

        @Override
        public void onClick(double mouseX, double mouseY, int button) {
            mekanism.common.network.PacketUtils.sendToServer(
                new com.github.mochi7054.network.PacketSetImaginatorSorting(dataSource.getBlockPos(), !dataSource.sorting)
            );
        }
    }
}
