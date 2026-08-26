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
        return 64;
    }

    @Override
    public net.minecraft.world.InteractionResult useOn(net.minecraft.world.item.context.UseOnContext context) {
        var result = super.useOn(context);
        if (result.consumesAction() && context.getPlayer() instanceof net.minecraft.server.level.ServerPlayer player) {
            ReplicateMekanism.checkAndAwardCheatedAdvancement(player);
        }
        return result;
    }

    @Override
    public MutableComponent getName(ItemStack stack) {
        return super.getName(stack).copy().withStyle(style -> style.withColor(nameColor));
    }
}
