package io.github.lounode.extrabotany.forge.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import vazkii.botania.api.item.Relic;
import vazkii.botania.api.mana.ManaItem;
import vazkii.botania.common.entity.ManaBurstEntity;
import vazkii.botania.common.item.BotaniaItems;

import io.github.lounode.extrabotany.api.item.LeftClickItem;
import io.github.lounode.extrabotany.common.brew.ExtraBotanyMobEffects;
import io.github.lounode.extrabotany.common.entity.ExtraBotanyEntityType;
import io.github.lounode.extrabotany.common.entity.judah.JudahOathEntity;
import io.github.lounode.extrabotany.common.entity.judah.JudahSpearEntity;
import io.github.lounode.extrabotany.common.entity.judah.JudahSwordEntity;
import io.github.lounode.extrabotany.common.entity.subspace.SubspaceBursts;
import io.github.lounode.extrabotany.common.entity.subspace.SubspaceEntity;
import io.github.lounode.extrabotany.common.entity.subspace.SubspaceSpearEntity;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.item.relic.JudahOathItem;
import io.github.lounode.extrabotany.common.item.relic.JudahOathItem.Variant;

import java.util.List;
import java.util.UUID;

/**
 * 亚空之矛和犹大的誓约的回归测试。
 * Regression tests for the Spear of Subspace and the Judah Oath.
 */
@GameTestHolder("extrabotany")
@PrefixGameTestTemplate(false)
public final class SpearAndJudahGameTests {
	private static final Vec3 ARENA_CENTER = new Vec3(32, 4, 32);

	private SpearAndJudahGameTests() {}

	/**
	 * 生成一个在大场地中央悬空的玩家，主手拿着绑定给他的物品，攻击冷却已满。
	 * Makes a player that floats at the center of the large arena, with an item that is bound to that player in the main hand and a full attack cooldown.
	 */
	private static Player playerHolding(GameTestHelper helper, Item item) {
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		player.setPos(helper.absoluteVec(ARENA_CENTER));
		player.setNoGravity(true);
		player.setItemInHand(InteractionHand.MAIN_HAND, boundStack(item, player));
		for (int i = 0; i < 30; i++) {
			player.tick();
		}
		return player;
	}

	private static ItemStack boundStack(Item item, Player player) {
		ItemStack stack = new ItemStack(item);
		Relic.LOOKUP.find(stack).bindToUUID(player.getUUID());
		return stack;
	}

	private static ManaItem supplyMana(Player player, int amount) {
		ItemStack tablet = new ItemStack(BotaniaItems.MANA_TABLET);
		ManaItem mana = ManaItem.LOOKUP.find(tablet);
		mana.addMana(amount);
		player.getInventory().setItem(2, tablet);
		return mana;
	}

	private static <T extends Entity> List<T> find(GameTestHelper helper, Class<T> type) {
		return helper.getLevel().getEntitiesOfClass(type, new AABB(BlockPos.containing(helper.absoluteVec(ARENA_CENTER))).inflate(30));
	}

	private static Husk husk(GameTestHelper helper, Vec3 pos) {
		Husk husk = helper.spawnWithNoFreeWill(EntityType.HUSK, new BlockPos(32, 4, 32));
		husk.setPos(pos);
		return husk;
	}

	// ---------------------------------------------------------------------
	// 亚空之矛 / Spear of Subspace
	// ---------------------------------------------------------------------

	@GameTest(template = "projectile_arena", timeoutTicks = 40)
	public static void spearLeftClickOpensARiftThatShootsOneSpear(GameTestHelper helper) {
		Player player = playerHolding(helper, ExtraBotanyItems.spearOfSubspace);
		ManaItem mana = supplyMana(player, 50000);
		LeftClickItem item = (LeftClickItem) ExtraBotanyItems.spearOfSubspace;

		item.onLeftClick(player, null);
		helper.assertTrue(mana.getMana() == 50000 - 600, "The left-click skill did not take 600 mana: " + (50000 - mana.getMana()));
		helper.assertTrue(find(helper, SubspaceEntity.class).size() == 1, "Expected 1 rift");
		item.onLeftClick(player, null);
		helper.assertTrue(mana.getMana() == 50000 - 600, "A second click in the same tick used the skill");

		// 裂隙延迟 5 tick，再过 8 tick 射出矛。
		// The rift has a delay of 5 ticks, and shoots the spear 8 ticks after that.
		helper.runAfterDelay(18, () -> {
			List<SubspaceSpearEntity> spears = find(helper, SubspaceSpearEntity.class);
			helper.assertTrue(spears.size() == 1 && spears.get(0).getOwner() == player, "Expected 1 spear from the rift, found " + spears.size());
			helper.succeed();
		});
	}

