package io.github.lounode.extrabotany.forge.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import top.theillusivec4.curios.api.CuriosApi;

import vazkii.botania.api.item.Relic;

import io.github.lounode.extrabotany.common.entity.KeyOfTruthEntity;
import io.github.lounode.extrabotany.common.entity.mount.MotorEntity;
import io.github.lounode.extrabotany.common.entity.mount.MountEntity;
import io.github.lounode.extrabotany.common.entity.mount.UfoEntity;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.network.serverbound.MountSkillPacket;

import java.util.List;
import java.util.UUID;

/**
 * 机车、飞碟和真理之钥的回归测试。
 * Regression tests for the Armored Motor, the Cosmic Car and the Key of Truth.
 */
@GameTestHolder("extrabotany")
@PrefixGameTestTemplate(false)
public final class MountGameTests {
	private static final int FLOOR_Y = 1;

	private MountGameTests() {}

	/**
	 * 在测试区域里铺一层石头地板。实体站在地板上，y 坐标是 2。
	 * Puts a stone floor into the test area. Entities stand on the floor at y = 2.
	 */
	private static void buildFloor(GameTestHelper helper) {
		for (int x = 0; x < 5; x++) {
			for (int z = 0; z < 5; z++) {
				helper.setBlock(new BlockPos(x, FLOOR_Y, z), Blocks.STONE);
			}
		}
	}

	private static Vec3 pos(GameTestHelper helper, double x, double y, double z) {
		return helper.absoluteVec(new Vec3(x, y, z));
	}

	private static Player player(GameTestHelper helper, double x, double y, double z) {
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		player.setPos(pos(helper, x, y, z));
		return player;
	}

	private static <T extends Entity> List<T> find(GameTestHelper helper, Class<T> type) {
		return helper.getLevel().getEntitiesOfClass(type, new AABB(helper.absolutePos(new BlockPos(2, 2, 2))).inflate(8));
	}

	/**
	 * 生成一只不会移动的尸壳。尸壳是怪物，并且不会在阳光下燃烧，所以测试里的伤害数值是确定的。
	 * Spawns a husk that does not move. A husk is a monster and does not burn in sunlight, so the damage values in the tests are deterministic.
	 */
	private static Husk husk(GameTestHelper helper, double x, double z) {
		Husk husk = helper.spawnWithNoFreeWill(EntityType.HUSK, new BlockPos(2, 2, 2));
		husk.setPos(pos(helper, x, 2, z));
		return husk;
	}

