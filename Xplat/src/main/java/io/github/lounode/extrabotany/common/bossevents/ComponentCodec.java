package io.github.lounode.extrabotany.common.bossevents;

import com.mojang.serialization.Codec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

public class ComponentCodec {
	public static final Codec<Component> CODEC = ComponentSerialization.CODEC;

	public static Component fromNetwork(RegistryFriendlyByteBuf buf) {
		return ComponentSerialization.STREAM_CODEC.decode(buf);
	}

	public static void toNetwork(RegistryFriendlyByteBuf buf, Component component) {
		ComponentSerialization.STREAM_CODEC.encode(buf, component);
	}
}