	@GameTest(template = "projectile_arena", timeoutTicks = 20)
	public static void spearSkillsNeedTheRelicOwner(GameTestHelper helper) {
		Player player = playerHolding(helper, ExtraBotanyItems.spearOfSubspace);
		ItemStack stack = player.getMainHandItem();
		Relic.LOOKUP.find(stack).bindToUUID(UUID.randomUUID());
		ManaItem mana = supplyMana(player, 50000);

		((LeftClickItem) stack.getItem()).onLeftClick(player, null);
		stack.getItem().releaseUsing(stack, helper.getLevel(), player, 100);
		helper.assertTrue(mana.getMana() == 50000 && find(helper, SubspaceEntity.class).isEmpty(),
				"A player that does not own the spear used its skills");
		helper.succeed();
	}

	@GameTest(template = "projectile_arena", timeoutTicks = 20)
	public static void spearChargedSkillOpensTheDomain(GameTestHelper helper) {
		Player player = playerHolding(helper, ExtraBotanyItems.spearOfSubspace);
		ManaItem mana = supplyMana(player, 50000);
		ItemStack stack = player.getMainHandItem();
		Item item = stack.getItem();

		item.releaseUsing(stack, helper.getLevel(), player, 100);
		helper.assertTrue(mana.getMana() == 40000, "The domain did not take 10000 mana: " + (50000 - mana.getMana()));
		helper.assertTrue(find(helper, SubspaceEntity.class).size() == 24, "Expected 24 rifts, found " + find(helper, SubspaceEntity.class).size());
		helper.assertTrue(player.hasEffect(ExtraBotanyMobEffects.ETERNITY), "The domain did not give Eternity");
		helper.assertTrue(Math.abs(player.getDeltaMovement().y - 1.5) < 1.0E-6, "The domain did not lift the player");

		item.releaseUsing(stack, helper.getLevel(), player, 100);
		helper.assertTrue(mana.getMana() == 40000 && find(helper, SubspaceEntity.class).size() == 24, "The domain opened again during the cooldown");

		for (int i = 0; i < 599; i++) {
			player.getCooldowns().tick();
		}
		helper.assertTrue(player.getCooldowns().isOnCooldown(item), "The paid cooldown ended before 600 ticks");
		player.getCooldowns().tick();
		helper.assertFalse(player.getCooldowns().isOnCooldown(item), "The paid cooldown was longer than 600 ticks");
		helper.succeed();
	}

	@GameTest(template = "projectile_arena", timeoutTicks = 20)
	public static void spearChargedSkillWithNoManaHasADoubleCooldown(GameTestHelper helper) {
		Player player = playerHolding(helper, ExtraBotanyItems.spearOfSubspace);
		ItemStack stack = player.getMainHandItem();
		Item item = stack.getItem();

		item.releaseUsing(stack, helper.getLevel(), player, 100);
		helper.assertTrue(find(helper, SubspaceEntity.class).size() == 24, "The domain did not open with no mana");
		for (int i = 0; i < 1199; i++) {
			player.getCooldowns().tick();
		}
		helper.assertTrue(player.getCooldowns().isOnCooldown(item), "The cooldown with no mana ended before 1200 ticks");
		player.getCooldowns().tick();
		helper.assertFalse(player.getCooldowns().isOnCooldown(item), "The cooldown with no mana was longer than 1200 ticks");
		helper.succeed();
	}

