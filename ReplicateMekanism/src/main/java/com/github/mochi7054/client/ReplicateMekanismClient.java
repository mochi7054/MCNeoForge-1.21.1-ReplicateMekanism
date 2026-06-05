package com.github.mochi7054.client;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import com.github.mochi7054.ReplicateMekanism;
import net.neoforged.bus.api.IEventBus;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = ReplicateMekanism.MODID, dist = Dist.CLIENT)
public class ReplicateMekanismClient {
    public ReplicateMekanismClient(ModContainer container, IEventBus modEventBus) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        
        // Manually register to Mod Event Bus
        modEventBus.register(ReplicateMekanismClient.class);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // Some client setup code
        ReplicateMekanism.LOGGER.info("HELLO FROM CLIENT SETUP");
        ReplicateMekanism.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }

    @SubscribeEvent
    static void registerScreens(RegisterMenuScreensEvent event) {
        mekanism.client.ClientRegistrationUtil.registerScreen(event, ReplicateMekanism.IMAGINATOR_CONTAINER_TYPE, com.github.mochi7054.client.gui.ImaginatorScreen::new);
    }
}

