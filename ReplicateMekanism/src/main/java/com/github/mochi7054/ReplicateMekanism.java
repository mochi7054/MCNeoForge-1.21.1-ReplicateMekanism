package com.github.mochi7054;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
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
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    // Create a Deferred Register to hold Items which will all be registered under the "replicatemekanism" namespace
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    // Create a Deferred Register to hold Items registered under the "mekanism" namespace
    public static final DeferredRegister.Items MEKANISM_ITEMS = DeferredRegister.createItems("mekanism");
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "replicatemekanism" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final Upgrade REPLICA_UPGRADE_TYPE = EnumExtender.extendUpgrade();

    // Creates a new food item with the id "replicatemekanism:example_id", nutrition 1 and saturation 2
    public static final DeferredItem<Item> REPLICA_ALLOY = ITEMS.registerSimpleItem("replica_alloy", new Item.Properties());
    public static final DeferredItem<Item> REPLICA_DUST = ITEMS.registerSimpleItem("replica_dust", new Item.Properties());
    public static final DeferredItem<Item> ENRICHED_REPLICA = ITEMS.registerSimpleItem("enriched_replica", new Item.Properties());
    public static final DeferredItem<Item> REPLICA_INCOMPLETE_CONTROL_CIRCUIT = ITEMS.registerSimpleItem("replica_incomplete_control_circuit", new Item.Properties());
    public static final DeferredItem<Item> REPLICA_CONTROL_CIRCUIT = ITEMS.registerSimpleItem("replica_control_circuit", new Item.Properties());
    public static final DeferredItem<Item> REPLICA_UPGRADE = MEKANISM_ITEMS.register("replica_upgrade", () -> new ReplicaUpgradeItem(new Item.Properties()));
    public static final DeferredItem<Item> REPLICA_GUIDE = ITEMS.registerSimpleItem("replica_guide", new Item.Properties());

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
                output.accept(REPLICA_UPGRADE.get());
                output.accept(REPLICA_GUIDE.get());// Add the example item to the tab. For your own tabs, this method is preferred over the event
            }).build());

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public ReplicateMekanism(IEventBus modEventBus, ModContainer modContainer) {


        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);
        MEKANISM_ITEMS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (ReplicateMekanism) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        RMChemical.CHEMICALS.register(modEventBus);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("ReplicateMekanism common setup completed.");
    }

    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}

