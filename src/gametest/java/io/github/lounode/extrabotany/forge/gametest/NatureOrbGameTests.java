package io.github.lounode.extrabotany.forge.gametest;

import com.mojang.authlib.GameProfile;
import io.github.lounode.extrabotany.api.item.NatureEnergyItem;
import io.github.lounode.extrabotany.common.helper.ItemNBTHelper;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.item.equipment.bauble.NatureOrbItem;
import io.github.lounode.extrabotany.xplat.EXplatAbstractions;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import vazkii.botania.api.mana.ManaItem;
import vazkii.botania.common.item.BotaniaItems;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Covers the worn callback and actual Curios wire codec, without claiming a live connection test. */
@GameTestHolder("extrabotany")
@PrefixGameTestTemplate(false)
public final class NatureOrbGameTests {

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void chargedOrbWornFor120TicksGeneratesHealsAndCleanses(GameTestHelper h) {
		try (Fixture f = new Fixture(h, NatureOrbItem.MAX_ENERGY)) {
			f.player.setHealth(10);
			f.player.addEffect(new MobEffectInstance(MobEffects.POISON, 1000));
			f.player.addEffect(new MobEffectInstance(MobEffects.WITHER, 1000));
			f.player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 1000));
			for (int tick = 1; tick <= 120; tick++) {
				f.tick(tick);
				h.assertTrue(f.mana.getMana() == tick * 3, "Incorrect mana generation at worn tick " + tick);
				h.assertTrue(f.player.getHealth() == 10 + tick / 60, "Incorrect healing interval at worn tick " + tick);
				h.assertTrue(f.player.hasEffect(MobEffects.POISON) == (tick < 40)
						&& f.player.hasEffect(MobEffects.WITHER) == (tick < 40), "Incorrect harmful-effect removal at worn tick " + tick);
				h.assertTrue(f.player.hasEffect(MobEffects.DIG_SPEED), "The orb removed a beneficial effect");
				h.assertTrue(f.energy.getEnergy() == NatureOrbItem.MAX_ENERGY - (tick < 40 ? 0 : 2 * NatureOrbItem.REMOVE_HARMFUL_POTION_COST),
						"Energy changed without removing a harmful effect at worn tick " + tick);
			}
			var removed = f.player.packets.stream().filter(ClientboundRemoveMobEffectPacket.class::isInstance)
					.map(ClientboundRemoveMobEffectPacket.class::cast).toList();
			h.assertTrue(removed.size() == 2 && removed.stream().anyMatch(p -> p.effect().equals(MobEffects.POISON))
					&& removed.stream().anyMatch(p -> p.effect().equals(MobEffects.WITHER)),
					"The ServerPlayer did not send exactly the two actual effect-removal packets");
			for (var packet : removed) {
				RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), h.getLevel().registryAccess());
				try {
					ClientboundRemoveMobEffectPacket.STREAM_CODEC.encode(buf, packet);
					h.assertTrue(packet.equals(ClientboundRemoveMobEffectPacket.STREAM_CODEC.decode(buf)) && !buf.isReadable(),
							"Effect removal packet did not survive its real wire codec");
				} finally {
					buf.release();
				}
			}
		}
		h.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void curioSyncPreservesComponentsBeforeAndAfterEnergyConsumption(GameTestHelper h) throws ReflectiveOperationException {
		try (Fixture f = new Fixture(h, NatureOrbItem.TIER_4_ENERGY + 1)) {
			f.orb.set(DataComponents.CUSTOM_NAME, Component.literal("Nature Orb sync fixture"));
			ItemNBTHelper.setString(f.orb, "sync_fixture", f.player.getUUID().toString());
			assertCuriosRoundTrip(h, f);
			f.player.addEffect(new MobEffectInstance(MobEffects.POISON, 1000));
			f.tick(40);
			h.assertTrue(!f.player.hasEffect(MobEffects.POISON)
					&& f.energy.getEnergy() == NatureOrbItem.TIER_4_ENERGY + 1 - NatureOrbItem.REMOVE_HARMFUL_POTION_COST,
					"Fixture did not exercise the energy component mutation");
			h.assertTrue(NatureOrbItem.getTier(f.orb) == 3, "Energy consumption did not cross the tier boundary");
			h.assertTrue(ItemNBTHelper.getString(f.orb, "sync_fixture", "").equals(f.player.getUUID().toString()),
					"Energy mutation lost unrelated custom data");
			assertCuriosRoundTrip(h, f);
		}
		h.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void emptyOrbWornFor120TicksDoesNotMutateOrAct(GameTestHelper h) {
		try (Fixture f = new Fixture(h, 0)) {
			f.player.setHealth(10);
			f.player.addEffect(new MobEffectInstance(MobEffects.POISON, 1000));
			ItemStack original = f.orb.copy();
			for (int tick = 1; tick <= 120; tick++) {
				f.tick(tick);
			}
			h.assertTrue(f.mana.getMana() == 0 && f.player.getHealth() == 10 && f.player.hasEffect(MobEffects.POISON),
					"An empty orb generated mana, healed, or removed an effect");
			h.assertTrue(f.energy.getEnergy() == 0 && ItemStack.matches(original, f.orb),
					"An empty orb gained energy or rewrote its components while worn");
			h.assertTrue(f.player.packets.stream().noneMatch(ClientboundRemoveMobEffectPacket.class::isInstance),
					"An empty orb sent an effect-removal packet");
		}
		h.succeed();
	}

	@SuppressWarnings("unchecked")
	private static void assertCuriosRoundTrip(GameTestHelper h, Fixture f) throws ReflectiveOperationException {
		// Only the Curios API is on the compile classpath. Resolve its installed runtime packet
		// instead of copying its codec or adding an implementation dependency to the mod.
		Class<?> packetClass = Class.forName("top.theillusivec4.curios.common.network.server.sync.SPacketSyncStack");
		StreamCodec<RegistryFriendlyByteBuf, Object> codec =
				(StreamCodec<RegistryFriendlyByteBuf, Object>) packetClass.getField("STREAM_CODEC").get(null);
		Object packet = packetClass.getConstructor(int.class, String.class, int.class, ItemStack.class, int.class, CompoundTag.class)
				.newInstance(f.player.getId(), f.context.identifier(), 0, f.orb.copy(), 0, f.curio.writeSyncData(f.context));
		RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), h.getLevel().registryAccess());
		try {
			codec.encode(buf, packet);
			Object decoded = codec.decode(buf);
			ItemStack received = (ItemStack) packetClass.getMethod("stack").invoke(decoded);
			h.assertTrue(!buf.isReadable() && ItemStack.matches(f.orb, received),
					"Curios sync_stack changed the orb count or data components");
			h.assertTrue(EXplatAbstractions.INSTANCE.findNatureEnergyItem(received).getEnergy() == f.energy.getEnergy(),
					"Received stack capability did not expose the synchronized Nature Energy");
		} finally {
			buf.release();
		}
	}

	private static final class Fixture implements AutoCloseable {
		private final TestPlayer player;
		private final ItemStack orb = new ItemStack(ExtraBotanyItems.natureOrb);
		private final NatureEnergyItem energy = EXplatAbstractions.INSTANCE.findNatureEnergyItem(orb);
		private final IDynamicStackHandler slots;
		private final SlotContext context;
		private final ICurio curio;
		private final ManaItem mana;

		Fixture(GameTestHelper h, long amount) {
			player = new TestPlayer(h);
			player.setPos(h.absolutePos(new BlockPos(2, 2, 2)).getCenter());
			if (amount > 0) energy.addEnergy(amount);
			var slot = CuriosApi.getCuriosInventory(player).orElseThrow().getCurios().entrySet().stream()
					.filter(e -> e.getValue().getStacks().getSlots() > 0).findFirst().orElseThrow();
			slots = slot.getValue().getStacks();
			slots.setStackInSlot(0, orb);
			context = new SlotContext(slot.getKey(), player, 0, false, true);
			curio = orb.getCapability(CuriosCapability.ITEM);
			h.assertTrue(curio != null && curio.getStack() == orb, "Missing the real Botania Curios wrapper");
			curio.onEquip(context, ItemStack.EMPTY);
			ItemStack tablet = new ItemStack(BotaniaItems.MANA_TABLET);
			mana = ManaItem.LOOKUP.find(tablet);
			player.getInventory().setItem(0, tablet);
		}

		void tick(int tick) {
			// Advance only the accessory callback so vanilla regeneration and poison damage
			// cannot masquerade as Nature Orb behavior. This is not a wall-clock endurance test.
			player.tickCount = tick;
			curio.curioTick(context);
		}

		@Override
		public void close() {
			curio.onUnequip(context, ItemStack.EMPTY);
			slots.setStackInSlot(0, ItemStack.EMPTY);
			player.getTextFilter().leave();
		}
	}

	private static final class TestPlayer extends ServerPlayer {
		private final List<Packet<?>> packets = new ArrayList<>();

		TestPlayer(GameTestHelper h) {
			super(h.getLevel().getServer(), h.getLevel(), new GameProfile(UUID.randomUUID(), "eb-nature-orb"), ClientInformation.createDefault());
			new ServerGamePacketListenerImpl(h.getLevel().getServer(), new Connection(PacketFlow.SERVERBOUND), this,
					CommonListenerCookie.createInitial(getGameProfile(), false)) {
				@Override public void send(Packet<?> packet) { packets.add(packet); }
				@Override public void send(Packet<?> packet, @Nullable PacketSendListener listener) { packets.add(packet); }
			};
		}
	}
}
