package com.github.mochi7054.item;

import mekanism.common.item.ItemUpgrade;
import com.github.mochi7054.ReplicateMekanism;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.ItemStack;

public class ReplicaUpgradeItem extends ItemUpgrade {
    private final TextColor nameColor;

    public ReplicaUpgradeItem(Properties properties, TextColor nameColor) {
        super(ReplicateMekanism.REPLICA_UPGRADE_TYPE, properties);
        this.nameColor = nameColor;
    }

    @Override
    public MutableComponent getName(ItemStack stack) {
        return super.getName(stack).copy().withStyle(style -> style.withColor(nameColor));
    }
}
