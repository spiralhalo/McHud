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
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BucketWarning implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("Bucket Warning");
	public static final TranslatableComponent FULL_INVENTORY_WARNING = new TranslatableComponent("sh_mchud.warning.full_inventory");

	private static BucketWarning instance;

	public static void render(PoseStack poseStack, Font font) {
		if (instance == null) return;
		instance.guiRenderInner(poseStack, font);
	}

	private final ItemBucketState handState = new ItemBucketState();
	private boolean showWarning = false;
	private boolean isDev;

	@Override
	public void onInitializeClient() {
		instance = this;
		isDev = FabricLoader.getInstance().isDevelopmentEnvironment();
	}

	private void guiRenderInner(PoseStack poseStack, Font font){
		final Minecraft minecraft = Minecraft.getInstance();
		if (minecraft == null) return;

		final LocalPlayer player = minecraft.player;
		if (player == null) return;

		final ItemStack handItem = minecraft.player.getItemInHand(InteractionHand.MAIN_HAND);

		// doesn't account for inventory changes tho
		if (handState.stateChanged(handItem)) {
			showWarning = handState.valid() && inspectInventory(player);
		}

		if (showWarning) {
			final int width = font.width(FULL_INVENTORY_WARNING);
			font.draw(poseStack, FULL_INVENTORY_WARNING, -width / 2f, 0, 0xFFFFFFFF);
		}
	}

	private boolean inspectInventory(LocalPlayer player) {
		// relatively expensive loop operation
		final boolean result = player.getInventory().getFreeSlot() == -1;

		if (isDev) {
			LOGGER.info("Inventory was inspected");
		}

		return result;
	}

	private static class ItemBucketState {
		private boolean isBucket = false;
		private boolean isStacked = false;

		private void set(ItemStack itemStack) {
			isBucket = itemStack.is(Items.BUCKET);
			isStacked = itemStack.getCount() > 1;
		}

		private boolean equal(ItemStack itemStack) {
			return (itemStack.is(Items.BUCKET) == isBucket) && (itemStack.getCount() > 1 == isStacked);
		}

		private boolean valid() {
			return isBucket && isStacked;
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
