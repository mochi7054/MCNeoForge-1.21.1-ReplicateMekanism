package com.github.mochi7054;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import com.github.mochi7054.item.ReplicaUpgradeItem;
import com.github.mochi7054.config.Config;
import com.github.mochi7054.chemical.RMChemical;


import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.resources.ResourceLocation;
import mekanism.api.Upgrade;


// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(ReplicateMekanism.MODID)
public class ReplicateMekanism {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "replicatemekanism";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "replicatemekanism" namespace
    public static final mekanism.common.registration.impl.BlockDeferredRegister BLOCKS = new mekanism.common.registration.impl.BlockDeferredRegister(MODID);
    // Create a Deferred Register to hold Items which will all be registered under the "replicatemekanism" namespace
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "replicatemekanism" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final mekanism.common.registration.impl.TileEntityTypeDeferredRegister BLOCK_ENTITIES = new mekanism.common.registration.impl.TileEntityTypeDeferredRegister(MODID);
    public static final mekanism.common.registration.impl.ContainerTypeDeferredRegister MENU_TYPES = new mekanism.common.registration.impl.ContainerTypeDeferredRegister(MODID);


    public static final DeferredRegister<net.neoforged.neoforge.fluids.FluidType> FLUID_TYPES = DeferredRegister.create(net.neoforged.neoforge.registries.NeoForgeRegistries.Keys.FLUID_TYPES, MODID);
    public static final DeferredRegister<net.minecraft.world.level.material.Fluid> FLUIDS = DeferredRegister.create(BuiltInRegistries.FLUID, MODID);

    public static class MatterFluidRegistry {
        public final DeferredHolder<net.neoforged.neoforge.fluids.FluidType, net.neoforged.neoforge.fluids.FluidType> type;
        public final DeferredHolder<net.minecraft.world.level.material.Fluid, net.minecraft.world.level.material.Fluid> source;

        public MatterFluidRegistry(String name, java.util.function.Supplier<com.buuz135.replication.api.IMatterType> matterTypeSupplier) {
            this.type = FLUID_TYPES.register(name, () -> new com.github.mochi7054.fluid.MatterFluidType(
                net.neoforged.neoforge.fluids.FluidType.Properties.create().descriptionId("fluid.replicatemekanism." + name),
                matterTypeSupplier
            ));
            this.source = FLUIDS.register(name, () -> new com.github.mochi7054.fluid.SimpleDummyFluid(type));
        }
    }

    public static final MatterFluidRegistry EARTH_MATTER = new MatterFluidRegistry("earth_matter", () -> com.buuz135.replication.ReplicationRegistry.Matter.EARTH.get());
    public static final MatterFluidRegistry NETHER_MATTER = new MatterFluidRegistry("nether_matter", () -> com.buuz135.replication.ReplicationRegistry.Matter.NETHER.get());
    public static final MatterFluidRegistry ORGANIC_MATTER = new MatterFluidRegistry("organic_matter", () -> com.buuz135.replication.ReplicationRegistry.Matter.ORGANIC.get());
    public static final MatterFluidRegistry ENDER_MATTER = new MatterFluidRegistry("ender_matter", () -> com.buuz135.replication.ReplicationRegistry.Matter.ENDER.get());
    public static final MatterFluidRegistry METALLIC_MATTER = new MatterFluidRegistry("metallic_matter", () -> com.buuz135.replication.ReplicationRegistry.Matter.METALLIC.get());
    public static final MatterFluidRegistry PRECIOUS_MATTER = new MatterFluidRegistry("precious_matter", () -> com.buuz135.replication.ReplicationRegistry.Matter.PRECIOUS.get());
    public static final MatterFluidRegistry LIVING_MATTER = new MatterFluidRegistry("living_matter", () -> com.buuz135.replication.ReplicationRegistry.Matter.LIVING.get());
    public static final MatterFluidRegistry QUANTUM_MATTER = new MatterFluidRegistry("quantum_matter", () -> com.buuz135.replication.ReplicationRegistry.Matter.QUANTUM.get());

    public static Upgrade REPLICA_UPGRADE_TYPE;

