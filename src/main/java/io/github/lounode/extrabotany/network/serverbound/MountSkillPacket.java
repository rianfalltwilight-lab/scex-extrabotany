package io.github.lounode.extrabotany.network.serverbound;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import vazkii.botania.api.item.Relic;
import vazkii.botania.common.handler.EquipmentHandler;

import io.github.lounode.extrabotany.api.item.MountableAccessory;
import io.github.lounode.extrabotany.common.entity.mount.MountEntity;
import io.github.lounode.extrabotany.common.entity.mount.UfoEntity;
import io.github.lounode.extrabotany.network.ExtrabotanyPacket;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 客户端报告玩家按下了坐骑键。
 * The client reports that the player pushed the mount key.
 *
 * <p>玩家没有骑乘时：召唤他佩戴的坐骑饰品对应的坐骑。玩家驾驶飞碟时：抓住或者放开下方的生物。
 * <p>When the player rides nothing: summon the mount of the mount accessory that the player wears. When the player drives a Cosmic Car: catch or release the mob below.
 *
 * <p>数据包不带任何数据。服务端自己查找饰品和目标。
 * <p>The packet has no data. The server finds the accessory and the target itself.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code MountPack} 和 {@code UfoCatchPack}。原版的两个数据包分别带着物品和实体编号，服务端直接信任它们。
 * <p>Source: ExtraMeteorP 1.16 {@code MountPack} and {@code UfoCatchPack}. The two packets of the original contain an item and an entity ID, and the server trusts them.
 */
public final class MountSkillPacket implements ExtrabotanyPacket {
	public static final MountSkillPacket INSTANCE = new MountSkillPacket();
	public static final CustomPacketPayload.Type<MountSkillPacket> ID =
			new CustomPacketPayload.Type<>(prefix("mount_skill"));
	public static final StreamCodec<ByteBuf, MountSkillPacket> STREAM_CODEC = StreamCodec.unit(INSTANCE);

	private static final double SUMMON_HEIGHT = 0.5;

	private MountSkillPacket() {}

	@Override
	public Type<MountSkillPacket> type() {
		return ID;
	}

	public void handle(ServerPlayer player) {
		activate(player);
	}

	/**
	 * 执行坐骑键的服务端逻辑。
	 * Does the server logic of the mount key.
	 *
	 * @return 有动作发生时返回 {@code true}。 {@code true} when an action occurred.
	 */
	public static boolean activate(Player player) {
		if (player.isSpectator()) {
			return false;
		}
		if (player.getVehicle() instanceof UfoEntity ufo && ufo.getControllingPassenger() == player) {
			ufo.toggleCatch();
			return true;
		}
		return player.getVehicle() == null && summon(player);
	}

	private static boolean summon(Player player) {
		ItemStack stack = EquipmentHandler.findOrEmpty(candidate -> candidate.getItem() instanceof MountableAccessory, player);
		if (!(stack.getItem() instanceof MountableAccessory accessory)) {
			return false;
		}
		Relic relic = Relic.LOOKUP.find(stack);
		if (relic != null && !relic.isRightPlayer(player)) {
			return false;
		}

		MountEntity mount = accessory.createMount(player.level(), player, stack);
		mount.setPos(player.getX(), player.getY() + SUMMON_HEIGHT, player.getZ());
		mount.setYRot(player.getYRot());
		return player.level().addFreshEntity(mount) && player.startRiding(mount);
	}
}
