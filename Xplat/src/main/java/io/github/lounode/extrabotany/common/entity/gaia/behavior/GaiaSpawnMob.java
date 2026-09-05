package io.github.lounode.extrabotany.common.entity.gaia.behavior;

import com.google.common.collect.ImmutableMap;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;

import vazkii.botania.common.entity.PixieEntity;
import vazkii.botania.common.item.BotaniaItems;

import io.github.lounode.extrabotany.common.entity.ExtraBotanyMemoryType;
import io.github.lounode.extrabotany.common.entity.gaia.Gaia;
import io.github.lounode.extrabotany.common.entity.gaia.GaiaAI;

import java.util.ArrayList;
import java.util.List;

public class GaiaSpawnMob<E extends Gaia> extends Behavior<E> {

	public GaiaSpawnMob(int duration) {
		super(ImmutableMap.of(
				MemoryModuleType.NEAREST_PLAYERS, MemoryStatus.VALUE_PRESENT,
				ExtraBotanyMemoryType.MOB_SPAWN_TICKS, MemoryStatus.VALUE_PRESENT
		), duration);
	}

	@Override
	protected boolean canStillUse(ServerLevel level, E entity, long gameTime) {
		return true;
	}

	@Override
	protected void start(ServerLevel level, E gaia, long gameTime) {

	}

	@Override
	protected void tick(ServerLevel level, E gaia, long gameTime) {
		int countDown = getSpawnTicks(gaia);
		int ticks = GaiaAI.MOB_SPAWN_TICKS - countDown;

		gaia.setDeltaMovement(Vec3.ZERO);

		gaia.setInvulTime(gaia.getInvulTime() + 1);

		//Start fly
		if (ticks < GaiaAI.MOB_SPAWN_START_TICKS) {
			gaia.setDeltaMovement(gaia.getDeltaMovement().x(), 0.3, gaia.getDeltaMovement().z());
		}

		//Spawn
		if (ticks > GaiaAI.MOB_SPAWN_START_TICKS * 2 &&
				countDown > GaiaAI.MOB_SPAWN_END_TICKS &&
				countDown % GaiaAI.MOB_SPAWN_WAVE_TIME == 0) {
			spawnMobs(gaia, getPlayers(gaia));
		}

		setSpawnTicks(gaia, countDown - 1);
		if (countDown <= 0) {
			doStop(level, gaia, gameTime);
		}
	}

	private void spawnMobs(Gaia gaia, List<Player> players) {
		for (int pl = 0; pl < players.size(); pl++) {
			for (int i = 0; i < 3 + gaia.level().random.nextInt(2); i++) {
				Mob entity = switch (gaia.level().random.nextInt(3)) {
					case 0 -> {
						if (gaia.level().random.nextInt(12) == 0) {
							yield EntityType.WITCH.create(gaia.level());
						}
						yield EntityType.ZOMBIE.create(gaia.level());
					}
					case 1 -> {
						if (gaia.level().random.nextInt(8) == 0) {
							yield EntityType.WITHER_SKELETON.create(gaia.level());
						}
						yield EntityType.SKELETON.create(gaia.level());
					}
					case 2 -> {
						if (!players.isEmpty()) {
							for (int j = 0; j < 1 + gaia.level().random.nextInt(8); j++) {
								PixieEntity pixie = new PixieEntity(gaia.level(), true);
								pixie.setProps(players.get(gaia.getRandom().nextInt(players.size())), gaia, 8);
								pixie.setPos(gaia.getX() + gaia.getBbWidth() / 2, gaia.getY() + 2, gaia.getZ() + gaia.getBbWidth() / 2);
								EventHooks.finalizeMobSpawn(pixie, (ServerLevelAccessor) gaia.level(),
										gaia.level().getCurrentDifficultyAt(pixie.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
								gaia.level().addFreshEntity(pixie);
							}
						}
						yield null;
					}
					default -> null;
				};

				if (entity != null) {
					if (!entity.fireImmune()) {
						entity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 600, 0));
					}
					float range = 6F;
					entity.setPos(gaia.getX() + 0.5 + Math.random() * range - range / 2, gaia.getY() - 1,
							gaia.getZ() + 0.5 + Math.random() * range - range / 2);
					EventHooks.finalizeMobSpawn(entity, (ServerLevelAccessor) gaia.level(),
							gaia.level().getCurrentDifficultyAt(entity.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
					if (entity instanceof WitherSkeleton) {
						entity.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(BotaniaItems.ELEMENTIUM_SWORD));
					}
					gaia.level().addFreshEntity(entity);
				}
			}
		}
	}

	@Override
	protected void stop(ServerLevel level, E entity, long gameTime) {
		entity.getBrain().eraseMemory(ExtraBotanyMemoryType.MOB_SPAWN_TICKS);
	}

	protected List<Player> getPlayers(Gaia gaia) {
		return gaia.getBrain().getMemory(MemoryModuleType.NEAREST_PLAYERS).orElse(new ArrayList<>());
	}

	protected int getSpawnTicks(Gaia gaia) {
		return gaia.getBrain().getMemory(ExtraBotanyMemoryType.MOB_SPAWN_TICKS).orElse(0);
	}

	protected void setSpawnTicks(Gaia gaia, int ticks) {
		gaia.getBrain().setMemory(ExtraBotanyMemoryType.MOB_SPAWN_TICKS, ticks);
	}
}
