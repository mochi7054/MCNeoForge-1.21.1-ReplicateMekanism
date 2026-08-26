package com.github.mochi7054.forensic;

import com.buuz135.replication.api.pattern.IMatterPatternHolder;
import com.buuz135.replication.api.pattern.IMatterPatternModifier;
import com.buuz135.replication.api.pattern.IMatterPatternModifier.ModifierAction;
import com.buuz135.replication.api.pattern.MatterPattern;
import com.buuz135.replication.calculation.MatterCompound;
import com.buuz135.replication.calculation.ReplicationCalculation;
import com.github.mochi7054.ReplicateMekanism;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.prefab.TileEntityConfigurableMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ForensicChamberBlockEntity extends TileEntityConfigurableMachine {

    public static final long SCAN_ENERGY_COST = 10_000L; // 10,000 J (4,000 FE)
    public static final long MAX_ENERGY = 40_000L;

    public MachineEnergyContainer<ForensicChamberBlockEntity> energyContainer;
    public BasicInventorySlot inputSlot;
    public BasicInventorySlot chipInputSlot;
    public OutputInventorySlot chipOutputSlot;
    public EnergyInventorySlot energySlot;

    public ForensicChamberBlockEntity(BlockPos pos, BlockState state) {
        super(state.getBlockHolder(), pos, state);

        ejectorComponent = new TileComponentEjector(this);

        configComponent.setupItemIOExtraConfig(inputSlot, chipOutputSlot, chipInputSlot, energySlot);
        configComponent.setupInputConfig(TransmissionType.ENERGY, energyContainer);

        ejectorComponent.setOutputData(configComponent, TransmissionType.ITEM);
    }

    @NotNull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSideWithConfig(this);
        energyContainer = MachineEnergyContainer.input(this, listener);
        builder.addContainer(energyContainer);
        return builder.build();
    }

    @SuppressWarnings("unchecked")
    private static <T> List<?> getPatternsRaw(IMatterPatternHolder<T> holder, net.minecraft.world.level.Level level, Object target) {
        return holder.getPatterns(level, (T) target);
    }

    @NotNull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(this);

        // Input slot accepts only items that have matter compound values
        inputSlot = BasicInventorySlot.at(stack -> {
            MatterCompound compound = ReplicationCalculation.getMatterCompound(stack);
            return compound != null && !compound.getValues().isEmpty();
        }, listener, 32, 42);

        // Chip input slot accepts any Memory Chip / Pattern Modifier
        chipInputSlot = BasicInventorySlot.at(stack -> stack.getItem() instanceof IMatterPatternModifier, listener, 74, 42);

        // Chip output slot
        chipOutputSlot = OutputInventorySlot.at(listener, 116, 42);

        // Energy slot
        energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, listener, 142, 42);

        builder.addSlot(inputSlot);
        builder.addSlot(chipInputSlot);
        builder.addSlot(chipOutputSlot);
        builder.addSlot(energySlot);

        return builder.build();
    }

    public void tryAutoScan() {
        if (level == null || level.isClientSide) return;

        ItemStack inputStack = inputSlot.getStack();
        ItemStack chipStack = chipInputSlot.getStack();

        if (inputStack.isEmpty() || chipStack.isEmpty()) {
            return;
        }

        if (energyContainer.getEnergy() < SCAN_ENERGY_COST) {
            return;
        }

        if (!(chipStack.getItem() instanceof IMatterPatternModifier modifier)) {
            return;
        }

        ItemStack targetItem = inputStack.getItem().getDefaultInstance();

        // Check if this item is ALREADY 100% completed in the memory chip
        if (chipStack.getItem() instanceof IMatterPatternHolder<?> holder) {
            List<?> patterns = getPatternsRaw(holder, level, chipStack);
            if (patterns != null) {
                for (Object obj : patterns) {
                    if (obj instanceof MatterPattern pattern && !pattern.getStack().isEmpty()) {
                        if (ItemStack.isSameItemSameComponents(pattern.getStack(), targetItem)) {
                            if (pattern.getCompletion() >= 1.0f) {
                                // Already 100% identified in this chip -> Do nothing!
                                return;
                            }
                        }
                    }
                }
            }
        }

        ItemStack chipCopy = chipStack.copy();
        chipCopy.setCount(1);

        // Execute 100% pattern identification on item's default instance
        ModifierAction action = (ModifierAction) modifier.addPattern(level, chipCopy, targetItem, 1.0f);
        
        if (action != null && action.getPattern() != null) {
            ItemStack outputStack = chipOutputSlot.getStack();
            if (outputStack.isEmpty()) {
                chipOutputSlot.setStack(chipCopy);
            } else if (ItemStack.isSameItemSameComponents(outputStack, chipCopy) && outputStack.getCount() + 1 <= outputStack.getMaxStackSize()) {
                outputStack.grow(1);
            } else {
                // Output slot is full or has different item
                return;
            }

            // Consume 1 chip, 1 target item, and energy (No sound)
            chipInputSlot.shrinkStack(1, Action.EXECUTE);
            inputStack = inputSlot.getStack();
            if (!inputStack.isEmpty()) {
                inputSlot.shrinkStack(1, Action.EXECUTE);
            }
            energyContainer.extract(SCAN_ENERGY_COST, Action.EXECUTE, AutomationType.INTERNAL);

            markForSave();
        }
    }

    @Override
    protected boolean onUpdateServer() {
        boolean superRet = super.onUpdateServer();
        energySlot.fillContainerOrConvert();
        tryAutoScan();
        return superRet;
    }
}