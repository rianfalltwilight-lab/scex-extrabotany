package io.github.lounode.extrabotany.common.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 这个模组的数据映射（data map）。数据包可以修改它们。
 * The data maps of this mod. A data pack can change them.
 */
public final class ExtraBotanyDataMaps {
	/**
	 * 不随想之茧的孵化表：一种物品孵化出哪种生物。
	 * The hatch table of the Cocoon of Desire: the type of creature that an item hatches into.
	 *
	 * <p>文件位置：{@code data/extrabotany/data_maps/item/cocoon_hatch.json}。只有服务端使用这张表。
	 * <p>File location: {@code data/extrabotany/data_maps/item/cocoon_hatch.json}. Only the server uses this table.
	 */
	public static final DataMapType<Item, CocoonHatch> COCOON_HATCH = DataMapType.builder(
			prefix("cocoon_hatch"), Registries.ITEM, CocoonHatch.CODEC).build();

	/**
	 * 孵化表里的一行。
	 * One row of the hatch table.
	 *
	 * @param entity 孵化出的生物类型 / the type of creature that hatches
	 */
	public record CocoonHatch(EntityType<?> entity) {
		public static final Codec<CocoonHatch> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("entity").forGetter(CocoonHatch::entity)
		).apply(instance, CocoonHatch::new));
	}

	public static void register(RegisterDataMapTypesEvent event) {
		event.register(COCOON_HATCH);
	}

	private ExtraBotanyDataMaps() {}
}
