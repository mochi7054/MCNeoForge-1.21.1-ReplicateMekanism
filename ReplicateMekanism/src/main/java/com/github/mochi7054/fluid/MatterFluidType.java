package com.github.mochi7054.fluid;

import net.neoforged.neoforge.fluids.FluidType;
import net.minecraft.resources.ResourceLocation;
import com.buuz135.replication.api.IMatterType;
import java.util.function.Supplier;

public class MatterFluidType extends FluidType {
    private final Supplier<IMatterType> matterTypeSupplier;

    public MatterFluidType(FluidType.Properties properties, Supplier<IMatterType> matterTypeSupplier) {
        super(properties);
        this.matterTypeSupplier = matterTypeSupplier;
    }

    public IMatterType getMatterType() {
        return matterTypeSupplier.get();
    }

    @Override
    @SuppressWarnings("removal")
    public void initializeClient(java.util.function.Consumer<net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions> consumer) {
        consumer.accept(new net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return ResourceLocation.withDefaultNamespace("block/water_still");
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return ResourceLocation.withDefaultNamespace("block/water_flow");
            }

            @Override
            public int getTintColor() {
                return getTintColorFromSupplier();
            }

            @Override
            public int getTintColor(net.neoforged.neoforge.fluids.FluidStack stack) {
                return getTintColorFromSupplier();
            }

            private int getTintColorFromSupplier() {
                try {
                    IMatterType type = matterTypeSupplier.get();
                    if (type != null) {
                        float[] color = type.getColor().get();
                        if (color != null && color.length >= 3) {
                            int r = Math.max(0, Math.min(255, (int) (color[0] * 255)));
                            int g = Math.max(0, Math.min(255, (int) (color[1] * 255)));
                            int b = Math.max(0, Math.min(255, (int) (color[2] * 255)));
                            int a = color.length > 3 ? Math.max(0, Math.min(255, (int) (color[3] * 255))) : 255;
                            return (a << 24) | (r << 16) | (g << 8) | b;
                        }
                    }
                } catch (Exception ignored) {}
                return 0xFFFFFFFF;
            }
        });
    }
}