	private static ItemStack equip(Player player, Item bauble) {
		ItemStack stack = new ItemStack(bauble);
		Relic relic = Relic.LOOKUP.find(stack);
		if (relic != null) {
			relic.bindToUUID(player.getUUID());
		}
		var inventory = CuriosApi.getCuriosInventory(player).orElseThrow();
		var handler = inventory.getCurios().values().stream()
				.filter(candidate -> candidate.getStacks().getSlots() > 0)
				.findFirst().orElseThrow();
		handler.getStacks().setStackInSlot(0, stack);
		return stack;
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void motorItemPlacesMotorAndKeepsItsOwner(GameTestHelper helper) {
		buildFloor(helper);
		Player player = player(helper, 2.5, 2, 2.5);
		player.setXRot(90F);
		UUID owner = UUID.randomUUID();
		ItemStack stack = new ItemStack(ExtraBotanyItems.motor);
		Relic.LOOKUP.find(stack).bindToUUID(owner);
		player.setItemInHand(InteractionHand.MAIN_HAND, stack);

		stack.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
		List<MotorEntity> motors = find(helper, MotorEntity.class);
		helper.assertTrue(motors.size() == 1, "Expected 1 motor, found " + motors.size());
		helper.assertTrue(player.getMainHandItem().isEmpty(), "The motor item was not used up");
		MotorEntity motor = motors.get(0);
		helper.assertTrue(owner.equals(motor.getOwnerId()), "The motor did not keep the player that the item is bound to");

		// 不是玩家造成的伤害不会拆下机车。
		// Damage that does not come from a player does not take the motor apart.
		motor.hurt(helper.getLevel().damageSources().generic(), 100F);
		helper.assertTrue(motor.isAlive(), "Generic damage took the motor apart");

		motor.hurt(helper.getLevel().damageSources().playerAttack(player), 5F);
		helper.assertTrue(motor.isRemoved(), "A player attack of 5 damage did not take the motor apart");
		List<ItemEntity> drops = find(helper, ItemEntity.class);
		helper.assertTrue(drops.size() == 1 && drops.get(0).getItem().is(ExtraBotanyItems.motor), "The motor did not drop its item");
		helper.assertTrue(owner.equals(Relic.LOOKUP.find(drops.get(0).getItem()).getSoulbindUUID()),
				"The dropped motor item is not bound to its owner");
		helper.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 40)
	public static void summonedMotorGoesAwayWithoutRider(GameTestHelper helper) {
		buildFloor(helper);
		Player player = player(helper, 2.5, 2, 2.5);
		helper.assertFalse(MountSkillPacket.activate(player), "A mount was summoned without an accessory");

		equip(player, ExtraBotanyItems.motorAccessory);
		helper.runAfterDelay(2, () -> {
			helper.assertTrue(MountSkillPacket.activate(player), "The accessory did not summon a mount");
			helper.assertTrue(player.getVehicle() instanceof MotorEntity, "The player does not ride a motor");
			MotorEntity motor = (MotorEntity) player.getVehicle();
			helper.assertTrue(motor.isSummoned(), "The motor is not marked as summoned");
			helper.assertFalse(MountSkillPacket.activate(player), "A second mount was summoned while the player rides");

			// 被召唤的机车不掉落物品。
			// A summoned motor drops no item.
			helper.runAfterDelay(6, () -> {
				helper.assertTrue(motor.isAlive(), "The summoned motor went away while the player rides it");
				player.stopRiding();
				helper.runAfterDelay(3, () -> {
					helper.assertTrue(motor.isRemoved(), "The summoned motor stayed after the player got off");
					helper.assertTrue(find(helper, ItemEntity.class).isEmpty(), "The summoned motor dropped an item");
					helper.succeed();
				});
			});
		});
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void motorCollisionDoesIceDamage(GameTestHelper helper) {
		buildFloor(helper);
		Player player = player(helper, 2.5, 2, 2.5);
		MotorEntity motor = new MotorEntity(helper.getLevel(), pos(helper, 2.5, 2, 2.5));
		helper.getLevel().addFreshEntity(motor);
		player.startRiding(motor, true);
		Husk husk = husk(helper, 3.2, 2.5);

		helper.runAfterDelay(4, () -> {
			float lost = husk.getMaxHealth() - husk.getHealth();
			helper.assertTrue(Math.abs(lost - 7F) < 1.0E-4F, "Expected 7 ice damage from the collision, found " + lost);
			helper.assertTrue(player.getLastHurtMob() == husk, "The collision did not record the last attacked mob");
			helper.succeed();
		});
	}

	@GameTest(template = "empty", timeoutTicks = 200)
	public static void motorChargesEnergyAndUsesTheCyclone(GameTestHelper helper) {
		buildFloor(helper);
		Player player = player(helper, 2.5, 2, 2.5);
		MotorEntity motor = new MotorEntity(helper.getLevel(), pos(helper, 2.5, 2, 2.5));
		helper.getLevel().addFreshEntity(motor);
		player.startRiding(motor, true);
		// 尸壳离机车 1.9 格：在旋风的范围内，但是碰不到机车。
		// The husk is 1.9 blocks from the motor: in the range of the cyclone, but it does not touch the motor.
		Husk husk = husk(helper, 4.4, 2.5);

		helper.runAfterDelay(100, () -> {
			helper.assertTrue(motor.getEnergy() == 0, "The motor got energy before 120 riding ticks: " + motor.getEnergy());
			helper.assertTrue(husk.getHealth() == husk.getMaxHealth(), "The husk took damage before the motor was charged");
		});
		helper.runAfterDelay(130, () -> {
			int energy = motor.getEnergy();
			helper.assertTrue(energy > 0 && energy <= 30, "Unexpected energy after 130 ticks: " + energy);
			motor.setEnergy(MotorEntity.MAX_ENERGY);
			motor.setInput(MountEntity.INPUT_SPRINT);
		});
		helper.runAfterDelay(133, () -> {
			helper.assertTrue(motor.getCycloneTicks() > 0, "The sprint key did not start the cyclone");
			int energy = motor.getEnergy();
			helper.assertTrue(energy >= 400 && energy <= 410, "The cyclone did not take 400 energy: " + energy);
			motor.setInput(0);
		});
		helper.runAfterDelay(140, () ->
				helper.assertFalse(find(helper, KeyOfTruthEntity.class).isEmpty(), "The charged motor summoned no Key of Truth"));
		helper.runAfterDelay(160, () -> {
			helper.assertTrue(motor.getCycloneTicks() == 0, "The cyclone did not end");
			helper.assertTrue(husk.getHealth() < husk.getMaxHealth(), "The cyclone and the keys did no damage");
			helper.succeed();
		});
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void motorDashNeedsEnergy(GameTestHelper helper) {
		buildFloor(helper);
		Player player = player(helper, 2.5, 2, 2.5);
		MotorEntity motor = new MotorEntity(helper.getLevel(), pos(helper, 2.5, 2, 2.5));
		helper.getLevel().addFreshEntity(motor);
		player.startRiding(motor, true);

		// 骑乘不满 120 tick 时，跳跃键不消耗能量。
		// Before 120 riding ticks, the jump key takes no energy.
		motor.setEnergy(500);
		motor.setInput(MountEntity.INPUT_JUMP);
		helper.runAfterDelay(5, () -> {
			helper.assertTrue(motor.getEnergy() == 500, "The jump key took energy before the motor was charged: " + motor.getEnergy());
			helper.succeed();
		});
	}

	@GameTest(template = "empty", timeoutTicks = 60)
	public static void ufoCatchesAndReleasesTheMobBelow(GameTestHelper helper) {
		buildFloor(helper);
		Player player = player(helper, 2.5, 4.5, 2.5);
		UfoEntity ufo = new UfoEntity(helper.getLevel(), pos(helper, 2.5, 4.5, 2.5));
		helper.getLevel().addFreshEntity(ufo);
		player.startRiding(ufo, true);
		// 这只猪保留 AI。没有 AI 的生物不会按速度移动，所以飞碟拉不动它。
		// This pig keeps its AI. A mob with no AI does not move with its velocity, so the car cannot pull it.
		Pig pig = helper.spawn(EntityType.PIG, new BlockPos(2, 2, 2));
		pig.setPos(pos(helper, 1.2, 2, 2.5));

		helper.runAfterDelay(2, () -> {
			helper.assertTrue(ufo.isNoGravity() && Math.abs(ufo.getY() - pos(helper, 0, 4.5, 0).y) < 0.01, "The Cosmic Car fell");
			helper.assertTrue(MountSkillPacket.activate(player), "The mount key did nothing on the Cosmic Car");
			helper.assertTrue(ufo.getCaughtId() == pig.getId(), "The Cosmic Car did not catch the pig below");
		});
		helper.runAfterDelay(14, () -> {
			double distance = Math.hypot(pig.getX() - ufo.getX(), pig.getZ() - ufo.getZ());
			helper.assertTrue(distance < 0.8, "The pig was not pulled below the Cosmic Car: " + distance);
			MountSkillPacket.activate(player);
			helper.assertTrue(ufo.getCaughtId() == -1, "The second push of the mount key did not release the pig");
		});
		helper.runAfterDelay(30, () -> {
			helper.assertTrue(pig.onGround(), "The released pig did not fall back to the ground");
			helper.succeed();
		});
	}

	@GameTest(template = "empty", timeoutTicks = 80)
	public static void keyOfTruthDamagesItsTargetAndExpires(GameTestHelper helper) {
		buildFloor(helper);
		Player player = player(helper, 0.5, 2, 0.5);
		Husk husk = husk(helper, 3.5, 3.5);
		KeyOfTruthEntity key = new KeyOfTruthEntity(helper.getLevel(), player);
		key.setPos(pos(helper, 2.5, 4, 2.5));
		helper.getLevel().addFreshEntity(key);

		helper.runAfterDelay(4, () -> {
			helper.assertTrue(key.getTargetId() == husk.getId(), "The key did not select the monster as its target");
			float lost = husk.getMaxHealth() - husk.getHealth();
			helper.assertTrue(Math.abs(lost - 4F) < 1.0E-4F, "Expected 4 ice damage in the first ticks, found " + lost);
		});
		helper.runAfterDelay(50, () -> {
			helper.assertTrue(key.isRemoved(), "The key did not go away after 45 ticks");
			helper.succeed();
		});
	}
}
