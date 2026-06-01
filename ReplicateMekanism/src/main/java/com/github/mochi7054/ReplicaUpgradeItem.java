package com.github.mochi7054;

import java.util.List;
import mekanism.common.item.ItemUpgrade;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class ReplicaUpgradeItem extends ItemUpgrade {
    public ReplicaUpgradeItem(Properties properties) {
        super(ReplicateMekanism.REPLICA_UPGRADE_TYPE, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient()) {
            if (ReplicaClientUtils.isShiftDown()) {
                // Add description: Doubles output of machinery.
                tooltip.add(Component.translatable("upgrade.replicatemekanism.replica.desc").withStyle(ChatFormatting.GRAY));
                // Add max installed: 1
                tooltip.add(Component.translatable("upgrade.mekanism.max_installed", 1).withStyle(ChatFormatting.GRAY));
            } else {
                // Add Hold Left Shift for details.
                Component holdShift = Component.literal("Hold ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal("Left Shift").withStyle(net.minecraft.ChatFormatting.BLUE))
                    .append(Component.literal(" for details.").withStyle(ChatFormatting.GRAY));
                tooltip.add(holdShift);
            }
        }
    }
}
