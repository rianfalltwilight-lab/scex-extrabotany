package io.github.lounode.extrabotany.common.entity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import vazkii.botania.client.fx.WispParticleData;
import vazkii.botania.common.helper.PlayerHelper;

import io.github.lounode.extrabotany.common.lib.LibAdvancementNames;
import io.github.lounode.extrabotany.xplat.ExtraBotanyConfig;

import java.util.List;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 盖亚守护者III的骷髅地雷。它的范围是 {@value #LANDMINE_WIDTH} 格宽。它有三种：蓝色只有普通的地雷伤害；红色再追加"最大生命值 30% + 8"的真实伤害；绿色再让玩家丢下主手的物品。
 * The skull landmine of Guardian of Gaia III. Its area is {@value #LANDMINE_WIDTH} blocks wide. There are three types: blue only has the usual landmine damage; red adds true damage of "30% of maximum health + 8"; green also makes the player drop the item of the main hand.
 *
 * <p>来源：ExtraMeteorP 1.12 {@code EntitySkullLandmine}。
 * <p>Source: ExtraMeteorP 1.12 {@code EntitySkullLandmine}.
 */
public abstract class SkullLandMineEntity extends MagicLandMineEntity {
	private static final ResourceLocation DEFAULT_SKULL = prefix("textures/entity/skull_missile/skull_missile.png");
	public static final float LANDMINE_WIDTH = 5F;

	public SkullLandMineEntity(EntityType<? extends SkullLandMineEntity> type, Level level) {
		super(type, level);
	}

	@Override
	protected float getParticleRange() {
		return LANDMINE_WIDTH / 2;
	}

	@Override
	public List<? extends LivingEntity> getVictims(Class<? extends LivingEntity> entityClass) {
		return level().getEntitiesOfClass(entityClass, new AABB(getX(), getY(), getZ(), getX(), getY(), getZ()).inflate(LANDMINE_WIDTH / 2));
	}

	public ResourceLocation getTexture() {
		return DEFAULT_SKULL;
	}

	public static class Default extends SkullLandMineEntity {

		public static final ResourceLocation SKULL = prefix("textures/entity/skull_landmine/blue.png");

		public Default(EntityType<? extends Default> type, Level level) {
			super(type, level);
		}

		public Default(Level level, Entity owner) {
			super(ExtraBotanyEntityType.SKULL_LANDMINE_BLUE, level);
			this.setOwner(owner);
		}

		@Override
		public ResourceLocation getTexture() {
			return SKULL;
		}

		@Override
		protected WispParticleData getParticleData() {
			return WispParticleData.wisp(0.4F, 0.2F, 0F, 0.6F, 0.5F);
		}
	}

	public static class Danger extends SkullLandMineEntity {
		public static final ResourceLocation SKULL = prefix("textures/entity/skull_landmine/red.png");

		public Danger(EntityType<? extends Danger> type, Level level) {
			super(type, level);
		}

		public Danger(Level level, Entity owner) {
			super(ExtraBotanyEntityType.SKULL_LANDMINE_RED, level);
			this.setOwner(owner);
		}

		@Override
		public void explode() {
			if (!level().isClientSide()) {
				// Preserve the original Extra Botany red mine contract: normal trap
				// damage followed by 30% max-health + 8 true damage. Snapshot the
				// victims before super.explode() discards this entity.
				List<Player> victims = List.copyOf(getVictimPlayers());
				super.explode();
				for (Player player : victims) {
					if (!player.isSpectator() && !player.isCreative() && player.isAlive()) {
						// 和原版相同：被红色地雷炸到的玩家得到"雷区激活"进度。以前有这个进度的数据，但是没有授予它的代码。
						// The same as in the original: a player that a red landmine hits gets the advancement "Minefield Active". Before, the data of this advancement was there, but no code granted it.
						if (player instanceof ServerPlayer serverPlayer) {
							PlayerHelper.grantCriterion(serverPlayer, prefix("main/" + LibAdvancementNames.LANDMINE_ACTIVE), "code_triggered");
						}
						io.github.lounode.extrabotany.common.handler.DamageHandler.INSTANCE.dealTrueDamage(getOwner(), player, player.getMaxHealth() * 0.30F + 8F);
					}
				}
				return;
			}
			super.explode();
		}

		@Override
		public ResourceLocation getTexture() {
			return SKULL;
		}

		@Override
		protected WispParticleData getParticleData() {
			return WispParticleData.wisp(0.4F, 0.6F, 0F, 0.2F, 0.5F);
		}
	}

	public static class Disarm extends SkullLandMineEntity {
		public static final ResourceLocation SKULL = prefix("textures/entity/skull_landmine/green.png");

		public Disarm(EntityType<? extends Disarm> type, Level level) {
			super(type, level);
		}

		public Disarm(Level level, Entity owner) {
			super(ExtraBotanyEntityType.SKULL_LANDMINE_GREEN, level);
			this.setOwner(owner);
		}

		@Override
		public void explode() {
			super.explode();
			// The explosion is also simulated on the client for particles. Inventory
			// mutation must stay server authoritative (the old implementation could
			// clear a client player's hand and then be overwritten by a later sync).
			if (level().isClientSide() || ExtraBotanyConfig.common().disableGaiaDisArm()) {
				return;
			}
			List<Player> players = getVictimPlayers();
			for (Player player : players) {
				if (player.isSpectator() || player.isCreative()) {
					continue;
				}

				ItemStack itemStack = player.getItemInHand(InteractionHand.MAIN_HAND).copy();
				player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
				player.drop(itemStack, false, true);
			}
		}

		@Override
		public ResourceLocation getTexture() {
			return SKULL;
		}

		@Override
		protected WispParticleData getParticleData() {
			return WispParticleData.wisp(0.4F, 0.2F, 0.6F, 0.2F, 0.5F);
		}
	}
}
