package io.github.lounode.extrabotany.forge.xplat;

import com.mojang.authlib.GameProfile;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.network.PacketDistributor;

import vazkii.botania.neoforge.xplat.NeoForgeXplatImpl;

import io.github.lounode.extrabotany.api.ExtrabotanyForgeCapabilities;
import io.github.lounode.extrabotany.api.item.NatureEnergyItem;
import io.github.lounode.extrabotany.common.lib.LibMisc;
import io.github.lounode.extrabotany.network.ExtrabotanyPacket;
import io.github.lounode.extrabotany.xplat.EXplatAbstractions;

import javax.annotation.Nullable;

public class ExForgeXplatImpl extends NeoForgeXplatImpl implements EXplatAbstractions {
	@Override
	public void sendToPlayer(ServerPlayer player, ExtrabotanyPacket packet) {
		PacketDistributor.sendToPlayer(player, packet);
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Packet<ClientGamePacketListener> toVanillaClientboundPacket(ExtrabotanyPacket packet) {
		return (Packet) new ClientboundCustomPayloadPacket(packet);
	}

	@Nullable
	@Override
	public NatureEnergyItem findNatureEnergyItem(ItemStack stack) {
		return stack.getCapability(ExtrabotanyForgeCapabilities.NATURE_ENERGY_ITEM);
	}

	@Override
	public String getExtraBotanyVersion() {
		return ModList.get().getModContainerById(LibMisc.MOD_ID).get().getModInfo().getVersion().toString();
	}

	@Override
	public Player createFakePlayer(ServerLevel level, GameProfile userName) {
		return FakePlayerFactory.get(level, userName);
	}

	@Override
	public int getFluidTemperature(Fluid fluid) {
		return fluid.getFluidType().getTemperature();
	}

	@Override
	public float getEnchantPowerBonus(ServerLevel level, BlockPos pos) {
		float j = 0;
		for (BlockPos blockpos : EnchantingTableBlock.BOOKSHELF_OFFSETS) {
			if (EnchantingTableBlock.isValidBookShelf(level, pos, blockpos)) {
				j += level.getBlockState(pos.offset(blockpos)).getEnchantPowerBonus(level, pos.offset(blockpos));
			}
		}
		return j;
	}
}
