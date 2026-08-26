package com.github.mochi7054.forensic;

import com.github.mochi7054.ReplicateMekanism;
import mekanism.api.text.ILangEntry;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Supplier;

public class ForensicChamberBlock extends BlockTile<ForensicChamberBlockEntity, BlockTypeTile<ForensicChamberBlockEntity>> implements mekanism.common.block.interfaces.IHasDescription {

    public ForensicChamberBlock(BlockBehaviour.Properties properties,
                                Supplier<TileEntityTypeRegistryObject<ForensicChamberBlockEntity>> tileSupplier,
                                Supplier<ContainerTypeRegistryObject<ForensicChamberMenu>> containerSupplier) {
        super(createBlockType(tileSupplier, containerSupplier), properties);
    }

    private static BlockTypeTile<ForensicChamberBlockEntity> createBlockType(
            Supplier<TileEntityTypeRegistryObject<ForensicChamberBlockEntity>> tileSupplier,
            Supplier<ContainerTypeRegistryObject<ForensicChamberMenu>> containerSupplier) {

        BlockTypeTile<ForensicChamberBlockEntity> blockType = new BlockTypeTile<>(
                tileSupplier,
                new ILangEntry() {
                    @Override
                    public String getTranslationKey() {
                        return "container.replicatemekanism.forensic_chamber";
                    }
                }
        );

        blockType.add(
                new mekanism.common.block.attribute.AttributeEnergy(() -> 50L, () -> 40_000L),
                mekanism.common.block.attribute.AttributeSideConfig.create(TransmissionType.ITEM, TransmissionType.ENERGY),
                mekanism.common.block.attribute.Attributes.ACTIVE,
                mekanism.common.block.attribute.Attributes.REDSTONE,
                mekanism.common.block.attribute.Attributes.SECURITY,
                new mekanism.common.block.attribute.AttributeStateFacing(),
                new mekanism.common.block.attribute.AttributeGui(containerSupplier::get, new ILangEntry() {
                    @Override
                    public String getTranslationKey() {
                        return "container.replicatemekanism.forensic_chamber";
                    }
                })
        );

        return blockType;
    }

    @Override
    public ILangEntry getDescription() {
        return new ILangEntry() {
            @Override
            public String getTranslationKey() {
                return "description.replicatemekanism.forensic_chamber";
            }
        };
    }
}