    // Creates a new food item with the id "replicatemekanism:example_id", nutrition 1 and saturation 2
    public static final DeferredItem<Item> REPLICA_ALLOY = ITEMS.registerSimpleItem("replica_alloy", new Item.Properties());
    public static final DeferredItem<Item> REPLICA_DUST = ITEMS.registerSimpleItem("replica_dust", new Item.Properties());
    public static final DeferredItem<Item> ENRICHED_REPLICA = ITEMS.registerSimpleItem("enriched_replica", new Item.Properties());
    public static final DeferredItem<Item> REPLICA_INCOMPLETE_CONTROL_CIRCUIT = ITEMS.registerSimpleItem("replica_incomplete_control_circuit", new Item.Properties());
    public static final DeferredItem<Item> REPLICA_CONTROL_CIRCUIT = ITEMS.registerSimpleItem("replica_control_circuit", new Item.Properties());
    public static final DeferredItem<Item> REPLICA_UPGRADE = ITEMS.register("replica_upgrade", () -> new ReplicaUpgradeItem(new Item.Properties()));

    public static final mekanism.common.registration.impl.BlockRegistryObject<com.github.mochi7054.block.ImaginatorBlock, com.github.mochi7054.item.ImaginatorBlockItem> IMAGINATOR =
            BLOCKS.register("imaginator",
                    () -> new com.github.mochi7054.block.ImaginatorBlock(BlockBehaviour.Properties.of().strength(3.5F)),
                    com.github.mochi7054.item.ImaginatorBlockItem::new);

    public static final mekanism.common.registration.impl.TileEntityTypeRegistryObject<com.github.mochi7054.block.entity.ImaginatorBlockEntity> IMAGINATOR_TILE = BLOCK_ENTITIES.mekBuilder(IMAGINATOR, com.github.mochi7054.block.entity.ImaginatorBlockEntity::new)
            .clientTicker(mekanism.common.tile.base.TileEntityMekanism::tickClient)
            .serverTicker(mekanism.common.tile.base.TileEntityMekanism::tickServer)
            .build();

    public static final mekanism.common.registration.impl.ContainerTypeRegistryObject<com.github.mochi7054.inventory.container.ImaginatorMenu> IMAGINATOR_CONTAINER_TYPE =
            MENU_TYPES.register("imaginator",
                    com.github.mochi7054.block.entity.ImaginatorBlockEntity.class,
                    com.github.mochi7054.inventory.container.ImaginatorMenu::new);

    public static final mekanism.common.registration.impl.BlockRegistryObject<com.github.mochi7054.block.CollapserBlock, com.github.mochi7054.item.CollapserBlockItem> COLLAPSER =
            BLOCKS.register("collapser",
                    () -> new com.github.mochi7054.block.CollapserBlock(BlockBehaviour.Properties.of().strength(3.5F)),
                    com.github.mochi7054.item.CollapserBlockItem::new);

    public static final mekanism.common.registration.impl.TileEntityTypeRegistryObject<com.github.mochi7054.block.entity.CollapserBlockEntity> COLLAPSER_TILE = BLOCK_ENTITIES.mekBuilder(COLLAPSER, com.github.mochi7054.block.entity.CollapserBlockEntity::new)
            .clientTicker(mekanism.common.tile.base.TileEntityMekanism::tickClient)
            .serverTicker(mekanism.common.tile.base.TileEntityMekanism::tickServer)
            .build();

    public static final mekanism.common.registration.impl.ContainerTypeRegistryObject<com.github.mochi7054.inventory.container.CollapserMenu> COLLAPSER_CONTAINER_TYPE =
            MENU_TYPES.register("collapser",
                    com.github.mochi7054.block.entity.CollapserBlockEntity.class,
                    com.github.mochi7054.inventory.container.CollapserMenu::new);

