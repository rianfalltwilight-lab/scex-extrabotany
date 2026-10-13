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

/**
 * 泰拉钢盾牌。格挡成功时，把挡下的伤害的一半反弹给攻击者，并给持盾者 10 秒的生命恢复。
 * Terrasteel Shield. A successful block returns half of the blocked damage to the attacker, and gives Regeneration to the holder for 10 seconds.
 */
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
