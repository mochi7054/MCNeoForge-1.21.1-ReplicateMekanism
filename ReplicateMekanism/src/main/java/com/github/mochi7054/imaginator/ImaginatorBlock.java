package com.github.mochi7054.imaginator;

import com.github.mochi7054.block.ReplicaTier;

import com.github.mochi7054.imaginator.ImaginatorBlockEntity;
import com.github.mochi7054.imaginator.ImaginatorMenu;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.registration.impl.BlockRegistryObject;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.state.BlockBehaviour;
import mekanism.common.lib.transmitter.TransmissionType;
import java.util.function.Supplier;

public class ImaginatorBlock extends BlockTile<ImaginatorBlockEntity, BlockTypeTile<ImaginatorBlockEntity>> implements mekanism.common.block.interfaces.IHasDescription {

    private final ReplicaTier tier;

    public ImaginatorBlock(BlockBehaviour.Properties properties, ReplicaTier tier,
                           Supplier<TileEntityTypeRegistryObject<ImaginatorBlockEntity>> tileSupplier,
                           Supplier<ContainerTypeRegistryObject<ImaginatorMenu>> containerSupplier,
                           Supplier<BlockRegistryObject<?, ?>> nextTierBlockSupplier) {
        super(createBlockType(tier, tileSupplier, containerSupplier, nextTierBlockSupplier), properties);
        this.tier = tier;
    }

    public ReplicaTier getTier() {
        return tier;
    }

    private static BlockTypeTile<ImaginatorBlockEntity> createBlockType(
            ReplicaTier tier,
            Supplier<TileEntityTypeRegistryObject<ImaginatorBlockEntity>> tileSupplier,
            Supplier<ContainerTypeRegistryObject<ImaginatorMenu>> containerSupplier,
            Supplier<BlockRegistryObject<?, ?>> nextTierBlockSupplier) {
        
        BlockTypeTile<ImaginatorBlockEntity> blockType = new BlockTypeTile<>(
            tileSupplier,
            new mekanism.api.text.ILangEntry() {
                @Override
                public String getTranslationKey() {
                    return "container.replicatemekanism.imaginator_" + tier.getName();
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
                    return "container.replicatemekanism.imaginator_" + tier.getName();
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
                return "description.replicatemekanism.imaginator_" + tier.getName();
            }
        };
    }

    @Override
    public void animateTick(net.minecraft.world.level.block.state.BlockState state, net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos, net.minecraft.util.RandomSource random) {
        net.minecraft.world.level.block.entity.BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ImaginatorBlockEntity imaginator && imaginator.getActive()) {
            for (int i = 0; i < 4; i++) {
                // Emit from the top surface of the block, like rising smoke
                double x = pos.getX() + 0.2 + random.nextDouble() * 0.6;
                double y = pos.getY() + 0.95 + random.nextDouble() * 0.15;
                double z = pos.getZ() + 0.2 + random.nextDouble() * 0.6;
                float r = random.nextFloat();
                float g = random.nextFloat();
                float b = random.nextFloat();
                // Slight upward velocity and gentle horizontal drift for smoke effect
                double vx = (random.nextDouble() - 0.5) * 0.03;
                double vy = 0.04 + random.nextDouble() * 0.04;
                double vz = (random.nextDouble() - 0.5) * 0.03;
                net.minecraft.core.particles.DustParticleOptions dust = new net.minecraft.core.particles.DustParticleOptions(new org.joml.Vector3f(r, g, b), 1.0F);
                level.addParticle(dust, x, y, z, vx, vy, vz);
            }
        }
    }
}
