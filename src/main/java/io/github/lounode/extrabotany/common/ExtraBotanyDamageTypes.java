package io.github.lounode.extrabotany.common;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;

import org.jetbrains.annotations.Nullable;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 本模组的伤害类型。伤害类型是数据包注册表，定义在 {@code data/extrabotany/damage_type}。
 * The damage types of this mod. Damage types are a data pack registry. The definitions are in {@code data/extrabotany/damage_type}.
 *
 * <p>伤害是否无视护甲由伤害类型标签决定。标签在 {@code data/minecraft/tags/damage_type}。
 * <p>Damage type tags control if the damage ignores armor. The tags are in {@code data/minecraft/tags/damage_type}.
 */
public final class ExtraBotanyDamageTypes {
	public static final ResourceKey<DamageType> LINK_DAMAGE =
			ResourceKey.create(Registries.DAMAGE_TYPE, prefix("link"));
	public static final ResourceKey<DamageType> EXCALIBUR_BEAM_DAMAGE =
			ResourceKey.create(Registries.DAMAGE_TYPE, prefix("excalibur"));
	public static final ResourceKey<DamageType> JINGWEI_PUNCH_DAMAGE =
			ResourceKey.create(Registries.DAMAGE_TYPE, prefix("jingwei"));
	public static final ResourceKey<DamageType> REVERSE_HEAL_DAMAGE =
			ResourceKey.create(Registries.DAMAGE_TYPE, prefix("reverse_heal"));
	public static final ResourceKey<DamageType> BACKFIRE_DAMAGE =
			ResourceKey.create(Registries.DAMAGE_TYPE, prefix("backfire"));
	/**
	 * 穿透攻击伤害。对应原版的 {@code NETURAL_PIERCING}。
	 * Piercing attack damage. This type agrees with {@code NETURAL_PIERCING} of the original.
	 */
	public static final ResourceKey<DamageType> PIERCING_DAMAGE =
			ResourceKey.create(Registries.DAMAGE_TYPE, prefix("piercing"));
	/**
	 * 穿透魔法伤害。对应原版的 {@code MAGIC_PIERCING}。
	 * Piercing magic damage. This type agrees with {@code MAGIC_PIERCING} of the original.
	 */
	public static final ResourceKey<DamageType> MAGIC_PIERCING_DAMAGE =
			ResourceKey.create(Registries.DAMAGE_TYPE, prefix("magic_piercing"));

	/**
	 * 律者的冰伤害。机车和真理之钥使用它。
	 * The ice damage of the Herrscher. The Armored Motor and the Key of Truth use it.
	 */
	public static final ResourceKey<DamageType> HERRSCHER_ICE_DAMAGE =
			ResourceKey.create(Registries.DAMAGE_TYPE, prefix("herrscher_ice"));
	/**
	 * 律者的雷伤害。心斩使用它。
	 * The thunder damage of the Herrscher. The Thunder Slash uses it.
	 */
	public static final ResourceKey<DamageType> HERRSCHER_THUNDER_DAMAGE =
			ResourceKey.create(Registries.DAMAGE_TYPE, prefix("herrscher_thunder"));

	/**
	 * 薪炎的火焰伤害。
	 * The flame damage of the Flamescion.
	 */
	public static final ResourceKey<DamageType> FLAMESCION_FLAME_DAMAGE =
			ResourceKey.create(Registries.DAMAGE_TYPE, prefix("flamescion_flame"));

	/**
	 * 穿透火焰伤害。樱花的誓约使用它。
	 * Piercing fire damage. The Oath of Sakura uses it.
	 */
	public static final ResourceKey<DamageType> PIERCING_FIRE_DAMAGE =
			ResourceKey.create(Registries.DAMAGE_TYPE, prefix("piercing_fire"));

