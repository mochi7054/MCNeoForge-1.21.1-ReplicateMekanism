package com.github.mochi7054;

import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalBuilder;
import mekanism.common.registration.impl.ChemicalDeferredRegister;
import mekanism.common.registration.impl.DeferredChemical;
import net.minecraft.resources.ResourceLocation;


public class RMChemical {
    public static final ChemicalDeferredRegister CHEMICALS = new ChemicalDeferredRegister(ReplicateMekanism.MODID);
    public static final DeferredChemical<Chemical> REPLICA = createInfuse("replica",ReplicateMekanism.rl("infuse_type/replica"),0x445763);
    private static DeferredChemical<Chemical> createInfuse(String id, ResourceLocation texture, int color){
        return CHEMICALS.register(id,() -> new Chemical(ChemicalBuilder.builder(texture).tint(color)));
    }
}
