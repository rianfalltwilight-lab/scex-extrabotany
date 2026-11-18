package io.github.lounode.extrabotany.forge.gametest;

import com.mojang.authlib.GameProfile;
import io.github.lounode.extrabotany.common.entity.ExtraBotanyEntityType;
import io.github.lounode.extrabotany.common.entity.gaia.Ego;
import io.github.lounode.extrabotany.common.entity.gaia.Gaia;
import io.github.lounode.extrabotany.common.entity.gaia.VoidHerrscher;
import io.github.lounode.extrabotany.common.entity.projectile.PhantomSwordEntity;
import io.github.lounode.extrabotany.common.handler.EgoRequirementHandler;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.forge.ForgeExtrabotanyConfig;
import io.github.lounode.extrabotany.network.serverbound.LeftClickEmptyPacket;
import io.github.lounode.extrabotany.xplat.ExtraBotanyConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.item.Relic;
import vazkii.botania.common.helper.PlayerHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/** Exercises progression with survival ServerPlayers; the two boss criteria are never manually awarded. */
@GameTestHolder("extrabotany")
@PrefixGameTestTemplate(false)
public final class FirstFractalGameTests {

	private static final ResourceLocation HERRSCHER = prefix("main/herrscher_defeat");
	private static final BlockPos CENTER = new BlockPos(32, 4, 32);

	@GameTest(template = "projectile_arena", timeoutTicks = 20)
	public static void bindingTheFractalDoesNotGrantBossProgress(GameTestHelper h) {
		try (Fixture fixture = new Fixture(h, false)) {
			TestPlayer player = fixture.player();
			ItemStack stack = fixture.bindFractal(player);
			h.assertTrue(Relic.LOOKUP.find(stack).isRightPlayer(player), "Inventory tick did not bind the relic");
			h.assertTrue(HERRSCHER.equals(Relic.LOOKUP.find(stack).getAdvancement()), "Relic metadata changed");
			h.assertTrue(!has(player, HERRSCHER) && !has(player, EgoRequirementHandler.ADVANCEMENT),
					"Binding a relic must not award either boss kill advancement");
		}
		h.succeed();
	}

