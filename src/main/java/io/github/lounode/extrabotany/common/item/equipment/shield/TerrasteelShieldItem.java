package io.github.lounode.extrabotany.common.item.equipment.shield;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;

import vazkii.botania.api.BotaniaAPI;

/**
 * 泰拉钢盾牌。格挡成功时，把挡下的伤害的一半还给直接攻击它的实体，并点燃它 5 秒。
 * Terrasteel Shield. A successful block returns half of the blocked damage to the entity that hit it directly, and ignites that entity for 5 seconds.
 *
 * <p>来源：ExtraMeteorP 1.12 {@code ItemTerrasteelShield}。重燃的生命恢复已经去掉。
 * <p>Source: {@code ItemTerrasteelShield} of ExtraMeteorP 1.12. The Regeneration of Reburn is removed.
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
		Entity entity = source.getDirectEntity();
		if (entity != null && !source.is(DamageTypes.THORNS)) {
			entity.hurt(damageSource(blocker), damage * THORNS_RETURN_RATE);
			entity.igniteForSeconds(5);
		}
	}

	public DamageSource damageSource(LivingEntity entity) {
		// 1.12 用的是攻击伤害；这里用荆棘伤害，这样两面泰拉钢盾不会互相无限反弹（onShieldBlock 跳过荆棘伤害）。
		// 1.12 used attack damage; here it is thorns damage, so two Terrasteel Shields do not bounce it back and forth forever (onShieldBlock skips thorns damage).
		return entity.damageSources().thorns(entity);
	}
}
