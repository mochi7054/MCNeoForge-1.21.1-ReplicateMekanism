package com.github.mochi7054.block;

import com.github.mochi7054.block.entity.CollapserBlockEntity;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.content.blocktype.BlockTypeTile;
import net.minecraft.world.level.block.state.BlockBehaviour;

import mekanism.common.lib.transmitter.TransmissionType;

public class CollapserBlock extends BlockTile<CollapserBlockEntity, BlockTypeTile<CollapserBlockEntity>> implements mekanism.common.block.interfaces.IHasDescription {

    private static final BlockTypeTile<CollapserBlockEntity> BLOCK_TYPE;

    static {
        BLOCK_TYPE = new BlockTypeTile<>(
                () -> com.github.mochi7054.ReplicateMekanism.COLLAPSER_TILE,
                new mekanism.api.text.ILangEntry() {
                    @Override
                    public String getTranslationKey() {
                        return "container.replicatemekanism.collapser";
                    }
                }
        );
        BLOCK_TYPE.add(
                new mekanism.common.block.attribute.AttributeEnergy(() -> 50L, () -> 100_000L),
                mekanism.common.block.attribute.AttributeUpgradeSupport.SPEED_ENERGY,
                mekanism.common.block.attribute.AttributeSideConfig.create(TransmissionType.ITEM, TransmissionType.ENERGY, TransmissionType.FLUID),
                mekanism.common.block.attribute.Attributes.ACTIVE,
                mekanism.common.block.attribute.Attributes.REDSTONE,
                mekanism.common.block.attribute.Attributes.SECURITY,
                new mekanism.common.block.attribute.AttributeStateFacing(),
                new mekanism.common.block.attribute.AttributeGui(() -> com.github.mochi7054.ReplicateMekanism.COLLAPSER_CONTAINER_TYPE, new mekanism.api.text.ILangEntry() {
                    @Override
                    public String getTranslationKey() {
                        return "container.replicatemekanism.collapser";
                    }
                })
        );
    }

    public CollapserBlock(BlockBehaviour.Properties properties) {
        super(BLOCK_TYPE, properties);
    }

    @Override
    public mekanism.api.text.ILangEntry getDescription() {
        return new mekanism.api.text.ILangEntry() {
            @Override
            public String getTranslationKey() {
                return "description.replicatemekanism.collapser";
            }
        };
    }
}