	@GameTest(template = "projectile_arena", timeoutTicks = 20)
	public static void herrscherKillerUnlocksTheActualEmptySwingSkill(GameTestHelper h) {
		try (Fixture fixture = new Fixture(h, false)) {
			TestPlayer player = fixture.player();
			fixture.bindFractal(player);
			VoidHerrscher boss = fixture.herrscher();
			fixture.finishBoss(boss, player);
			h.assertTrue(has(player, HERRSCHER), "Actual Herrscher death did not award the killer's advancement");
			h.assertTrue(!has(player, EgoRequirementHandler.ADVANCEMENT), "Herrscher death must not award the Ego criterion");
			// Minecraft.startAttack fires LeftClickEmpty before LocalPlayer.swing sends the animation packet.
			h.assertTrue(player.getAttackStrengthScale(0) == 1, "The skill fixture did not start with a full attack cooldown");
			LeftClickEmptyPacket.INSTANCE.handle(player);
			fixture.assertFourSwords(player);
			h.assertTrue(player.getAttackStrengthScale(0) == 0, "Successful empty swing did not consume the cooldown");
			// A replay must fail even before the vanilla animation reaches ServerPlayer.swing.
			LeftClickEmptyPacket.INSTANCE.handle(player);
			fixture.assertFourSwords(player);
			player.connection.handleAnimate(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
			h.assertTrue(player.getAttackStrengthScale(0) == 0, "Swing animation unexpectedly restored the attack cooldown");
			fixture.assertFourSwords(player);
			LeftClickEmptyPacket.INSTANCE.handle(player);
			fixture.assertFourSwords(player);
		}
		h.succeed();
	}

	@GameTest(template = "projectile_arena", timeoutTicks = 20)
	public static void herrscherContributorUnlocksTheActualEntityAttackSkill(GameTestHelper h) {
		try (Fixture fixture = new Fixture(h, false)) {
			TestPlayer contributor = fixture.player();
			TestPlayer killer = fixture.player();
			ItemStack stack = fixture.bindFractal(contributor);
			VoidHerrscher boss = fixture.herrscher();
			h.assertTrue(boss.hurt(contributor.damageSources().playerAttack(contributor), 1), "Contributor did not land a real hit");
			h.assertTrue(boss.getPlayersWhoAttacked().contains(contributor.getUUID()) && !has(contributor, HERRSCHER),
					"A living boss must record participation without granting the kill advancement");
			fixture.finishBoss(boss, killer);
			h.assertTrue(has(contributor, HERRSCHER) && has(killer, HERRSCHER),
					"Actual death did not grant both killer and contributor progression");

			Zombie target = new Zombie(EntityType.ZOMBIE, h.getLevel());
			target.setNoAi(true);
			target.setPos(contributor.position().add(0, 0, 3));
			try {
				h.assertTrue(h.getLevel().addFreshEntity(target), "Failed to add the attack target");
				contributor.attack(target); // Fires the real NeoForge AttackEntityEvent before vanilla resets cooldown.
				fixture.assertFourSwords(contributor);
				h.assertTrue(contributor.getAttackStrengthScale(0) == 0, "Entity attack did not consume its normal cooldown");
			} finally {
				target.discard();
			}
		}
		h.succeed();
	}

	@GameTest(template = "projectile_arena", timeoutTicks = 20)
	public static void disabledEgoRuleKeepsTheFractalButStillRequiresHerrscherProgress(GameTestHelper h) {
		try (Fixture fixture = new Fixture(h, false)) {
			TestPlayer player = fixture.player();
			ItemStack stack = fixture.bindFractal(player);
			ItemStack before = stack.copy();
			EgoRequirementHandler.tickPlayer(player);
			h.assertTrue(!EgoRequirementHandler.isRestricted(stack) && ItemStack.matches(player.getMainHandItem(), before),
					"Disabled optional Ego rule dropped or changed First Fractal");
			LeftClickEmptyPacket.INSTANCE.handle(player);
			h.assertTrue(fixture.swords(player).isEmpty() && player.getAttackStrengthScale(0) == 1,
					"Missing Herrscher progression must refuse the skill without consuming cooldown");
			h.assertTrue(player.lastMessage != null && player.lastMessage.getContents() instanceof TranslatableContents text
					&& text.getKey().equals("extrabotany.message.advancement_required"), "Wrong message for the Herrscher gate");
		}
		h.succeed();
	}

	@GameTest(template = "projectile_arena", timeoutTicks = 20)
	public static void enabledEgoRuleDropsTheFractalUntilAnActualEgoKill(GameTestHelper h) {
		try (Fixture fixture = new Fixture(h, true)) {
			TestPlayer player = fixture.player();
			ItemStack stack = fixture.bindFractal(player);
			ItemStack before = stack.copy();
			h.assertTrue(EgoRequirementHandler.isForbidden(player, stack), "Fixture did not activate the optional Ego gate");
			EgoRequirementHandler.tickPlayer(player);
			List<ItemEntity> drops = h.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(player.blockPosition()).inflate(3),
					entity -> ItemStack.matches(entity.getItem(), before));
			try {
				h.assertTrue(player.getMainHandItem().isEmpty() && drops.size() == 1,
						"Missing Ego progress must cause exactly one complete, component-preserving drop");
			} finally {
				drops.forEach(ItemEntity::discard);
			}
			fixture.finishBoss(fixture.ego(), player);
			h.assertTrue(has(player, EgoRequirementHandler.ADVANCEMENT), "Actual Ego death did not award its progression");
			player.setItemInHand(InteractionHand.MAIN_HAND, before.copy());
			EgoRequirementHandler.tickPlayer(player);
			h.assertTrue(!EgoRequirementHandler.isForbidden(player, player.getMainHandItem())
					&& ItemStack.matches(player.getMainHandItem(), before), "Ego winner still loses the bound First Fractal");
			h.assertTrue(!has(player, HERRSCHER), "Ego death incorrectly bypassed the independent Herrscher requirement");
			LeftClickEmptyPacket.INSTANCE.handle(player);
			h.assertTrue(fixture.swords(player).isEmpty(), "Ego progression alone must not unlock the Herrscher skill");
		}
		h.succeed();
	}

	private static boolean has(ServerPlayer player, ResourceLocation id) {
		return PlayerHelper.hasAdvancement(player, id);
	}

	private static final class Fixture implements AutoCloseable {
		private final GameTestHelper helper;
		private final boolean originalRule = ExtraBotanyConfig.common().relicsNeedEgoDefeat();
		private final List<TestPlayer> players = new ArrayList<>();
		private final List<Gaia> bosses = new ArrayList<>();
		private final List<ItemEntity> deathDrops = new ArrayList<>();

		Fixture(GameTestHelper helper, boolean egoRule) {
			this.helper = helper;
			helper.setBlock(CENTER.below(), Blocks.STONE);
			ForgeExtrabotanyConfig.setRelicsNeedEgoDefeat(egoRule);
		}

		TestPlayer player() {
			TestPlayer player = new TestPlayer(helper);
			player.setPos(helper.absolutePos(CENTER).getCenter());
			player.setXRot(90);
			// Gaia's contributor lookup uses ServerLevel.players(), not the network player list.
			// Keep this synchronous fixture out of login/chunk tracking; outbound packets are discarded below.
			helper.getLevel().players().add(player);
			players.add(player);
			helper.assertTrue(!player.isCreative() && !player.isSpectator() && PlayerHelper.isTruePlayer(player),
					"Progression fixture must be a true survival ServerPlayer");
			return player;
		}

