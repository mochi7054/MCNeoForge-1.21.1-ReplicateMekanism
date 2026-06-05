package com.github.mochi7054.item;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import mekanism.common.item.block.ItemBlockTooltip;
import com.github.mochi7054.block.ImaginatorBlock;
import mekanism.api.security.IItemSecurityUtils;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.Attributes.AttributeInventory;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.MekanismLang;
import mekanism.api.text.EnumColor;
import mekanism.common.util.text.BooleanStateDisplay;

public class ImaginatorBlockItem extends ItemBlockTooltip<ImaginatorBlock> {

    public ImaginatorBlockItem(ImaginatorBlock block, Properties properties) {
        super(block, true, properties); // hasDetails = true
    }

    @Override
    protected void addDetails(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        // Add security tooltip (Owner, Security settings)
        IItemSecurityUtils.INSTANCE.addSecurityTooltip(stack, tooltip);
        
        // Add type details (Energy stored)
        addTypeDetails(stack, context, tooltip, flag);
        
        // Skip fluid retrieval from attachment to prevent IllegalArgumentException (No known containers)
        
        // Add inventory status if block supports it
        if (Attribute.has(getBlock(), AttributeInventory.class) && ContainerType.ITEM.supports(stack)) {
            tooltip.add(MekanismLang.HAS_INVENTORY.translateColored(EnumColor.AQUA, EnumColor.GRAY, BooleanStateDisplay.YesNo.hasInventory(stack)));
        }
    }
}