	@GameTest(template = "projectile_arena", timeoutTicks = 20)
	public static void domainRiftShootsFiveGoldenBursts(GameTestHelper helper) {
		Player player = playerHolding(helper, ExtraBotanyItems.spearOfSubspace);
		player.setYRot(35F);
		player.setXRot(-20F);
		SubspaceEntity rift = new SubspaceEntity(helper.getLevel(), player, SubspaceEntity.Mode.BURSTS, 120, 15, 10, 1.5F, 145F);
		rift.setPos(player.position().add(0, 4, 0));
		for (int i = 0; i < 100; i++) {
			helper.getLevel().tickNonPassenger(rift);
		}

		List<ManaBurstEntity> bursts = find(helper, ManaBurstEntity.class).stream().filter(burst -> burst.getOwner() == player).toList();
		helper.assertTrue(bursts.size() == 5, "Expected 5 bursts, found " + bursts.size());
		for (ManaBurstEntity burst : bursts) {
			helper.assertTrue(burst.getColor() == SubspaceBursts.PLAYER_COLOR && burst.getMana() == 160
					&& Math.abs(burst.getDeltaMovement().length() - 1.8) < 1.0E-4, "A burst has the wrong color, mana or speed");
		}
		helper.assertTrue(find(helper, SubspaceSpearEntity.class).isEmpty(), "A domain rift shot a spear");
		bursts.forEach(Entity::discard);
		helper.succeed();
	}

	@GameTest(template = "projectile_arena", timeoutTicks = 20)
	public static void riftStateIsSavedAndSynchronized(GameTestHelper helper) {
		Player player = playerHolding(helper, ExtraBotanyItems.spearOfSubspace);
		SubspaceEntity rift = new SubspaceEntity(helper.getLevel(), player, SubspaceEntity.Mode.BURSTS, 120, 23, 13, 1.75F, 57F);

		SubspaceEntity loaded = ExtraBotanyEntityType.SUBSPACE.create(helper.getLevel());
		loaded.load(rift.saveWithoutId(new CompoundTag()));
		SubspaceEntity synced = ExtraBotanyEntityType.SUBSPACE.create(helper.getLevel());
		synced.getEntityData().assignValues(rift.getEntityData().getNonDefaultValues());

		for (SubspaceEntity copy : List.of(loaded, synced)) {
			helper.assertTrue(copy.getLiveTicks() == 120 && copy.getDelay() == 23 && copy.getSize() == 1.75F && copy.getRotation() == 57F,
					"A rift lost its state");
			copy.tickCount = 20;
			helper.assertTrue(copy.getRenderScale(0F) == 0F, "A rift is visible before its delay");
			copy.tickCount = 48;
			helper.assertTrue(copy.getRenderScale(0F) == 1.75F, "A rift did not open to its full size");
			copy.tickCount = 130;
			helper.assertTrue(copy.getRenderScale(0F) == 0F, "A rift did not close");
		}
		helper.succeed();
	}

	@GameTest(template = "projectile_arena", timeoutTicks = 20)
	public static void subspaceSpearDoesTrueAndAttackDamage(GameTestHelper helper) {
		Player player = playerHolding(helper, ExtraBotanyItems.spearOfSubspace);
		Vec3 pos = player.position().add(6, 0, 0);
		IronGolem golem = helper.spawnWithNoFreeWill(EntityType.IRON_GOLEM, new BlockPos(32, 4, 32));
		golem.setPos(pos);
		SubspaceSpearEntity spear = new SubspaceSpearEntity(helper.getLevel(), player, 12F, 100);
		spear.setPos(pos.add(0, 1, 0));
		helper.getLevel().addFreshEntity(spear);

		helper.runAfterDelay(3, () -> {
			// 真实伤害 12 × 0.4 = 4.8，攻击伤害 12 × 1.5 = 18。之后铁傀儡处在无敌帧里，所以只命中一次。
			// True damage 12 × 0.4 = 4.8, attack damage 12 × 1.5 = 18. After that the golem is in its invulnerability time, so there is only one hit.
			float lost = golem.getMaxHealth() - golem.getHealth();
			helper.assertTrue(Math.abs(lost - 22.8F) < 0.05F, "Expected 22.8 damage from one spear hit, found " + lost);
			helper.succeed();
		});
	}

	// ---------------------------------------------------------------------
	// 犹大的誓约 / Judah Oath
	// ---------------------------------------------------------------------

