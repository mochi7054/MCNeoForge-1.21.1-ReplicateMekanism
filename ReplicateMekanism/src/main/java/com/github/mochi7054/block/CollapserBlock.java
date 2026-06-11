package com.github.mochi7054.block;

import com.github.mochi7054.block.entity.CollapserBlockEntity;
import com.github.mochi7054.inventory.container.CollapserMenu;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.registration.impl.BlockRegistryObject;
import net.minecraft.world.level.block.state.BlockBehaviour;
import mekanism.common.lib.transmitter.TransmissionType;
import java.util.function.Supplier;

public class CollapserBlock extends BlockTile<CollapserBlockEntity, BlockTypeTile<CollapserBlockEntity>> implements mekanism.common.block.interfaces.IHasDescription {

    private final ReplicaTier tier;

    public CollapserBlock(BlockBehaviour.Properties properties, ReplicaTier tier,
                          Supplier<TileEntityTypeRegistryObject<CollapserBlockEntity>> tileSupplier,
                          Supplier<ContainerTypeRegistryObject<CollapserMenu>> containerSupplier,
                          Supplier<BlockRegistryObject<?, ?>> nextTierBlockSupplier) {
        super(createBlockType(tier, tileSupplier, containerSupplier, nextTierBlockSupplier), properties);
        this.tier = tier;
    }

    public ReplicaTier getTier() {
        return tier;
    }

    private static BlockTypeTile<CollapserBlockEntity> createBlockType(
            ReplicaTier tier,
            Supplier<TileEntityTypeRegistryObject<CollapserBlockEntity>> tileSupplier,
            Supplier<ContainerTypeRegistryObject<CollapserMenu>> containerSupplier,
            Supplier<BlockRegistryObject<?, ?>> nextTierBlockSupplier) {
        
        BlockTypeTile<CollapserBlockEntity> blockType = new BlockTypeTile<>(
            tileSupplier,
            new mekanism.api.text.ILangEntry() {
                @Override
                public String getTranslationKey() {
                    return "container.replicatemekanism.collapser_" + tier.getName();
                }
            }
        );

        blockType.add(
            new mekanism.common.block.attribute.AttributeEnergy(() -> 50L, tier::getEnergyCapacity),
            mekanism.common.block.attribute.AttributeUpgradeSupport.SPEED_ENERGY,
            mekanism.common.block.attribute.AttributeSideConfig.create(TransmissionType.ITEM, TransmissionType.ENERGY, TransmissionType.FLUID),
            mekanism.common.block.attribute.Attributes.ACTIVE,
            mekanism.common.block.attribute.Attributes.REDSTONE,
            mekanism.common.block.attribute.Attributes.SECURITY,
            new mekanism.common.block.attribute.AttributeStateFacing(),
            new mekanism.common.block.attribute.AttributeGui(containerSupplier::get, new mekanism.api.text.ILangEntry() {
                @Override
                public String getTranslationKey() {
                    return "container.replicatemekanism.collapser_" + tier.getName();
                }
            })
        );

        if (tier.getBaseTier() != null) {
            blockType.add(new mekanism.common.block.attribute.AttributeTier<>(tier));
        }

        if (nextTierBlockSupplier != null) {
            blockType.add(new mekanism.common.block.attribute.AttributeUpgradeable(nextTierBlockSupplier));
        }

        return blockType;
    }

    @Override
    public mekanism.api.text.ILangEntry getDescription() {
        return new mekanism.api.text.ILangEntry() {
            @Override
            public String getTranslationKey() {
                return "description.replicatemekanism.collapser_" + tier.getName();
            }
        };
    }
}
