package com.github.mochi7054.chemical;

import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalBuilder;
import mekanism.common.registration.impl.ChemicalDeferredRegister;
import mekanism.common.registration.impl.DeferredChemical;
import net.minecraft.resources.ResourceLocation;
import com.github.mochi7054.ReplicateMekanism;

public class RMChemical {
    public static final ChemicalDeferredRegister CHEMICALS = new ChemicalDeferredRegister(ReplicateMekanism.MODID);
    public static final DeferredChemical<Chemical> REPLICA = createInfuse("replica", ReplicateMekanism.rl("infuse_type/replica"), 0x445763);
    
    public static final DeferredChemical<Chemical> EARTH_MATTER = createInfuse("earth_matter", ReplicateMekanism.rl("infuse_type/replica"), 0x8A7355);
    public static final DeferredChemical<Chemical> NETHER_MATTER = createInfuse("nether_matter", ReplicateMekanism.rl("infuse_type/replica"), 0x933030);
    public static final DeferredChemical<Chemical> ORGANIC_MATTER = createInfuse("organic_matter", ReplicateMekanism.rl("infuse_type/replica"), 0x6E9330);
    public static final DeferredChemical<Chemical> ENDER_MATTER = createInfuse("ender_matter", ReplicateMekanism.rl("infuse_type/replica"), 0x30938A);
    public static final DeferredChemical<Chemical> METALLIC_MATTER = createInfuse("metallic_matter", ReplicateMekanism.rl("infuse_type/replica"), 0x8A939F);
    public static final DeferredChemical<Chemical> PRECIOUS_MATTER = createInfuse("precious_matter", ReplicateMekanism.rl("infuse_type/replica"), 0xCCB94C);
    public static final DeferredChemical<Chemical> LIVING_MATTER = createInfuse("living_matter", ReplicateMekanism.rl("infuse_type/replica"), 0xCC6E4C);
    public static final DeferredChemical<Chemical> QUANTUM_MATTER = createInfuse("quantum_matter", ReplicateMekanism.rl("infuse_type/replica"), 0xAE4CCC);

    private static DeferredChemical<Chemical> createInfuse(String id, ResourceLocation texture, int color){
        return CHEMICALS.register(id, () -> new Chemical(ChemicalBuilder.builder(texture).tint(color)));
    }
}
