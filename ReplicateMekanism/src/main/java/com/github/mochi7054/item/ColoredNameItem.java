package com.github.mochi7054.item;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * A simple Item that displays its name in a custom text color.
 */
public class ColoredNameItem extends Item {

    private final TextColor nameColor;

    public ColoredNameItem(Properties properties, TextColor nameColor) {
        super(properties);
        this.nameColor = nameColor;
    }

    @Override
    public MutableComponent getName(ItemStack stack) {
        return super.getName(stack).copy().withStyle(style -> style.withColor(nameColor));
    }
}
