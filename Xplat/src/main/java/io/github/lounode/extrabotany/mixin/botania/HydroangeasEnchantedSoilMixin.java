package io.github.lounode.extrabotany.mixin.botania;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import vazkii.botania.common.block.block_entity.flower.generating.HydroangeasBlockEntity;

import io.github.lounode.extrabotany.common.block.ExtraBotanyBlocks;

@Mixin(value = HydroangeasBlockEntity.class, remap = false)
public abstract class HydroangeasEnchantedSoilMixin {
	@Shadow
	private int passiveDecayTicks;

	@Unique
	private int extrabotany_decayTicksBeforeTick;
	@Unique
	private boolean extrabotany_preserveDecayTicks;

	@Inject(method = "tickFlower", at = @At("HEAD"))
	private void extrabotany_captureDecayTicks(CallbackInfo ci) {
		HydroangeasBlockEntity self = (HydroangeasBlockEntity) (Object) this;
		extrabotany_preserveDecayTicks = !self.isFloating()
				&& self.getLevel().getBlockState(self.getBlockPos().below()).is(ExtraBotanyBlocks.enchantedSoil);
		if (extrabotany_preserveDecayTicks) {
			extrabotany_decayTicksBeforeTick = passiveDecayTicks;
			// Restoring at TAIL alone is too late when ++age destroys the flower.
			// Keep the original age for persistence, but make this tick non-decaying.
			passiveDecayTicks = 0;
		}
	}

	@Inject(method = "tickFlower", at = @At("TAIL"))
	private void extrabotany_restoreDecayTicks(CallbackInfo ci) {
		if (extrabotany_preserveDecayTicks) {
			passiveDecayTicks = extrabotany_decayTicksBeforeTick;
		}
	}
}