	@GameTest(template = "projectile_arena", timeoutTicks = 20)
	public static void judahVariantsGiveMainHandAttributes(GameTestHelper helper) {
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		player.setPos(helper.absoluteVec(ARENA_CENTER));
		player.setNoGravity(true);
		double baseDamage = player.getAttributeBaseValue(Attributes.ATTACK_DAMAGE);
		double baseSpeed = player.getAttributeBaseValue(Attributes.ATTACK_SPEED);
		for (Variant variant : Variant.values()) {
			ItemStack stack = boundStack(JudahOathItem.itemFor(variant), player);
			player.setItemInHand(InteractionHand.MAIN_HAND, stack);
			player.tick();
			helper.assertTrue(Math.abs(player.getAttributeValue(Attributes.ATTACK_DAMAGE) - (baseDamage + 12)) < 1.0E-6,
					variant + " does not give +12 attack damage in the main hand");
			helper.assertTrue(Math.abs(player.getAttributeValue(Attributes.ATTACK_SPEED) - (baseSpeed - 3.6)) < 1.0E-6,
					variant + " does not give -3.6 attack speed in the main hand");

			player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
			player.setItemInHand(InteractionHand.OFF_HAND, stack);
			player.tick();
			helper.assertTrue(Math.abs(player.getAttributeValue(Attributes.ATTACK_DAMAGE) - baseDamage) < 1.0E-6,
					variant + " gives attack damage from the off hand");
			player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
			player.tick();
		}
		helper.succeed();
	}

	@GameTest(template = "projectile_arena", timeoutTicks = 20)
	public static void judahVariantsShareOneCooldown(GameTestHelper helper) {
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		player.setPos(helper.absoluteVec(ARENA_CENTER));
		player.setNoGravity(true);
		ManaItem mana = supplyMana(player, 50000);
		int casts = 0;
		for (Variant variant : Variant.values()) {
			Item item = JudahOathItem.itemFor(variant);
			player.setItemInHand(InteractionHand.MAIN_HAND, boundStack(item, player));
			helper.assertTrue(item.use(helper.getLevel(), player, InteractionHand.MAIN_HAND).getResult().consumesAction(),
					variant + " could not be used after the cooldown ended");
			casts++;
			for (Variant other : Variant.values()) {
				Item otherItem = JudahOathItem.itemFor(other);
				helper.assertTrue(player.getCooldowns().isOnCooldown(otherItem), other + " is not on the shared cooldown");
				player.setItemInHand(InteractionHand.MAIN_HAND, boundStack(otherItem, player));
				helper.assertFalse(otherItem.use(helper.getLevel(), player, InteractionHand.MAIN_HAND).getResult().consumesAction(),
						other + " was used during the shared cooldown");
			}
			int ownedOaths = (int) find(helper, JudahOathEntity.class).stream().filter(oath -> oath.getOwner() == player).count();
			helper.assertTrue(mana.getMana() == 50000 - casts * 2500 && ownedOaths == casts,
					"A blocked use took mana or threw an oath");

			for (int tick = 0; tick < 79; tick++) {
				player.getCooldowns().tick();
			}
			helper.assertTrue(player.getCooldowns().isOnCooldown(item), "The shared cooldown ended before 80 ticks");
			player.getCooldowns().tick();
			helper.assertFalse(player.getCooldowns().isOnCooldown(item), "The shared cooldown was longer than 80 ticks");
		}
		find(helper, JudahOathEntity.class).forEach(Entity::discard);
		helper.succeed();
	}

