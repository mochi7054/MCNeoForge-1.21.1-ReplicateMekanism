package com.github.mochi7054.item;

import com.github.mochi7054.ReplicateMekanism;
import com.buuz135.replication.block.ReplicatorBlock;
import com.buuz135.replication.block.DisintegratorBlock;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.minecraft.world.Containers;

import java.util.ArrayList;
import java.util.List;

public class ReplicaTierInstallerItem extends Item {

    private final TextColor nameColor;

    public ReplicaTierInstallerItem(Properties properties, TextColor nameColor) {
        super(properties);
        this.nameColor = nameColor;
    }

    @Override
    public MutableComponent getName(ItemStack stack) {
        return super.getName(stack).copy().withStyle(style -> style.withColor(nameColor));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Player player = context.getPlayer();

        boolean isReplicator = state.getBlock() instanceof ReplicatorBlock;
        boolean isDisintegrator = state.getBlock() instanceof DisintegratorBlock;

        if (!isReplicator && !isDisintegrator) {
            return InteractionResult.PASS;
        }

        // Security check using Mekanism security utils
        if (player != null) {
            if (!mekanism.api.security.IBlockSecurityUtils.INSTANCE.canAccessOrDisplayError(player, level, pos)) {
                return InteractionResult.FAIL;
            }
            if (player instanceof net.minecraft.server.level.ServerPlayer) {
                if (!level.mayInteract(player, pos)) {
                    return InteractionResult.FAIL;
                }
            }
        }

        // 1. Gather capabilities from the old BlockEntity
        BlockEntity oldBe = level.getBlockEntity(pos);
        long storedEnergy = 0;
        List<ItemStack> storedItems = new ArrayList<>();
        List<FluidStack> storedFluids = new ArrayList<>();

        if (oldBe != null) {
            // Energy
            var energyCap = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos, state, oldBe, null);
            if (energyCap != null) {
                storedEnergy = energyCap.getEnergyStored();
            }

            // Items
            var itemCap = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, state, oldBe, null);
            if (itemCap != null) {
                for (int i = 0; i < itemCap.getSlots(); i++) {
                    ItemStack stack = itemCap.getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        storedItems.add(stack.copy());
                    }
                }
            }

            // Fluids
            var fluidCap = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, state, oldBe, null);
            if (fluidCap != null) {
                for (int i = 0; i < fluidCap.getTanks(); i++) {
                    FluidStack stack = fluidCap.getFluidInTank(i);
                    if (!stack.isEmpty()) {
                        storedFluids.add(stack.copy());
                    }
                }
            }
        }

        // 2. Keep facing direction
        Direction facing = Direction.NORTH;
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        }

        // 3. Determine and place new BlockState
        BlockState newState;
        if (isReplicator) {
            newState = ReplicateMekanism.IMAGINATOR.get().defaultBlockState();
        } else {
            newState = ReplicateMekanism.COLLAPSER.get().defaultBlockState();
        }

        if (newState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            newState = newState.setValue(BlockStateProperties.HORIZONTAL_FACING, facing);
        }

        // Replace block in the world
        level.setBlockAndUpdate(pos, newState);

        // 4. Inject capabilities to the new BlockEntity
        BlockEntity newBe = level.getBlockEntity(pos);
        if (newBe != null) {
            // Energy
            if (storedEnergy > 0) {
                var energyCap = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos, newState, newBe, null);
                if (energyCap != null && energyCap.canReceive()) {
                    energyCap.receiveEnergy((int) Math.min(Integer.MAX_VALUE, storedEnergy), false);
                }
            }

            // Items
            var itemCap = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, newState, newBe, null);
            for (ItemStack stack : storedItems) {
                ItemStack remaining = stack;
                if (itemCap != null) {
                    remaining = ItemHandlerHelper.insertItem(itemCap, stack, false);
                }
                if (!remaining.isEmpty()) {
                    Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, remaining);
                }
            }

            // Fluids
            var fluidCap = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, newState, newBe, null);
            for (FluidStack stack : storedFluids) {
                if (fluidCap != null) {
                    fluidCap.fill(stack, net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
                }
            }
        }

        // 5. Effects (sound, particles)

        // 6. Shrink installer item stack
        if (player != null && !player.isCreative()) {
            context.getItemInHand().shrink(1);
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
