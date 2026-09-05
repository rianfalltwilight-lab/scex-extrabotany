package io.github.lounode.extrabotany.data.patchouli;

import com.demonwav.mcdev.annotations.Translatable;
import com.google.common.collect.Sets;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;

import org.jetbrains.annotations.NotNull;

import io.github.lounode.extrabotany.data.patchouli.page.patchouli.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public abstract class PatchouliProvider implements DataProvider {
	protected PackOutput packOutput;

	public PatchouliProvider(PackOutput packOutput) {
		this.packOutput = packOutput;
	}

	@Override
	public CompletableFuture<?> run(CachedOutput cache) {
		Set<ResourceLocation> checkDuplicates = Sets.newHashSet();
		List<CompletableFuture<?>> output = new ArrayList<>();
		buildEntries((entry) -> {
			if (!checkDuplicates.add(entry.getID())) {
				throw new IllegalStateException("Duplicate entry " + entry.getID());
			} else {
				output.add(DataProvider.saveStable(cache, entry.serializeEntry(), getOutputPath(entry)));
			}
		});
		return CompletableFuture.allOf(output.toArray(CompletableFuture[]::new));
	}

	protected abstract Path getOutputPath(PatchouliEntry entry);

	protected abstract void buildEntries(Consumer<PatchouliEntry> consumer);

	protected EmptyPage empty() {
		return new EmptyPage();
	}

	protected TextPage text(@Translatable String text) {
		return new TextPage(text);
	}

	protected CraftingPage crafting(ItemLike itemLike) {
		return new CraftingPage(itemLike);
	}

	protected CraftingPage crafting(String recipe) {
		return new CraftingPage(recipe);
	}

	protected SpotlightPage spotlight(ItemLike itemLike) {
		return new SpotlightPage(itemLike).linkRecipe(true);
	}

	protected MultiBlockPage multiBlock(@Translatable String name, String[][] pattern) {
		return new MultiBlockPage(name, pattern);
	}

	@NotNull
	@Override
	public String getName() {
		return "Patchouli Provider";
	}
}
