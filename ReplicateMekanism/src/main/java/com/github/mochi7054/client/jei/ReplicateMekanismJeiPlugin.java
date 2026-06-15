package com.github.mochi7054.client.jei;

import com.github.mochi7054.ReplicateMekanism;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IIngredientAliasRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import java.util.List;

@JeiPlugin
public class ReplicateMekanismJeiPlugin implements IModPlugin {

    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(ReplicateMekanism.MODID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerIngredientAliases(IIngredientAliasRegistration registration) {
        // 1. Imaginator aliases
        List<ItemStack> imaginators = List.of(
            new ItemStack(ReplicateMekanism.IMAGINATOR.asItem()),
            new ItemStack(ReplicateMekanism.IMAGINATOR_BASIC.asItem()),
            new ItemStack(ReplicateMekanism.IMAGINATOR_ADVANCED.asItem()),
            new ItemStack(ReplicateMekanism.IMAGINATOR_ELITE.asItem()),
            new ItemStack(ReplicateMekanism.IMAGINATOR_ULTIMATE.asItem())
        );
        List<String> imaginatorAliases = List.of(
            Component.translatable("alias.replicatemekanism.imaginator.replicator").getString(),
            Component.translatable("alias.replicatemekanism.imaginator.solidifier").getString(),
            Component.translatable("alias.replicatemekanism.imaginator.materializer").getString()
        );
        registration.addAliases(VanillaTypes.ITEM_STACK, imaginators, imaginatorAliases);

        // 2. Collapser aliases
        List<ItemStack> collapsers = List.of(
            new ItemStack(ReplicateMekanism.COLLAPSER.asItem()),
            new ItemStack(ReplicateMekanism.COLLAPSER_BASIC.asItem()),
            new ItemStack(ReplicateMekanism.COLLAPSER_ADVANCED.asItem()),
            new ItemStack(ReplicateMekanism.COLLAPSER_ELITE.asItem()),
            new ItemStack(ReplicateMekanism.COLLAPSER_ULTIMATE.asItem())
        );
        List<String> collapserAliases = List.of(
            Component.translatable("alias.replicatemekanism.collapser.disintegrator").getString(),
            Component.translatable("alias.replicatemekanism.collapser.crusher").getString(),
            Component.translatable("alias.replicatemekanism.collapser.destroyer").getString()
        );
        registration.addAliases(VanillaTypes.ITEM_STACK, collapsers, collapserAliases);

        // 3. Tier Installer aliases
        List<ItemStack> installers = List.of(new ItemStack(ReplicateMekanism.REPLICA_TIER_INSTALLER.get()));
        List<String> installerAliases = List.of(
            Component.translatable("alias.replicatemekanism.installer.factory").getString(),
            Component.translatable("alias.replicatemekanism.installer.upgrade").getString()
        );
        registration.addAliases(VanillaTypes.ITEM_STACK, installers, installerAliases);

        // 4. Upgrade aliases
        List<ItemStack> upgrades = List.of(new ItemStack(ReplicateMekanism.REPLICA_UPGRADE.get()));
        List<String> upgradeAliases = List.of(
            Component.translatable("alias.replicatemekanism.upgrade.augment").getString()
        );
        registration.addAliases(VanillaTypes.ITEM_STACK, upgrades, upgradeAliases);

        // 5. Material aliases
        List<ItemStack> materials = List.of(
            new ItemStack(ReplicateMekanism.REPLICA_ALLOY.get()),
            new ItemStack(ReplicateMekanism.REPLICA_DUST.get()),
            new ItemStack(ReplicateMekanism.ENRICHED_REPLICA.get()),
            new ItemStack(ReplicateMekanism.REPLICA_CONTROL_CIRCUIT.get()),
            new ItemStack(ReplicateMekanism.REPLICA_INCOMPLETE_CONTROL_CIRCUIT.get())
        );
        List<String> materialAliases = List.of(
            Component.translatable("alias.replicatemekanism.material.replica").getString()
        );
        registration.addAliases(VanillaTypes.ITEM_STACK, materials, materialAliases);
    }
}
