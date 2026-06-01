package com.github.mochi7054.mixin;

import java.util.HashSet;
import java.util.Set;
import mekanism.api.Upgrade;
import mekanism.common.inventory.slot.UpgradeInventorySlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import com.github.mochi7054.ReplicateMekanism;

@Mixin(value = UpgradeInventorySlot.class, remap = false)
public class UpgradeInventorySlotMixin {

    @ModifyVariable(
        method = "input(Lmekanism/api/IContentsListener;Ljava/util/Set;)Lmekanism/common/inventory/slot/UpgradeInventorySlot;",
        at = @At("HEAD"),
        argsOnly = true,
        ordinal = 0
    )
    private static Set<Upgrade> modifySupportedTypes(Set<Upgrade> supportedTypes) {
        if (supportedTypes != null) {
            Set<Upgrade> mutableSet = new HashSet<>(supportedTypes);
            mutableSet.add(ReplicateMekanism.REPLICA_UPGRADE_TYPE);
            return mutableSet;
        }
        return supportedTypes;
    }
}