		ItemStack bindFractal(TestPlayer player) {
			ItemStack stack = new ItemStack(ExtraBotanyItems.firstFractal);
			CompoundTag data = new CompoundTag();
			data.putUUID("firstFractalFixture", player.getUUID());
			stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
			player.setItemInHand(InteractionHand.MAIN_HAND, stack);
			stack.getItem().inventoryTick(stack, helper.getLevel(), player, 0, true);
			return stack;
		}

		VoidHerrscher herrscher() {
			VoidHerrscher boss = new VoidHerrscher(ExtraBotanyEntityType.VOID_HERRSCHER, helper.getLevel());
			prepare(boss);
			CompoundTag data = boss.saveWithoutId(new CompoundTag());
			data.putInt("ShieldLayers", 0);
			data.putInt("RotatingShields", 0);
			data.putInt("DodgeCd", 100);
			boss.load(data);
			return boss;
		}

		Ego ego() {
			Ego boss = new Ego(ExtraBotanyEntityType.EGO, helper.getLevel());
			prepare(boss);
			return boss;
		}

		private void prepare(Gaia boss) {
			boss.setPos(helper.absolutePos(CENTER).getCenter().add(0, 0, 5));
			boss.setNoAi(true);
			CompoundTag data = boss.saveWithoutId(new CompoundTag());
			data.put("Home", GlobalPos.CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE,
					GlobalPos.of(helper.getLevel().dimension(), helper.absolutePos(CENTER))).getOrThrow());
			boss.load(data);
			boss.setInvulTime(0);
			bosses.add(boss);
		}

		void finishBoss(Gaia boss, TestPlayer killer) {
			CompoundTag data = boss.saveWithoutId(new CompoundTag());
			data.putFloat("Health", 1);
			boss.load(data);
			boss.setInvulTime(0);
			boss.invulnerableTime = 0;
			helper.assertTrue(boss.isAlive() && boss.getHealth() == 1, "Fixture boss was not alive before the final hit");
			AABB area = new AABB(helper.absolutePos(CENTER)).inflate(16);
			List<ItemEntity> before = helper.getLevel().getEntitiesOfClass(ItemEntity.class, area);
			boolean damaged;
			try {
				damaged = boss.hurt(killer.damageSources().playerAttack(killer), 10);
			} finally {
				deathDrops.addAll(helper.getLevel().getEntitiesOfClass(ItemEntity.class, area,
						item -> !before.contains(item)));
			}
			helper.assertTrue(damaged && boss.isDeadOrDying(),
					"Final player hit did not kill the actual boss");
		}

		List<PhantomSwordEntity> swords(TestPlayer player) {
			return helper.getLevel().getEntitiesOfClass(PhantomSwordEntity.class,
					new AABB(player.blockPosition()).inflate(32), sword -> sword.getOwner() == player && !sword.isFake());
		}

		void assertFourSwords(TestPlayer player) {
			List<PhantomSwordEntity> swords = swords(player);
			helper.assertTrue(swords.size() == 4, "Unlocked skill did not spawn four actual swords: " + swords.size());
			helper.assertTrue(swords.stream().map(PhantomSwordEntity::getDelay).sorted().toList().equals(List.of(0, 5, 10, 15)),
					"The four-sword sequence has incorrect delays");
		}

		@Override
		public void close() {
			try {
				for (TestPlayer player : players) {
					swords(player).forEach(PhantomSwordEntity::discard);
					helper.getLevel().players().remove(player);
					player.getTextFilter().leave();
				}
				bosses.forEach(Gaia::discard);
				deathDrops.forEach(ItemEntity::discard);
			} finally {
				ForgeExtrabotanyConfig.setRelicsNeedEgoDefeat(originalRule);
			}
		}
	}

	private static final class TestPlayer extends ServerPlayer {
		@Nullable private Component lastMessage;

		TestPlayer(GameTestHelper helper) {
			super(helper.getLevel().getServer(), helper.getLevel(),
					new GameProfile(UUID.randomUUID(), "eb-fractal"), ClientInformation.createDefault());
			attackStrengthTicker = 100;
			new ServerGamePacketListenerImpl(helper.getLevel().getServer(), new Connection(PacketFlow.SERVERBOUND), this,
					CommonListenerCookie.createInitial(getGameProfile(), false)) {
				@Override public void send(Packet<?> packet) {}
				@Override public void send(Packet<?> packet, @Nullable PacketSendListener listener) {}
			};
		}

		@Override
		public void displayClientMessage(Component message, boolean actionBar) {
			lastMessage = message;
		}
	}
}
