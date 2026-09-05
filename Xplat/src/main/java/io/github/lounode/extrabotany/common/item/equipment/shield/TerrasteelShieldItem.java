package io.github.lounode.extrabotany.common.item.equipment.shield;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;

import vazkii.botania.api.BotaniaAPI;

public class TerrasteelShieldItem extends ManasteelShieldItem {

	public static final float THORNS_RETURN_RATE = 0.5F;

	public TerrasteelShieldItem(Properties properties, Tier tier) {
		super(properties, tier);
	}

	public TerrasteelShieldItem(Properties properties) {
		this(properties, BotaniaAPI.instance().getTerrasteelItemTier());
	}

	@Override
	public void onShieldBlock(ItemStack stack, LivingEntity blocker, DamageSource source, float damage) {
		super.onShieldBlock(stack, blocker, source, damage);
		Entity entity = source.getEntity();
		if (entity != null) {
			entity.hurt(damageSource(blocker), damage * THORNS_RETURN_RATE);
		}

		blocker.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20 * 10));
	}

	public DamageSource damageSource(LivingEntity entity) {
		if (entity instanceof Player player) {
			return player.damageSources().playerAttack(player);
		} else {
			return entity.damageSources().mobAttack(entity);
		}
	}
}