    // Creates a creative tab with the id "replicatemekanism:example_tab" for the example item, that is placed after the creative tab
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("replicatemekanism", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.replicatemekanism")) //The language key for the title of your CreativeModeTab
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> REPLICA_ALLOY.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(REPLICA_ALLOY.get());
                output.accept(REPLICA_DUST.get());
                output.accept(ENRICHED_REPLICA.get());
                output.accept(REPLICA_INCOMPLETE_CONTROL_CIRCUIT.get());
                output.accept(REPLICA_CONTROL_CIRCUIT.get());
                output.accept(REPLICA_UPGRADE.get());// Add the example item to the tab. For your own tabs, this method is preferred over the event
                output.accept(IMAGINATOR.asItem());
                output.accept(COLLAPSER.asItem());
            }).build());


    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.

    public ReplicateMekanism(IEventBus modEventBus, ModContainer modContainer) {


        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(net.neoforged.bus.api.EventPriority.HIGHEST, this::registerCapabilities);

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);
        FLUID_TYPES.register(modEventBus);
        FLUIDS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modEventBus);
        MENU_TYPES.register(modEventBus);
        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (ReplicateMekanism) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        // NeoForge.EVENT_BUS.register(this);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        RMChemical.CHEMICALS.register(modEventBus);
    }


    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("ReplicateMekanism common setup completed.");
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {

        net.neoforged.neoforge.capabilities.IBlockCapabilityProvider<net.neoforged.neoforge.fluids.capability.IFluidHandler, net.minecraft.core.Direction> blockFluidProvider = (level, pos, state, be, side) -> {
            if (be == null) return null;
            var matterHandler = level.getCapability(
                    com.buuz135.replication.ReplicationRegistry.Capabilities.MATTER_HANDLER,
                    pos,
                    state,
                    be,
                    side
            );
            if (matterHandler == null) {
                matterHandler = level.getCapability(
                        com.buuz135.replication.ReplicationRegistry.Capabilities.MATTER_HANDLER,
                        pos,
                        state,
                        be,
                        null
                );
            }
            if (matterHandler == null) {
                if (be instanceof com.buuz135.replication.api.network.IMatterTanksSupplier supplier) {
                    var tanks = supplier.getTanks();
                    if (tanks != null && !tanks.isEmpty()) {
                        @SuppressWarnings("unchecked")
                        java.util.List<com.buuz135.replication.api.matter_fluid.IMatterTank> typedTanks = (java.util.List<com.buuz135.replication.api.matter_fluid.IMatterTank>) (Object) tanks;
                        matterHandler = new com.github.mochi7054.fluid.MatterTanksWrapper(typedTanks);
                    }
                }
            }
            if (matterHandler == null) {
                return null;
            }
            return new com.github.mochi7054.fluid.MatterFluidWrapper(matterHandler);
        };

        try {


            event.registerBlock(
                    net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK,
                    blockFluidProvider,
                    com.buuz135.replication.ReplicationRegistry.Blocks.MATTER_TANK.getBlock(),
                    com.buuz135.replication.ReplicationRegistry.Blocks.CREATIVE_MATTER_TANK.getBlock(),
                    com.buuz135.replication.ReplicationRegistry.Blocks.REPLICATOR.getBlock(),
                    com.buuz135.replication.ReplicationRegistry.Blocks.DISINTEGRATOR.getBlock()
            );

            var matterTankType = getReplicationBEType("matter_tank");
            if (matterTankType != null) {
                event.registerBlockEntity(
                        net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK,
                        matterTankType,
                        (be, side) -> {
                            return blockFluidProvider.getCapability(be.getLevel(), be.getBlockPos(), be.getBlockState(), be, side);
                        }
                );
            }
            var creativeMatterTankType = getReplicationBEType("creative_matter_tank");
            if (creativeMatterTankType != null) {
                event.registerBlockEntity(
                        net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK,
                        creativeMatterTankType,
                        (be, side) -> {
                            return blockFluidProvider.getCapability(be.getLevel(), be.getBlockPos(), be.getBlockState(), be, side);
                        }
                );
            }
            var disintegratorType = getReplicationBEType("disintegrator");
            if (disintegratorType != null) {
                event.registerBlockEntity(
                        net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK,
                        disintegratorType,
                        (be, side) -> {
                            return blockFluidProvider.getCapability(be.getLevel(), be.getBlockPos(), be.getBlockState(), be, side);
                        }
                );
            }
            var replicatorType = getReplicationBEType("replicator");
            if (replicatorType != null) {
                event.registerBlockEntity(
                        net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK,
                        replicatorType,
                        (be, side) -> {
                            return blockFluidProvider.getCapability(be.getLevel(), be.getBlockPos(), be.getBlockState(), be, side);
                        }
                );
            }

            event.registerBlockEntity(
                    com.buuz135.replication.ReplicationRegistry.Capabilities.MATTER_HANDLER,
                    ReplicateMekanism.IMAGINATOR_TILE.get(),
                    (be, side) -> {
                        return new com.github.mochi7054.fluid.ImaginatorMatterHandler(be);
                    }
            );
        } catch (Exception e) {
            LOGGER.error("Failed to register FluidHandler wrapper capabilities for Replication Blocks/BlockEntities", e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends net.minecraft.world.level.block.entity.BlockEntity> net.minecraft.world.level.block.entity.BlockEntityType<T> getReplicationBEType(String name) {
        var rl = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("replication", name);
        var type = net.minecraft.core.registries.BuiltInRegistries.BLOCK_ENTITY_TYPE.get(rl);
        return (net.minecraft.world.level.block.entity.BlockEntityType<T>) type;
    }

    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