	@GameTest(template = "projectile_arena", timeoutTicks = 30)
	public static void judahSpearHitsOneTargetAndStacksDivineJustice(GameTestHelper helper) {
		Player player = playerHolding(helper, ExtraBotanyItems.judahOath);
		Vec3 pos = player.position().add(8, 0, 0);
		Husk husk = husk(helper, pos);

		JudahSpearEntity fake = new JudahSpearEntity(helper.getLevel(), player, Variant.JUDAH, 7F, true);
		fake.setPos(pos.add(0, 3, 0));
		helper.getLevel().addFreshEntity(fake);
		JudahSpearEntity spear = new JudahSpearEntity(helper.getLevel(), player, Variant.JUDAH, 7F, false);
		spear.setPos(pos.add(0, 3, 0));
		helper.getLevel().addFreshEntity(spear);

		helper.runAfterDelay(4, () -> {
			MobEffectInstance effect = husk.getEffect(ExtraBotanyMobEffects.DIVINE_JUSTICE);
			helper.assertTrue(effect != null && effect.getAmplifier() == 0, "One spear hit did not give Divine Justice I");
			helper.assertTrue(Math.abs(husk.getMaxHealth() - 19F) < 1.0E-4F, "Divine Justice I did not remove 5% of the maximum health: " + husk.getMaxHealth());
			// 雷电伤害 7 × 1.6 = 11.2（尸壳的 2 点护甲会减免一点），真实伤害 7 × 0.15 = 1.05。
			// Lightning damage 7 × 1.6 = 11.2 (the 2 armor of a husk removes a small part), true damage 7 × 0.15 = 1.05.
			float lost = 20F - husk.getHealth();
			helper.assertTrue(lost > 11F && lost < 12.5F, "Unexpected damage from one spear: " + lost);

			husk.invulnerableTime = 0;
			JudahSpearEntity second = new JudahSpearEntity(helper.getLevel(), player, Variant.KIRA, 7F, false);
			second.setPos(husk.position().add(0, 3, 0));
			helper.getLevel().addFreshEntity(second);
		});
		helper.runAfterDelay(9, () -> {
			MobEffectInstance effect = husk.getEffect(ExtraBotanyMobEffects.DIVINE_JUSTICE);
			helper.assertTrue(effect != null && effect.getAmplifier() == 1, "A second spear hit did not raise Divine Justice to level II");
			helper.succeed();
		});
	}

	@GameTest(template = "projectile_arena", timeoutTicks = 20)
	public static void divineJusticeDoesTrueDamageNearItsEnd(GameTestHelper helper) {
		Husk husk = husk(helper, helper.absoluteVec(ARENA_CENTER).add(10, 0, 0));
		husk.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.DIVINE_JUSTICE, 7, 2));
		helper.assertTrue(Math.abs(husk.getMaxHealth() - 17F) < 1.0E-4F, "Divine Justice III did not remove 15% of the maximum health");

		helper.runAfterDelay(5, () -> {
			// 等级 III 的放大值是 2，所以真实伤害是 2 × 2 = 4。
			// Level III has an amplifier of 2, so the true damage is 2 × 2 = 4.
			float lost = 17F - husk.getHealth();
			helper.assertTrue(Math.abs(lost - 4.01F) < 0.02F, "Expected 4 true damage near the end of Divine Justice, found " + lost);
			helper.succeed();
		});
	}

	@GameTest(template = "projectile_arena", timeoutTicks = 20)
	public static void sakuraSwordFinisherOnlyHitsMobsOnItsPath(GameTestHelper helper) {
		Player player = playerHolding(helper, ExtraBotanyItems.judahOathSakura);
		Vec3 start = player.position().add(0, 4, 0);
		Vec3 end = start.add(10, 0, 10);
		JudahSwordEntity sword = new JudahSwordEntity(helper.getLevel(), player, start, end, 6F);

		// 客户端只收到同步数据。它必须能靠同步数据让剑前进。
		// The client only gets the synchronized data. It must be able to move the sword from that data.
		JudahSwordEntity mirror = ExtraBotanyEntityType.JUDAH_SWORD.create(helper.getLevel());
		mirror.setPos(start);
		mirror.getEntityData().assignValues(sword.getEntityData().getNonDefaultValues());
		mirror.tick();
		helper.assertTrue(Math.abs(mirror.position().distanceTo(start) - 0.75) < 1.0E-6, "The sword does not move from its synchronized data");

		Husk onPath = husk(helper, start.add(5, 0, 5));
		Husk offPath = husk(helper, start.add(1, 0, 9));
		sword.tickCount = 81;
		sword.tick();
		helper.assertTrue(onPath.getHealth() < onPath.getMaxHealth(), "The finisher did not hit a mob on the path");
		helper.assertTrue(offPath.getHealth() == offPath.getMaxHealth(), "The finisher hit a mob that is not on the path");
		helper.assertTrue(onPath.isOnFire(), "The finisher did not set the mob on fire");
		helper.succeed();
	}
}
