package com.github.mochi7054.fluid;

import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.neoforged.neoforge.fluids.FluidType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;

public class SimpleDummyFluid extends Fluid {
    private final java.util.function.Supplier<FluidType> fluidType;

    public SimpleDummyFluid(java.util.function.Supplier<FluidType> fluidType) {
        this.fluidType = fluidType;
    }

    @Override
    public FluidType getFluidType() {
        return fluidType.get();
    }

    @Override
    public boolean isSource(FluidState state) {
        return true;
    }

    @Override
    public int getAmount(FluidState state) {
        return 8;
    }

    @Override
    public Item getBucket() {
        return net.minecraft.world.item.Items.AIR;
    }

    @Override
    protected boolean canBeReplacedWith(FluidState state, net.minecraft.world.level.BlockGetter level, net.minecraft.core.BlockPos pos, Fluid fluid, net.minecraft.core.Direction direction) {
        return false;
    }

    @Override
    protected net.minecraft.world.phys.Vec3 getFlow(net.minecraft.world.level.BlockGetter level, net.minecraft.core.BlockPos pos, FluidState state) {
        return net.minecraft.world.phys.Vec3.ZERO;
    }

    @Override
    public int getTickDelay(net.minecraft.world.level.LevelReader level) {
        return 5;
    }

    @Override
    protected float getExplosionResistance() {
        return 100.0f;
    }

    @Override
    protected BlockState createLegacyBlock(FluidState state) {
        return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
    }

    @Override
    public boolean isSame(Fluid fluid) {
        return fluid == this;
    }

    @Override
    protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
        // do nothing
    }

    @Override
    public net.minecraft.world.phys.shapes.VoxelShape getShape(FluidState state, net.minecraft.world.level.BlockGetter level, net.minecraft.core.BlockPos pos) {
        return net.minecraft.world.phys.shapes.Shapes.empty();
    }

    @Override
    public float getOwnHeight(FluidState state) {
        return 1.0f;
    }

    @Override
    public float getHeight(FluidState state, net.minecraft.world.level.BlockGetter level, net.minecraft.core.BlockPos pos) {
        return 1.0f;
    }
}
