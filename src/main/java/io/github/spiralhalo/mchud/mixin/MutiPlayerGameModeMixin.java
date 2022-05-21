package io.github.spiralhalo.mchud.mixin;

import io.github.spiralhalo.mchud.BucketWarning;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public class MutiPlayerGameModeMixin {
	@Inject(method = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;useItem(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;", at = @At("HEAD"), cancellable = true)
	void onUseItem(Player player, Level level, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResult> cir) {
		if (!BucketWarning.canUseItem(player, interactionHand)) {
			// PASS is probably safer
			cir.setReturnValue(InteractionResult.PASS);
		}
	}
}
