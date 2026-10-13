package io.github.lounode.extrabotany.common.entity.gaia;

import com.mojang.serialization.Dynamic;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Unit;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.neoforge.event.EventHooks;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.common.handler.BotaniaSounds;

import io.github.lounode.extrabotany.api.gaia.GaiaArena;
import io.github.lounode.extrabotany.common.entity.ExtraBotanyEntityType;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.sounds.ExtraBotanySounds;

import java.util.List;

/**
 * 盖亚守护者III。玩家用挑战券右键盖亚仪式场地中间的信标来召唤它。
 * Guardian of Gaia III. A player uses the Challenge Ticket on the beacon in the center of a Gaia ritual arena to summon it.
 *
 * <p>它的场地比植物魔法的盖亚仪式大（半径 15 格，高 7 格）。它的攻击写在行为树里（{@link GaiaIIIAI}）：骷髅导弹、骷髅地雷和小精灵。
 * <p>Its arena is larger than that of the Gaia ritual of Botania (a radius of 15 blocks, a height of 7 blocks). Its attacks are in the behavior tree ({@link GaiaIIIAI}): skull missiles, skull landmines and pixies.
 *
 * <p>来源：重制版（Lounode）。它对应 ExtraMeteorP 1.12 的 {@code EntityGaiaIII}。
 * <p>Source: Reburn (Lounode). It corresponds to {@code EntityGaiaIII} of ExtraMeteorP 1.12.
 *
 * <p>这个移植版以前把原版 1.16 的本我做成了这个 Boss 的第二个大阶段。现在本我是一个单独的 Boss（{@link Ego}），和原版 1.16 相同。旧存档里这个 Boss 的阶段数据（{@code BigPhase}、{@code EgoStage} 等键）不再被读取。
 * <p>Before, this port made the Ego of the original 1.16 the second large phase of this boss. Now the Ego is a separate boss ({@link Ego}), the same as in the original 1.16. The phase data of this boss in old saves (the keys {@code BigPhase}, {@code EgoStage} and others) is no longer read.
 */
public class GaiaIII extends Gaia {
	public static final float ARENA_RANGE = 15F;
	public static final int ARENA_HEIGHT = 7;
	public static final float MAX_HP = 600F;
	private static final float DAMAGE_CAP = 30;

	public GaiaIII(EntityType<? extends GaiaIII> type, Level world) {
		super(type, world);
		this.xpReward = 1000;
	}

	public GaiaIII(EntityType<? extends GaiaIII> type, Level world, BlockPos source) {
		super(type, world, source);
	}

	public GaiaIII(Level world, BlockPos source) {
		this(ExtraBotanyEntityType.GAIA_III, world, source);
	}

	public static boolean spawn(Player player, ItemStack stack, Level world, BlockPos pos) {
		GaiaArena arena = GaiaArena.of(GlobalPos.of(world.dimension(), pos), ARENA_RANGE, ARENA_HEIGHT);
		if (!arena.checksModern(player, world, stack)) {
			return false;
		}
		// 召唤时不检查物品栏。违规的装备在战斗中被收缴。
		// The inventory is not examined at the summon. Illegal equipment is removed during the fight.

		//all checks ok, spawn the boss
		if (world instanceof ServerLevel serverLevel) {
			GaiaIII gaia = new GaiaIII(world, pos);
			gaia.setArena(arena);
			gaia.setPos(pos.getX() + 0.5, pos.getY() + 3, pos.getZ() + 0.5);

			gaia.getBrain().setMemoryWithExpiry(MemoryModuleType.IS_EMERGING, Unit.INSTANCE, (long) GaiaIIIAI.EMERGE_TIME);
			gaia.setInvulTime(GaiaIIIAI.EMERGE_TIME);
			gaia.setHealth(1F);
			gaia.bossEvent.setProgress(0.0F);

			List<Player> playersAround = arena.getPlayersAround(world);

			int playerCount = playersAround.size();
			gaia.playerCount = playerCount;
			gaia.bossEvent.setPlayerCount(playerCount);

			float healthMultiplier = 1;
			if (playerCount > 1) {
				healthMultiplier += playerCount * 0.25F;
			}
			gaia.getAttribute(Attributes.MAX_HEALTH).setBaseValue(MAX_HP * healthMultiplier);
			gaia.getAttribute(Attributes.ARMOR).setBaseValue(30);

			gaia.playSound(BotaniaSounds.GAIA_SUMMON, 0.05F, 1F);
			EventHooks.finalizeMobSpawn(gaia, serverLevel, world.getCurrentDifficultyAt(gaia.blockPosition()), MobSpawnType.EVENT, null);
			world.addFreshEntity(gaia);

			for (Player nearbyPlayer : playersAround) {
				if (nearbyPlayer instanceof ServerPlayer serverPlayer) {
					CriteriaTriggers.SUMMONED_ENTITY.trigger(serverPlayer, gaia);
				}
			}
		}
		return true;
	}

	public static AttributeSupplier.Builder createGaiaAttributes() {
		return Monster.createMonsterAttributes()
				.add(Attributes.MOVEMENT_SPEED, 0.4)
				.add(Attributes.MAX_HEALTH, MAX_HP)
				.add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
	}

	@Override
	protected Brain<?> makeBrain(Dynamic<?> dynamic) {
		return GaiaIIIAI.makeBrain(this, dynamic);
	}

	@Override
	protected void initMemories(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData spawnData) {
		GaiaIIIAI.initMemories(this, level.getLevel(), getHome().pos());
	}

	@Override
	protected void updateAI() {
		GaiaIIIAI.updateActivity(this);
	}

	@Override
	public ResourceKey<LootTable> getDefaultLootTable() {
		return this.getType().getDefaultLootTable();
	}

	@Override
	public float getDamageCap() {
		return DAMAGE_CAP;
	}

	@Override
	public int getEmergeTime() {
		return GaiaIIIAI.EMERGE_TIME;
	}

	@Override
	public SoundEvent getBGM() {
		return ExtraBotanySounds.MUSIC_GAIA3;
	}

	@Override
	public Item getGuardianBypassItem() {
		return ExtraBotanyItems.voidArchives;
	}
}
