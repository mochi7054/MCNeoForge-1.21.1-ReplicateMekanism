package com.github.mochi7054.lang;

import mekanism.api.text.EnumColor;
import mekanism.api.text.ILangEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;

public class ReplicaLangEntry implements ILangEntry {
    private final String key;

    public ReplicaLangEntry(String key) {
        this.key = key;
    }

    @Override
    public String getTranslationKey() {
        return key;
    }

    @Override
    public MutableComponent translate(Object... args) {
        return Component.translatable(key, args);
    }

    @Override
    public MutableComponent translate() {
        return Component.translatable(key);
    }

    @Override
    public MutableComponent translateColored(TextColor color, Object... args) {
        return translate(args).withStyle(style -> style.withColor(color));
    }

    @Override
    public MutableComponent translateColored(TextColor color) {
        return translate().withStyle(style -> style.withColor(color));
    }

    @Override
    public MutableComponent translateColored(EnumColor color) {
        return translate().withStyle(style -> style.withColor(color.getColor()));
    }

    @Override
    public MutableComponent translateColored(EnumColor color, Object... args) {
        return translate(args).withStyle(style -> style.withColor(color.getColor()));
    }
}
