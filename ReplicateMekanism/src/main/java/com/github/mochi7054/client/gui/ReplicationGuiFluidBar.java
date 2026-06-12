package com.github.mochi7054.client.gui;

import mekanism.client.gui.element.bar.GuiFluidBar;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.bar.GuiTankBar.TankInfoProvider;
import net.neoforged.neoforge.fluids.FluidStack;
import net.minecraft.client.gui.GuiGraphics;

public class ReplicationGuiFluidBar extends GuiFluidBar {

    public ReplicationGuiFluidBar(IGuiWrapper gui, TankInfoProvider<FluidStack> provider, int x, int y, int width, int height, boolean horizontal) {
        super(gui, provider, x, y, width, height, horizontal);
    }

    @Override
    public void drawBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        int x = this.relativeX;
        int y = this.relativeY;
        int w = this.width;
        int h = this.height;

        // Draw custom Replication dark background inside the bar (like the inside of a slot)
        guiGraphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0xFF181A24);

        // Draw the fluid fill overlay
        double level = getHandler().getLevel();
        if (level > 0) {
            renderBarOverlay(guiGraphics, mouseX, mouseY, partialTicks, level);
        }

        // Draw Replication green border
        drawReplicationBorder(guiGraphics);
    }

    private void drawReplicationBorder(GuiGraphics guiGraphics) {
        int x = this.relativeX;
        int y = this.relativeY;
        int w = this.width;
        int h = this.height;

        int lightGreen = 0xFF72E567;
        int darkGreen = 0xFF158C82;
        int cornerColor = 0xFF19A683;

        guiGraphics.fill(x + 1, y, x + w - 1, y + 1, darkGreen);
        guiGraphics.fill(x, y + 1, x + 1, y + h - 1, darkGreen);
        guiGraphics.fill(x + 1, y + h - 1, x + w - 1, y + h, lightGreen);
        guiGraphics.fill(x + w - 1, y + 1, x + w, y + h - 1, lightGreen);

        guiGraphics.fill(x, y, x + 1, y + 1, cornerColor);
        guiGraphics.fill(x + w - 1, y, x + w, y + 1, cornerColor);
        guiGraphics.fill(x, y + h - 1, x + 1, y + h, cornerColor);
        guiGraphics.fill(x + w - 1, y + h - 1, x + w, y + h, cornerColor);
    }
}

