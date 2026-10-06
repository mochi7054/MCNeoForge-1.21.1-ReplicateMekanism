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

        // Draw custom Replication dark background inside the bar
        guiGraphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0xFF181A24);

        if (tank != null && tank.getMatterType() != null) {
            double max = tank.getCapacity();
            double current = tank.getMatterAmount();
            double ratio = max > 0 ? current / max : 0;

            if (ratio > 0) {
                int color = getMatterColor(tank.getMatterType());
                if (horizontal) {
                    int fillWidth = (int) Math.round((w - 2) * ratio);
                    guiGraphics.fill(x + 1, y + 1, x + 1 + fillWidth, y + h - 1, color);
                } else {
                    int fillHeight = (int) Math.round((h - 2) * ratio);
                    guiGraphics.fill(x + 1, y + h - 1 - fillHeight, x + w - 1, y + h - 1, color);
                }
            }
        }

        // Draw Replication border
        drawReplicationBorder(guiGraphics);
    }

    @Override
    public void renderToolTip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderToolTip(guiGraphics, mouseX, mouseY);
        if (tank == null || tank.getMatterType() == null) {
            Component tooltipText = Component.translatable("tooltip.replicatemekanism.matter_tank_empty", tank != null ? (int) tank.getCapacity() : 0);
            guiGraphics.renderTooltip(net.minecraft.client.Minecraft.getInstance().font, tooltipText, mouseX, mouseY);
            return;
        }
        var matterType = tank.getMatterType();
        String typeName = matterType.getName().toLowerCase();
        Component typeComponent = Component.translatable("replication.matter_type." + typeName);
        Component tooltipText = Component.translatable("tooltip.replicatemekanism.matter_tank_amount", typeComponent, (int) tank.getMatterAmount(), (int) tank.getCapacity());
        guiGraphics.renderTooltip(net.minecraft.client.Minecraft.getInstance().font, tooltipText, mouseX, mouseY);
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
        try {
            float[] rgb = type.getColor().get();
            if (rgb != null && rgb.length >= 3) {
                int r = (int) (rgb[0] * 255.0f);
                int g = (int) (rgb[1] * 255.0f);
                int b = (int) (rgb[2] * 255.0f);
                return 0xFF000000 | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
            }
        } catch (Throwable ignored) {}
        int hash = type.getName().hashCode();
        int r = (hash & 0x7F) + 128;
        int g = ((hash >> 8) & 0x7F) + 128;
        int b = ((hash >> 16) & 0x7F) + 128;
        return 0xFF000000 | (r << 16) | (g << 8) | b;
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
