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

/**
 * 奖励袋内容的数据表，原本用于在客户端显示奖励袋里可能有的物品。这个功能在重制版里没有完成，所以这张表现在总是空的。
 * The data map of the reward bag contents. It was made to show the possible items of a reward bag on the client. This function was not completed in Reburn, thus the map is always empty at this time.
 */
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
