package com.github.mochi7054.client.render;

import com.github.mochi7054.block.entity.CollapserBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import mekanism.client.model.MekanismModelCache;
import mekanism.client.render.tileentity.MekanismTileEntityRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

public class RenderCollapser extends MekanismTileEntityRenderer<CollapserBlockEntity> {

    public RenderCollapser(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void render(@NotNull CollapserBlockEntity tile, float partialTicks, @NotNull PoseStack matrix,
                          @NotNull MultiBufferSource renderer, int light, int overlayLight, @NotNull ProfilerFiller profiler) {
        if (tile.getActive()) {
            matrix.pushPose();
            matrix.translate(0.5D, 0.5D, 0.5D);
            // 栄養液化機と同じ回転計算 (時間と partialTicks に基づいて回転)
            float angle = ((tile.getLevel().getGameTime() + partialTicks) * 25.0F) % 360.0F;
            matrix.mulPose(Axis.YP.rotationDegrees(angle));
            matrix.translate(-0.5D, -0.5D, -0.5D);
            PoseStack.Pose entry = matrix.last();
            VertexConsumer buffer = renderer.getBuffer(Sheets.solidBlockSheet());
            for (BakedQuad quad : MekanismModelCache.INSTANCE.LIQUIFIER_BLADE.getQuads(tile.getLevel().random)) {
                buffer.putBulkData(entry, quad, 1.0F, 1.0F, 1.0F, 1.0F, light, overlayLight);
            }
            matrix.popPose();
        }
    }

    @Override
    protected String getProfilerSection() {
        return "collapser";
    }
}
