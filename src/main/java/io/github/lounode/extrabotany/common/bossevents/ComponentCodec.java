package io.github.lounode.extrabotany.common.bossevents;

import com.mojang.serialization.Codec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

/**
 * 文本组件的编解码辅助方法。血条的数据包用它传输 Boss 的名字。
 * Helper methods to encode and decode text components. The boss bar packets use them to send the name of the boss.
 */
public class ComponentCodec {
	public static final Codec<Component> CODEC = ComponentSerialization.CODEC;

	public static Component fromNetwork(RegistryFriendlyByteBuf buf) {
		return ComponentSerialization.STREAM_CODEC.decode(buf);
	}

	public static void toNetwork(RegistryFriendlyByteBuf buf, Component component) {
		ComponentSerialization.STREAM_CODEC.encode(buf, component);
	}
}
