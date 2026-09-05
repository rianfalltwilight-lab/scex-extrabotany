package io.github.lounode.extrabotany.common.util;

import com.google.gson.JsonElement;

import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.Nullable;

import vazkii.patchouli.client.book.BookContentLoader;
import vazkii.patchouli.client.book.BookContentResourceDirectLoader;
import vazkii.patchouli.client.book.ClientBookRegistry;
import vazkii.patchouli.client.book.template.BookTemplate;
import vazkii.patchouli.common.book.Book;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * A Patchouli Template Poll to cache all books registered templates
 * 
 * @author Lounode
 * @date 2025/04/06
 */
public class PatchouliUtil {
	private PatchouliUtil() {}

	public static PatchouliUtil INSTANCE;
	private final Map<ResourceLocation, Supplier<BookTemplate>> templatesPool = new HashMap<>();

	public static void onPatchouliReload(Map<ResourceLocation, Book> books) {
		//Read and save template
		if (INSTANCE == null) {
			INSTANCE = new PatchouliUtil();
		}
		INSTANCE.templatesPool.clear();
		for (Book book : books.values()) {
			INSTANCE.load(book, "templates", PatchouliUtil::loadTemplate, INSTANCE.templatesPool);
		}
	}

	public @Nullable Supplier<BookTemplate> getTemplate(ResourceLocation id) {
		return this.templatesPool.get(id);
	}

	private static Supplier<BookTemplate> loadTemplate(Book book, BookContentLoader loader, ResourceLocation key, ResourceLocation res) {
		JsonElement json = loadLocalizedJson(book, loader, res).json();
		Supplier<BookTemplate> supplier = () -> ClientBookRegistry.INSTANCE.gson.fromJson(json, BookTemplate.class);
		BookTemplate template = supplier.get();
		if (template == null) {
			throw new IllegalArgumentException(res + " could not be instantiated by the supplier.");
		} else {
			return supplier;
		}
	}

	private static BookContentLoader.LoadResult loadLocalizedJson(Book book, BookContentLoader loader, ResourceLocation file) {
		ResourceLocation localizedFile = ResourceLocation.tryBuild(file.getNamespace(), file.getPath().replaceAll("en_us", ClientBookRegistry.INSTANCE.currentLang));
		BookContentLoader.LoadResult input = loader.loadJson(book, localizedFile);
		if (input == null) {
			input = loader.loadJson(book, file);
			if (input == null) {
				throw new IllegalArgumentException(file + " does not exist.");
			}
		}

		return input;
	}

	private <T> void load(Book book, String thing, LoadFunc<T> loader, Map<ResourceLocation, T> builder) {
		BookContentLoader contentLoader = BookContentResourceDirectLoader.INSTANCE;
		List<ResourceLocation> foundIds = new ArrayList<>();
		contentLoader.findFiles(book, thing, foundIds);

		for (ResourceLocation id : foundIds) {
			String filePath = String.format("%s/%s/%s/%s/%s.json", "patchouli_books", book.id.getPath(), "en_us", thing, id.getPath());
			T value = loader.load(book, contentLoader, id, ResourceLocation.tryBuild(id.getNamespace(), filePath));
			if (value != null) {
				builder.put(id, value);
			}
		}

	}

	private interface LoadFunc<T> {
		@Nullable
		T load(Book var1, BookContentLoader var2, ResourceLocation var3, ResourceLocation var4);
	}

}