	public static final DamageType LINK = new DamageType("extrabotany.link", 0.1f);
	public static final DamageType EXCALIBUR = new DamageType("extrabotany.excalibur", 0.1f);
	public static final DamageType JINGWEI = new DamageType("extrabotany.jingwei", 0.1f);
	public static final DamageType REVERSE_HEAL = new DamageType("extrabotany.reverse_heal", 0.1f);
	public static final DamageType BACKFIRE = new DamageType("extrabotany.backfire", 0.1f);
	// 两种穿透伤害使用原版的死亡消息，所以不需要新的语言键。
	// The two piercing types use the vanilla death messages, so new language keys are not necessary.
	public static final DamageType PIERCING = new DamageType("mob", 0.1f);
	public static final DamageType MAGIC_PIERCING = new DamageType("magic", 0.0f);
	public static final DamageType HERRSCHER_ICE = new DamageType("extrabotany.herrscher_ice", 0.0f);
	public static final DamageType HERRSCHER_THUNDER = new DamageType("extrabotany.herrscher_thunder", 0.0f);
	public static final DamageType FLAMESCION_FLAME = new DamageType("extrabotany.flamescion_flame", 0.1f);
	// 穿透火焰伤害使用原版"被烧死"的死亡消息。
	// Piercing fire damage uses the vanilla "burned to death" death message.
	public static final DamageType PIERCING_FIRE = new DamageType("onFire", 0.0f);

	private ExtraBotanyDamageTypes() {}

	/**
	 * 创建 {@link DamageSource} 的工厂方法。
	 * Factory methods that make a {@link DamageSource}.
	 */
	public static final class Sources {

		private Sources() {}

		private static Holder.Reference<DamageType> getHolder(RegistryAccess ra, ResourceKey<DamageType> key) {
			return ra.registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key);
		}

		public static DamageSource source(RegistryAccess ra, ResourceKey<DamageType> resourceKey) {
			return new DamageSource(getHolder(ra, resourceKey));
		}

		public static DamageSource source(RegistryAccess ra, ResourceKey<DamageType> resourceKey, @Nullable Entity entity) {
			return new DamageSource(getHolder(ra, resourceKey), entity);
		}

		public static DamageSource source(RegistryAccess ra, ResourceKey<DamageType> resourceKey, @Nullable Entity entity, @Nullable Entity entity2) {
			return new DamageSource(getHolder(ra, resourceKey), entity, entity2);
		}

		public static DamageSource linkDamage(RegistryAccess ra) {
			return source(ra, LINK_DAMAGE);
		}

		public static DamageSource excaliburDamage(RegistryAccess ra, Entity source) {
			return source(ra, EXCALIBUR_BEAM_DAMAGE, source);
		}

		public static DamageSource jingweiDamage(RegistryAccess ra, @Nullable Entity source) {
			return source == null ? source(ra, JINGWEI_PUNCH_DAMAGE) : source(ra, JINGWEI_PUNCH_DAMAGE, source);
		}

		public static DamageSource reverseHealDamage(RegistryAccess ra) {
			return source(ra, REVERSE_HEAL_DAMAGE);
		}

		public static DamageSource backfireDamage(RegistryAccess ra) {
			return source(ra, BACKFIRE_DAMAGE);
		}

		public static DamageSource piercingDamage(RegistryAccess ra, @Nullable Entity attacker) {
			return source(ra, PIERCING_DAMAGE, attacker);
		}

		/**
		 * 有攻击者但是没有直接来源实体的伤害。律者的冰伤害、雷伤害和薪炎的火焰伤害使用它。
		 * 没有直接来源实体，所以它不触发荆棘这类近战效果。
		 * Damage that has an attacker but no direct source entity. The ice damage and the thunder damage of the Herrscher and the flame damage of the Flamescion use it.
		 * There is no direct source entity, so it does not start melee effects such as thorns.
		 */
		public static DamageSource withoutDirectEntity(RegistryAccess ra, ResourceKey<DamageType> type, @Nullable Entity attacker) {
			return source(ra, type, null, attacker);
		}

		public static DamageSource magicPiercingDamage(RegistryAccess ra, @Nullable Entity attacker) {
			return attacker == null
					? source(ra, MAGIC_PIERCING_DAMAGE)
					: source(ra, MAGIC_PIERCING_DAMAGE, attacker, attacker);
		}
	}
}
