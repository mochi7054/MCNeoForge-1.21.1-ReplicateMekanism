package com.github.mochi7054.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue REPLICA_UPGRADE_MAX_STACK = BUILDER
            .comment("The maximum stack size for the replica upgrade (1-8)")
            .translation("replicatemekanism.configuration.replicaUpgradeMaxStack")
            .defineInRange("replicaUpgradeMaxStack", 1, 1, 8);

    public static final ModConfigSpec SPEC = BUILDER.build();

    public static int getReplicaUpgradeMaxStack() {
        try {
            return REPLICA_UPGRADE_MAX_STACK.get();
        } catch (Exception e) {
            return 1;
        }
    }
}
