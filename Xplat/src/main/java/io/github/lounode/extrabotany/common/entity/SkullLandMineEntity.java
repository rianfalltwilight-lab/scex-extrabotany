package io.github.lounode.extrabotany.common.entity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import vazkii.botania.client.fx.WispParticleData;

import io.github.lounode.extrabotany.xplat.ExtraBotanyConfig;

import java.util.List;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public abstract class SkullLandMineEntity extends MagicLandMineEntity {
	private static final ResourceLocation DEFAULT_SKULL = prefix("textures/entity/skull_missile/skull_missile.png");
	public static final float LANDMINE_WIDTH = 5F;

	public SkullLandMineEntity(EntityType<? extends SkullLandMineEntity> type, Level level) {
		super(type, level);
	}

	@Override
	protected void particle() {
		float range = LANDMINE_WIDTH / 2;
		float r = 0.2F;
		float g = 0F;
		float b = 0.2F;

		for (int i = 0; i < 6; i++) {
			WispParticleData data = WispParticleData.wisp(0.4F, r, g, b, (float) 1);
			level().addParticle(data, getX() - range + Math.random() * range * 2, getY(), getZ() - range + Math.random() * range * 2, 0, - -0.015F, 0);
		}
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
		protected void particle() {
			float range = LANDMINE_WIDTH / 2;
			float r = 0.2F;
			float g = 0F;
			float b = 0.6F;

			for (int i = 0; i < 6; i++) {
				WispParticleData data = WispParticleData.wisp(0.4F, r, g, b, 0.5F);
				level().addParticle(data, getX() - range + Math.random() * range * 2, getY(), getZ() - range + Math.random() * range * 2, 0, - -0.015F, 0);
			}
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

		//TODO 真实伤害

		@Override
		public ResourceLocation getTexture() {
			return SKULL;
		}

		@Override
		protected void particle() {
			float range = LANDMINE_WIDTH / 2;
			float r = 0.6F;
			float g = 0F;
			float b = 0.2F;

			for (int i = 0; i < 6; i++) {
				WispParticleData data = WispParticleData.wisp(0.4F, r, g, b, 0.5F);
				level().addParticle(data, getX() - range + Math.random() * range * 2, getY(), getZ() - range + Math.random() * range * 2, 0, - -0.015F, 0);
			}
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
			if (ExtraBotanyConfig.common().disableGaiaDisArm()) {
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
		protected void particle() {
			float range = LANDMINE_WIDTH / 2;
			float r = 0.2F;
			float g = 0.6F;
			float b = 0.2F;

			for (int i = 0; i < 6; i++) {
				WispParticleData data = WispParticleData.wisp(0.4F, r, g, b, 0.5F);
				level().addParticle(data, getX() - range + Math.random() * range * 2, getY(), getZ() - range + Math.random() * range * 2, 0, - -0.015F, 0);
			}
		}
	}
}
