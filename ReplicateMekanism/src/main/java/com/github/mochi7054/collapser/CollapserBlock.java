package com.github.mochi7054.collapser;

import com.github.mochi7054.block.ReplicaTier;

import com.github.mochi7054.collapser.CollapserBlockEntity;
import com.github.mochi7054.collapser.CollapserMenu;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.registration.impl.BlockRegistryObject;
import net.minecraft.network.chat.MutableComponent;
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
    public MutableComponent getName() {
        return super.getName().withStyle(style -> style.withColor(tier.getTextColor()));
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

    @Override
    public void animateTick(net.minecraft.world.level.block.state.BlockState state, net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos, net.minecraft.util.RandomSource random) {
        net.minecraft.world.level.block.entity.BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof CollapserBlockEntity collapser && collapser.getActive()) {
            for (int i = 0; i < 4; i++) {
                double x = pos.getX() + 0.2 + random.nextDouble() * 0.6;
                double y = pos.getY() + 0.4 + random.nextDouble() * 0.5;
                double z = pos.getZ() + 0.2 + random.nextDouble() * 0.6;
                float r = random.nextFloat();
                float g = random.nextFloat();
                float b = random.nextFloat();
                net.minecraft.core.particles.DustParticleOptions dust = new net.minecraft.core.particles.DustParticleOptions(new org.joml.Vector3f(r, g, b), 1.0F);
                level.addParticle(dust, x, y, z, 0.0D, 0.01D, 0.0D);
            }
        }
    }
}
