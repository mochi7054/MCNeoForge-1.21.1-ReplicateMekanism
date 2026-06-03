package com.github.mochi7054.mixin;

import java.util.Arrays;
import mekanism.api.Upgrade;
import mekanism.api.text.EnumColor;
import mekanism.api.text.ILangEntry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Upgrade.class, remap = false)
public abstract class UpgradeMixin {

    @Shadow
    @Final
    @Mutable
    private static Upgrade[] $VALUES;

    @Shadow
    @Final
    @Mutable
    private static java.util.function.IntFunction<Upgrade> BY_ID;

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void onClinit(CallbackInfo ci) {
        ILangEntry langKey = new com.github.mochi7054.lang.ReplicaLangEntry("upgrade.replicatemekanism.replica");
        ILangEntry descLangKey = new com.github.mochi7054.lang.ReplicaLangEntry("upgrade.replicatemekanism.replica.desc");

        Upgrade replicaUpgrade = UpgradeInvoker.createUpgrade("REPLICA", 7, "replica", langKey, descLangKey, 1, EnumColor.DARK_BLUE);

        // Append to $VALUES
        Upgrade[] oldValues = $VALUES;
        Upgrade[] newValues = Arrays.copyOf(oldValues, oldValues.length + 1);
        newValues[oldValues.length] = replicaUpgrade;
        $VALUES = newValues;

        // Override BY_ID
        java.util.function.IntFunction<Upgrade> originalById = BY_ID;
        BY_ID = id -> {
            if (id == 7) {
                return replicaUpgrade;
            }
            return originalById != null ? originalById.apply(id) : null;
        };
    }
}
