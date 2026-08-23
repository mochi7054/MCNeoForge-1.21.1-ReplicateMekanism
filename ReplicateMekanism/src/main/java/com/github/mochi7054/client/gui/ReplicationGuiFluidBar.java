package com.github.mochi7054.client.gui;

import com.github.mochi7054.fluid.SimpleMatterTank;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class ReplicationGuiFluidBar extends GuiElement {

    private final SimpleMatterTank tank;
    private final boolean horizontal;

    public ReplicationGuiFluidBar(IGuiWrapper gui, SimpleMatterTank tank, int x, int y, int width, int height, boolean horizontal) {
        super(gui, x, y, width, height);
        this.tank = tank;
        this.horizontal = horizontal;
    }

    @Override
    public void drawBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        int x = this.relativeX;
        int y = this.relativeY;
        int w = this.width;
        int h = this.height;

        // Draw custom Replication dark background inside the bar (like the inside of a slot)
        guiGraphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0xFF181A24);

        // Draw the matter level fill overlay
        double max = tank.getCapacity();
        double current = tank.getMatterAmount();
        double ratio = max > 0 ? current / max : 0;

        if (ratio > 0) {
            int color = getMatterColor(tank.getMatter().getMatterType());
            if (horizontal) {
                int fillWidth = (int) Math.round((w - 2) * ratio);
                guiGraphics.fill(x + 1, y + 1, x + 1 + fillWidth, y + h - 1, color);
            } else {
                int fillHeight = (int) Math.round((h - 2) * ratio);
                guiGraphics.fill(x + 1, y + h - 1 - fillHeight, x + w - 1, y + h - 1, color);
            }
        }

        // Draw Replication green border
        drawReplicationBorder(guiGraphics);
    }

    @Override
    public void renderToolTip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderToolTip(guiGraphics, mouseX, mouseY);
        String name = tank.getMatter().getMatterType().getName();
        // Capitalize first letter
        if (name != null && !name.isEmpty()) {
            name = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
        }
        String txt = name + " Matter: " + (int) tank.getMatterAmount() + " / " + (int) tank.getCapacity();
        guiGraphics.renderTooltip(net.minecraft.client.Minecraft.getInstance().font, Component.literal(txt), mouseX, mouseY);
    }

    private int getMatterColor(com.buuz135.replication.api.IMatterType type) {
        if (type == null) return 0xFFFFFFFF;
        String name = type.getName().toLowerCase();
        if (name.contains("earth")) return 0xFF5C3A21;
        if (name.contains("nether")) return 0xFFB71C1C;
        if (name.contains("organic")) return 0xFF2E7D32;
        if (name.contains("ender")) return 0xFF004D40;
        if (name.contains("metallic")) return 0xFF78909C;
        if (name.contains("precious")) return 0xFFFBC02D;
        if (name.contains("living")) return 0xFF81C784;
        if (name.contains("quantum")) return 0xFF3F51B5;
        return 0xFFFFFFFF;
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
