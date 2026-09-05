package io.github.lounode.extrabotany.fabric.xplat;

import com.mojang.authlib.GameProfile;

import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.common.helper.ItemNBTHelper;
import vazkii.botania.fabric.xplat.FabricXplatImpl;

import io.github.lounode.extrabotany.api.ExtrabotanyFabricCapabilities;
import io.github.lounode.extrabotany.api.item.NatureEnergyItem;
import io.github.lounode.extrabotany.common.lib.LibMisc;
import io.github.lounode.extrabotany.network.ExtrabotanyPacket;
import io.github.lounode.extrabotany.xplat.EXplatAbstractions;

import java.util.UUID;
import java.util.WeakHashMap;

public class ExFabricXplatImpl extends FabricXplatImpl implements EXplatAbstractions {
	@Override
	public void sendToPlayer(ServerPlayer player, ExtrabotanyPacket packet) {
		ServerPlayNetworking.send(player, packet.getFabricId(), packet.toBuf());
	}

	@Override
	public Packet<ClientGamePacketListener> toVanillaClientboundPacket(ExtrabotanyPacket packet) {
		return ServerPlayNetworking.createS2CPacket(packet.getFabricId(), packet.toBuf());
	}

	@Nullable
	@Override
	public NatureEnergyItem findNatureEnergyItem(ItemStack stack) {
		return ExtrabotanyFabricCapabilities.NATURE_ENERGY_ITEM.find(stack, Unit.INSTANCE);
	}

	@Override
	public String getExtraBotanyVersion() {
		return FabricLoader.getInstance().getModContainer(LibMisc.MOD_ID).get().getMetadata().getVersion().getFriendlyString();
	}

	@Override
	public Player createFakePlayer(ServerLevel level, GameProfile userName) {
		return FakePlayer.get(level, userName);
	}

	@SuppressWarnings("experimental")
	@Override
	public int getFluidTemperature(Fluid fluid) {
		return FluidVariantAttributes.getTemperature(FluidVariant.of(fluid));
	}

	public static WeakHashMap<UUID, Integer> SIMULATE_QUERY = new WeakHashMap<>();

	@Override
	public float getEnchantPowerBonus(ServerLevel level, BlockPos pos) {
		if (level.isClientSide()) {
			return 0;
		}
		UUID queryID = UUID.randomUUID();
		var simulateInv = new Inventory(createFakePlayer(level, new GameProfile(queryID, "[EXTRABOTANY_QUERY_ENCHANT_LEVEL]")));
		var enchantMenu = new EnchantmentMenu(level.getRandom().nextInt(), simulateInv, ContainerLevelAccess.create(level, pos));

		ItemStack queryItem = new ItemStack(Items.GOLDEN_HOE);
		ItemNBTHelper.setString(queryItem, "[EXTRABOTANY_QUERY_ENCHANT_LEVEL]", queryID.toString());

		//Anti-GC
		ExFabricXplatImpl.SIMULATE_QUERY.put(queryID, 0);
		enchantMenu.enchantSlots.setItem(0, queryItem);

		if (SIMULATE_QUERY.containsKey(queryID)) {
			return SIMULATE_QUERY.getOrDefault(queryID, 0);
		}

		return 0;
	}
}
