package com.github.mochi7054.forensic;

import com.github.mochi7054.client.gui.ReplicationGuiVerticalPowerBar;
import mekanism.api.text.ILangEntry;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class ForensicChamberScreen extends GuiConfigurableTile<ForensicChamberBlockEntity, ForensicChamberMenu> {

    public ForensicChamberScreen(ForensicChamberMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 74;
        this.titleLabelY = 6;
        this.dynamicSlots = true;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        ForensicChamberBlockEntity tile = menu.getTileEntity();

        // Energy Bar
        this.addElement(new ReplicationGuiVerticalPowerBar(this, tile.energyContainer, 162, 20, 52));
        this.addElement(new GuiEnergyTab(this, tile.energyContainer, () -> true));

        // Slots
        this.addElement(new GuiSlot(SlotType.INPUT, this, 43, 34));
        this.addElement(new GuiSlot(SlotType.EXTRA, this, 79, 19));
        this.addElement(new GuiSlot(SlotType.OUTPUT_LARGE, this, 111, 30));
        this.addElement(new GuiSlot(SlotType.POWER, this, 142, 34));

        // Scan / Identify Button
        ILangEntry scanLang = new ILangEntry() {
            @Override
            public String getTranslationKey() {
                return "gui.replicatemekanism.forensic.scan";
            }
        };
        TranslationButton scanButton = new TranslationButton(
                this,
                70, 48, 36, 18,
                scanLang,
                (element, mouseX, mouseY) -> {
                    PacketDistributor.sendToServer(new PacketScanForensicChamber(tile.getBlockPos()));
                    return true;
                }
        );
        scanButton.setTooltip(Tooltip.create(Component.translatable("gui.replicatemekanism.forensic.scan.tooltip")));
        this.addElement(scanButton);
    }

    @Override
    protected void drawForegroundText(GuiGraphics graphics, int mouseX, int mouseY) {
        int titleWidth = this.font.width(this.title);
        int titleX = (this.imageWidth - titleWidth) / 2;
        graphics.drawString(this.font, this.title, titleX, this.titleLabelY, 0x404040, false);
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
        super.drawForegroundText(graphics, mouseX, mouseY);
    }
}