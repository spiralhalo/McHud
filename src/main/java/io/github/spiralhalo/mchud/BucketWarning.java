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
	public static final Logger LOGGER = LoggerFactory.getLogger("Bucket Warning'");
	public static final TranslatableComponent FULL_INVENTORY_WARNING = new TranslatableComponent("sh_mchud.warning.full_inventory");

	public static void render(PoseStack poseStack, Font font) {
		final Minecraft minecraft = Minecraft.getInstance();
		if (minecraft == null) return;

		final LocalPlayer player = minecraft.player;
		if (player == null) return;

		final ItemStack handItem = minecraft.player.getItemInHand(InteractionHand.MAIN_HAND);
		if (handItem.is(Items.BUCKET) && handItem.getCount() > 1) {
			if (player.getInventory().getFreeSlot() == -1) {
				final int width = font.width(FULL_INVENTORY_WARNING);
				font.draw(poseStack, FULL_INVENTORY_WARNING, -width / 2f, 0, 0xFFFFFFFF);
			}
		}
	}

	@Override
	public void onInitializeClient() {
	}
}
