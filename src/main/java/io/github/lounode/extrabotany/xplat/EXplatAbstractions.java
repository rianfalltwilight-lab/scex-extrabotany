package io.github.lounode.extrabotany.xplat;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.ServiceUtil;
import vazkii.botania.api.item.Relic;
import vazkii.botania.api.mana.ManaItem;
import vazkii.botania.api.mana.ManaReceiver;
import vazkii.botania.common.block.flower.SpecialFlowerBlock;
import vazkii.botania.xplat.XplatAbstractions;

import io.github.lounode.extrabotany.api.item.NatureEnergyItem;
import io.github.lounode.extrabotany.network.ExtrabotanyPacket;

import java.util.function.BiFunction;

/**
 * 平台相关操作的入口：发送数据包、查找能力（魔力物品、遗物、自然能量物品等）、创建假玩家等。这个项目只支持 NeoForge，实现在 {@code ExForgeXplatImpl} 里。
 * The entry point for platform operations: to send packets, to find capabilities (mana items, relics, nature energy items and others), to make fake players, and others. This project only supports NeoForge, and the implementation is in {@code ExForgeXplatImpl}.
 */
public interface EXplatAbstractions extends XplatAbstractions {
	EXplatAbstractions INSTANCE = ServiceUtil.findService(EXplatAbstractions.class, null);

	void sendToPlayer(ServerPlayer player, ExtrabotanyPacket packet);
	Packet<ClientGamePacketListener> toVanillaClientboundPacket(ExtrabotanyPacket packet);

	@Nullable
	NatureEnergyItem findNatureEnergyItem(ItemStack stack);
	String getExtraBotanyVersion();

	/** Compatibility boundary for APIs removed from Botania's loader abstraction in 1.21. */
	default <T extends BlockEntity> BlockEntityType<T> createBlockEntityType(
			BiFunction<BlockPos, BlockState, T> factory, Block... blocks) {
		return BlockEntityType.Builder.of(factory::apply, blocks).build(null);
	}

	/** Compatibility wrappers for capability lookups that moved out of Botania's loader abstraction. */
	@Nullable
	default ManaItem findManaItem(ItemStack stack) {
		return ManaItem.LOOKUP.find(stack);
	}

	@Nullable
	default Relic findRelic(ItemStack stack) {
		return Relic.LOOKUP.find(stack);
	}

	@Nullable
	default ManaReceiver findManaReceiver(Level level, BlockPos pos, @Nullable Direction context) {
		return ManaReceiver.LOOKUP.find(level, pos, context);
	}

	default boolean isSpecialFlowerBlock(Block block) {
		return block instanceof SpecialFlowerBlock;
	}

	default ModLoaderType getModLoader() {
		try {
			Class.forName("net.minecraftforge.fml.loading.FMLLoader");
			return ModLoaderType.FORGE;
		} catch (ClassNotFoundException ignored) {}

		try {
			Class.forName("net.neoforged.fml.loading.FMLLoader");
			return ModLoaderType.NEOFORGE;
		} catch (ClassNotFoundException ignored) {}

		try {
			Class.forName("net.fabricmc.loader.api.FabricLoader");
			return ModLoaderType.FABRIC;
		} catch (ClassNotFoundException ignored) {}

		try {
			Class.forName("org.quiltmc.loader.api.QuiltLoader");
			return ModLoaderType.QUILT;
		} catch (ClassNotFoundException ignored) {}

		return ModLoaderType.UNKNOWN;
	}

	Player createFakePlayer(ServerLevel level, GameProfile userName);

	default int getFluidTemperature(Fluid fluid) {
		return 0;
	}

	default float getEnchantPowerBonus(ServerLevel level, BlockPos pos) {
		return 0;
	}

	enum ModLoaderType implements StringRepresentable {
		FORGE("forge"),
		FABRIC("fabric"),
		NEOFORGE("neoforge"),
		QUILT("quilt"),
		UNKNOWN("unknown");

		public static final Codec<ModLoaderType> CODEC = StringRepresentable.fromEnum(ModLoaderType::values);
		private final String key;

		ModLoaderType(String key) {
			this.key = key;
		}

		@Override
		public String getSerializedName() {
			return this.key;
		}
	}
}
