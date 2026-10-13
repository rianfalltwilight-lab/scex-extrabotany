package io.github.lounode.extrabotany.network.clientbound;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;

import io.github.lounode.extrabotany.network.ExtrabotanyPacket;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 让客户端播放"物品激活"动画。它和不死图腾的动画相同，但显示指定的物品。
 * Tells the client to play the item activation animation. The animation is the same as for the Totem of Undying, but it shows the given item.
 *
 * <p>来源：ExtraMeteorP 1.16 {@code PotatoChipsPack}。
 * <p>Source: ExtraMeteorP 1.16 {@code PotatoChipsPack}.
 */
public record ItemActivationPacket(ItemStack stack) implements ExtrabotanyPacket {
	public static final CustomPacketPayload.Type<ItemActivationPacket> ID =
			new CustomPacketPayload.Type<>(prefix("item_activation"));
	public static final StreamCodec<RegistryFriendlyByteBuf, ItemActivationPacket> STREAM_CODEC =
			ItemStack.STREAM_CODEC.map(ItemActivationPacket::new, ItemActivationPacket::stack);

	@Override
	public Type<ItemActivationPacket> type() {
		return ID;
	}
}
