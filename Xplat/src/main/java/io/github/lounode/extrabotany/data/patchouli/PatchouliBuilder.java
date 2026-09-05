package io.github.lounode.extrabotany.data.patchouli;

import com.demonwav.mcdev.annotations.Translatable;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import org.jetbrains.annotations.Nullable;

import io.github.lounode.extrabotany.common.lib.RegistryHelper;
import io.github.lounode.extrabotany.data.patchouli.page.AbstractPage;
import io.github.lounode.extrabotany.data.patchouli.page.IPatchouliPage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class PatchouliBuilder {
	private final ResourceLocation category;
	private String name;
	private Item icon;
	private int sortNum;
	private ResourceLocation advancement;
	private final List<IPatchouliPage> pages = new ArrayList<>();
	private final Map<ResourceLocation, Integer> extraRecipeMappings = new HashMap<>();
	private boolean priority;
	private boolean secret;
	private boolean read;

	@Nullable
	private Integer color;

	public PatchouliBuilder(ResourceLocation category, @Translatable String name, ItemLike icon, int sortNum) {
		this.category = category;
		this.name = name;
		this.icon = icon.asItem();
		this.sortNum = sortNum;
	}

	public static PatchouliBuilder entry(ResourceLocation category) {
		return new PatchouliBuilder(category, "null", Items.PAPER, 0);
	}

	public PatchouliBuilder withName(@Translatable String name) {
		this.name = name;
		return this;
	}

	public PatchouliBuilder withIcon(ItemLike icon) {
		this.icon = icon.asItem();
		return this;
	}

	public PatchouliBuilder withSortNum(int sortNum) {
		this.sortNum = sortNum;
		return this;
	}

	public PatchouliBuilder withAdvancement(AdvancementHolder advancement) {
		this.advancement = advancement.id();
		return this;
	}

	public PatchouliBuilder withAdvancement(ResourceLocation advancement) {
		this.advancement = advancement;
		return this;
	}

	public PatchouliBuilder pages(AbstractPage<?>... page) {
		this.pages.addAll(List.of(page));
		return this;
	}

	public void save(Consumer<PatchouliEntry> consumer, ResourceLocation id) {
		consumer.accept(new PatchouliEntry(category, name, icon, pages, ResourceLocation.tryBuild(category.getPath(), id.getPath()),
				sortNum, advancement, extraRecipeMappings, priority, secret, read, color));
	}

	public PatchouliBuilder extraRecipeMapping(Item item, int pageNum) {
		if (item == Items.AIR) {
			return this;
		}

		ResourceLocation location = RegistryHelper.getRegistryName(item);
		this.extraRecipeMappings.put(location, pageNum);
		return this;
	}

	public PatchouliBuilder extraRecipeMapping(Block block, int pageNum) {
		if (block == Blocks.AIR) {
			return this;
		}
		ResourceLocation location = RegistryHelper.getRegistryName(block);
		this.extraRecipeMappings.put(location, pageNum);
		return this;
	}

	public PatchouliBuilder priority() {
		this.priority = true;
		return this;
	}

	public PatchouliBuilder secret() {
		this.secret = true;
		return this;
	}

	public PatchouliBuilder defaultRead() {
		this.read = true;
		return this;
	}

	public PatchouliBuilder color(int color) {
		this.color = color;
		return this;
	}
}
