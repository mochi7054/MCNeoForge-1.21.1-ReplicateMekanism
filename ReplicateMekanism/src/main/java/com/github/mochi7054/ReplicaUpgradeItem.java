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
                tooltip.add(Component.translatable("upgrade.replicatemekanism.replica.desc").withStyle(ChatFormatting.WHITE));
                // Add max installed: 1
                tooltip.add(Component.translatable("upgrade.mekanism.max_installed", 1).withStyle(ChatFormatting.WHITE));
            } else {
                // Add Hold Left Shift for details.
                Component holdShift = Component.literal("Hold ")
                    .withStyle(ChatFormatting.WHITE)
                        .append(Component.literal("Left Shift").withStyle(style -> style.withColor(0x559eff)))
                    .append(Component.literal(" for details.").withStyle(ChatFormatting.WHITE));
                tooltip.add(holdShift);
            }
        }
    }
}
