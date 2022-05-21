/*
 * Copyright (c) 2022 spiralhalo <re.nanashi95@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package io.github.spiralhalo.mchud;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BucketWarning implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("Bucket Warning");
	public static final TranslatableComponent FULL_INVENTORY_WARNING = new TranslatableComponent("sh_mchud.warning.full_inventory");
	public static final TranslatableComponent ACTION_CANCELED_NOTIF = new TranslatableComponent("sh_mchud.warning.action_cancelled");

	private static BucketWarning instance;
//	private static final int CANCELED_NOTIF_DURATION = 2000;

	public static void render(PoseStack poseStack, Font font) {
		if (instance == null) return;
		if (Configuration.co.showBucketUseWarning) {
			instance.guiRenderInner(poseStack, font);
		}
	}

	public static boolean canUseItem(Player player, InteractionHand interactionHand) {
		// sus
		if (!(player instanceof LocalPlayer)) return true;

		if (instance.isDev) LOGGER.info("canUseItem is consulted");

		if (!Configuration.co.cancelPlayerActionEnabled || instance == null) return true;

		ItemStack itemStack = player.getItemInHand(interactionHand);
		instance.reusableState.set(itemStack);

		if (!instance.reusableState.valid()) return true;

		// It's okay to inspect the inventory in this case, for reliable result each time
		if (instance.inspectIsInventoryFull((LocalPlayer) player)) {
			if (instance.isDev) LOGGER.info("Player action is canceled");

			// inform the player
			if (Configuration.co.notifyActionCancellation) {
				player.displayClientMessage(ACTION_CANCELED_NOTIF, true);
//				instance.showCanceledSince = System.currentTimeMillis();
			}

			// might as well cache the result
			instance.showWarning = true;

			return false;
		}

		return true;
	}

	private static boolean bucketLike(ItemStack itemStack) {
		return itemStack.is(Items.BUCKET) || itemStack.is(Items.GLASS_BOTTLE);
	}

	private final ItemBucketState handState = new ItemBucketState();
	private final ItemBucketState reusableState = new ItemBucketState();
	private boolean showWarning = false;
//	private long showCanceledSince;
	private long lastChecked;
	private boolean isDev;

	@Override
	public void onInitializeClient() {
//		showCanceledSince = System.currentTimeMillis() - CANCELED_NOTIF_DURATION;
		instance = this;
		isDev = FabricLoader.getInstance().isDevelopmentEnvironment();
		Configuration.loadConfigFromDisk();
	}

	private void guiRenderInner(PoseStack poseStack, Font font){
		final Minecraft minecraft = Minecraft.getInstance();
		if (minecraft == null) return;

		final LocalPlayer player = minecraft.player;
		if (player == null) return;

		final ItemStack handItem = minecraft.player.getItemInHand(InteractionHand.MAIN_HAND);
		final long time = System.currentTimeMillis(); //idk how to use tick delta

		if (handState.stateChanged(handItem) || time - lastChecked > 200L) {
			lastChecked = time;
			showWarning = handState.valid() && inspectIsInventoryFull(player);
		}

		if (showWarning) {
			drawCenteredInfoLineAboveVanilla(poseStack, font, FULL_INVENTORY_WARNING, Mth.clamp(Configuration.co.warningOffsetLine, 0, 10), 255);
		}

//		final float showCanceledInterpolated = Mth.clamp(1.0f - ((time - showCanceledSince) / (float)CANCELED_NOTIF_DURATION), 0.0f, 1.0f);
//		if (showCanceledInterpolated > 0.025f) {
//			final int alpha = Math.round(255f * (showCanceledInterpolated >= 0.5f ? 1.0f : (showCanceledInterpolated * 2.0f)));
//			drawCenteredInfoLineAboveVanilla(poseStack, font, ACTION_CANCELED_NOTIF, 1, alpha);
//		}
	}

	private void drawCenteredInfoLineAboveVanilla(PoseStack poseStack, Font font, Component component, int line, int alpha) {
		final int colorWAlpha = FastColor.ARGB32.color(alpha, 255, 255, 255);
		final int width = font.width(component);
		// shadow
		font.draw(poseStack, component, -width / 2f + 1, 1 - line * 16, FastColor.ARGB32.multiply(0x66000000, colorWAlpha));
		font.draw(poseStack, component, -width / 2f    ,   - line * 16, colorWAlpha);
	}

	private boolean inspectIsInventoryFull(LocalPlayer player) {
		// relatively expensive loop operation
		final boolean result = player.getInventory().getFreeSlot() == -1;

		if (isDev) {
			LOGGER.info("Inventory was inspected");
		}

		return result;
	}

	private static class ItemBucketState {
		private boolean isBucketLike = false;
		private boolean isStacked = false;

		private void set(ItemStack itemStack) {
			isBucketLike = bucketLike(itemStack);
			isStacked = itemStack.getCount() > 1;
		}

		private boolean equal(ItemStack itemStack) {
			return (bucketLike(itemStack) == isBucketLike) && (itemStack.getCount() > 1 == isStacked);
		}

		private boolean valid() {
			return isBucketLike && isStacked;
		}

		private boolean stateChanged(ItemStack itemStack) {
			if (equal(itemStack)) {
				return false;
			}

			set(itemStack);
			return true;
		}
	}
}
