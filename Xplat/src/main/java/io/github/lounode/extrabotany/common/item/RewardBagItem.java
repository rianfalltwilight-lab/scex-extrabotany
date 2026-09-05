package io.github.lounode.extrabotany.common.item;

import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import org.jetbrains.annotations.Nullable;

import io.github.lounode.extrabotany.common.helper.ItemNBTHelper;

import io.github.lounode.extrabotany.api.item.RewardBag;
import io.github.lounode.extrabotany.client.LootDataPoolClient;
import io.github.lounode.extrabotany.common.sounds.ExtraBotanySounds;

import java.util.List;

public class RewardBagItem extends Item implements RewardBag {

	public static final String TAG_LOOT_TABLE = "LootTable";

	private final ResourceLocation lootTable;

	public RewardBagItem(Properties properties, ResourceLocation lootTable) {
		super(properties);
		this.lootTable = lootTable;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
		ItemStack stack = player.getItemInHand(usedHand);

		if (!level.isClientSide()) {
			ResourceLocation lootId = getLoot(stack);
			if (lootId == null) {
				return InteractionResultHolder.fail(stack);
			}
			LootTable table = level.getServer().reloadableRegistries()
					.getLootTable(ResourceKey.create(Registries.LOOT_TABLE, lootId));
			LootParams.Builder parameter = new LootParams.Builder((ServerLevel) level)
					.withParameter(LootContextParams.THIS_ENTITY, player)
					.withParameter(LootContextParams.ORIGIN, player.position())
					.withLuck(player.getLuck());
			LootParams lootparams = parameter.create(LootContextParamSets.CHEST);

			if (!player.hasInfiniteMaterials()) {
				stack.shrink(1);
			}

			table.getRandomItems(lootparams, player.getLootTableSeed(), itemStack -> player.spawnAtLocation(itemStack).setNoPickUpDelay());
			level.playSound(null, player.getX(), player.getY(), player.getZ(), getSound(), SoundSource.PLAYERS, 0.8F, 1);

			return InteractionResultHolder.success(stack);
		}

		return InteractionResultHolder.fail(stack);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flags) {
		super.appendHoverText(stack, context, tooltip, flags);
		//TODO 显示概率
		ResourceLocation tableKey = getLoot(stack);
		if (tableKey == null) {
			return;
		}
		var datas = LootDataPoolClient.getData(tableKey);
		if (datas == null) {
			return;
		}

		for (var data : datas) {
			tooltip.add(Component.literal(Component.translatable(data.item().getDescriptionId()).getString() + " " + data.chance() + "%"));
		}
	}

	@Nullable
	public static ResourceLocation getLoot(ItemStack stack) {
		if (stack.getItem() instanceof RewardBagItem bag) {
			String tableKey = ItemNBTHelper.getString(stack, TAG_LOOT_TABLE, "");
			if (!tableKey.isEmpty()) {
				ResourceLocation parsed = ResourceLocation.tryParse(tableKey);
				if (parsed != null) {
					return parsed.withPrefix("reward_bags/");
				}
			}
			return bag.getLootTable().withPrefix("reward_bags/");
		}
		return null;
	}

	@Override
	public ResourceLocation getLootTable() {
		return this.lootTable;
	}

	public SoundEvent getSound() {
		return ExtraBotanySounds.REWARD_BAG_OPEN;
	}
}
