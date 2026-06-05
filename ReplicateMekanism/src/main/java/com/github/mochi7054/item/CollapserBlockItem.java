package com.github.mochi7054.item;

import com.github.mochi7054.block.CollapserBlock;
import mekanism.api.security.IItemSecurityUtils;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.Attributes.AttributeInventory;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.common.util.text.BooleanStateDisplay;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class CollapserBlockItem extends ItemBlockTooltip<CollapserBlock> {

    public CollapserBlockItem(CollapserBlock block, Properties properties) {
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
