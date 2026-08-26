package com.github.mochi7054.item;

import mekanism.common.item.ItemUpgrade;
import mekanism.api.Upgrade;
import com.github.mochi7054.ReplicateMekanism;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.ItemStack;

public class ReplicaUpgradeItem extends ItemUpgrade {
    private final TextColor nameColor;

    public ReplicaUpgradeItem(Properties properties, TextColor nameColor) {
        super(getUpgradeType(), properties);
        this.nameColor = nameColor;
    }

    private static Upgrade getUpgradeType() {
        // Force class loading of Upgrade so that its static initializer (and our mixin) runs
        Upgrade dummy = Upgrade.SPEED;
        return ReplicateMekanism.REPLICA_UPGRADE_TYPE;
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return com.github.mochi7054.config.Config.getReplicaUpgradeMaxStack();
    }

    @Override
    public MutableComponent getName(ItemStack stack) {
        return super.getName(stack).copy().withStyle(style -> style.withColor(nameColor));
    }
}
