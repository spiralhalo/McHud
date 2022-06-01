package io.github.spiralhalo.mchud.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.spiralhalo.mchud.ext.DebugOverlayExt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.util.FrameTimer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(DebugScreenOverlay.class)
public abstract class DebugScreenOverlayMixin implements DebugOverlayExt {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Shadow
	protected abstract void drawChart(PoseStack poseStack, FrameTimer frameTimer, int i, int j, boolean bl);

	@Override
	public void renderFpsChart(PoseStack poseStack) {
		int i = this.minecraft.getWindow().getGuiScaledWidth();
		this.drawChart(poseStack, this.minecraft.getFrameTimer(), 0, i / 2, true);
	}
}
