package io.github.lounode.extrabotany.common.loot;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.Item;

import org.jetbrains.annotations.Nullable;

import io.github.lounode.extrabotany.xplat.EXplatAbstractions;

import java.util.*;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class RewardBagManager implements ResourceManagerReloadListener {
	private static final Map<ResourceLocation, List<LootData>> pool = new LinkedHashMap<>();
	private static final Set<ResourceLocation> rewardBagIds = new HashSet<>();

	public static void registerListener() {
		EXplatAbstractions.INSTANCE.registerReloadListener(PackType.SERVER_DATA, prefix("reward_bag"), new RewardBagManager());
	}

	@Override
	public void onResourceManagerReload(ResourceManager resourceManager) {
		pool.clear();
		rewardBagIds.clear();
		/*
		for (var resource : resourceManager.listResources("reward_bags", p -> p.getPath().endsWith(".json")).entrySet()) {
			String path = resource.getKey().getPath().replace(".json", "").replace("reward_bags/", "");
			rewardBagIds.add(ResourceLocation.fromNamespaceAndPath(resource.getKey().getNamespace(), path));
		}
		
		*/
	}

	public static void onReload(MinecraftServer server) {
		//server.getLootData().getLootTable()
	}

	//Client

	public static void syncDataFormServer() {
		pool.clear();
	}

	@Nullable
	public static List<LootData> getData(ResourceLocation key) {
		if (!pool.containsKey(key)) {
			return null;
		}

		return pool.get(key);
	}

	public record LootData(Item item, double chance) {
	}
}
