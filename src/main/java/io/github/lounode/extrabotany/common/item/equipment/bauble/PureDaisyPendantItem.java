package io.github.lounode.extrabotany.common.item.equipment.bauble;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.api.recipe.PureDaisyRecipe;
import vazkii.botania.common.crafting.BotaniaRecipeTypes;
import vazkii.botania.common.handler.EquipmentHandler;
import vazkii.botania.common.item.equipment.bauble.BaubleItem;

import io.github.lounode.extrabotany.common.util.PlayerUtil;
import io.github.lounode.extrabotany.common.crafting.ExtraBotanyRecipeTypes;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * 白雏菊项链。佩戴者空手右键一个方块时，按白雏菊的配方立刻转化这个方块，消耗 50 点魔力。
 * Pure Daisy Pendant. When the wearer uses a block with an empty hand, the block is converted immediately by the Pure Daisy recipes, for 50 mana.
 */
public class PureDaisyPendantItem extends BaubleItem {

	public static int TRANSFORM_MANA = 50;

	public PureDaisyPendantItem(Properties props) {
		super(props);
	}

	public static class EventHandler {

		public static void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
			if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
				return;
			}
			// 游戏为每只手各触发一次这个事件。只处理主手的那一次，否则一次点击会转化两次（例如冰直接变成蓝冰）。
			// The game starts this event one time for each hand. Only the event of the main hand is processed. If not, one click converts two times (for example, ice becomes blue ice directly).
			if (event.getHand() != InteractionHand.MAIN_HAND) {
				return;
			}
			if (!serverPlayer.getMainHandItem().isEmpty()) {
				return;
			}
			BlockPos pos = event.getPos();
			Level level = event.getLevel();
			BlockState state = level.getBlockState(pos);
			ItemStack stack = EquipmentHandler.findOrEmpty(s -> s.getItem() instanceof PureDaisyPendantItem, serverPlayer);
			if (stack.isEmpty()) {
				return;
			}
			BlockState output = getDaisyRecipe(level, state);
			if (output == null) {
				return;
			}

			if (output.is(state.getBlock())) {
				return;
			}
			if (!ManaItemHandler.instance().requestManaExactForTool(stack, serverPlayer, TRANSFORM_MANA, true)) {
				return;
			}
			if (PlayerUtil.tryBreakBlock(serverPlayer, ItemStack.EMPTY, level, pos)) {
				level.setBlockAndUpdate(pos, output.getBlock().withPropertiesOf(state));
				level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(output));
				level.gameEvent(null, GameEvent.BLOCK_CHANGE, pos);
				// 这次点击已经用掉了，副手的物品不再对新方块起作用。
				// This click is used up. The item of the other hand does not act on the new block.
				event.setCanceled(true);
			}
		}

		@Nullable
		private static BlockState getDaisyRecipe(Level level, BlockState input) {
			for (var holder : ExtraBotanyRecipeTypes.getRecipeHolders(level, BotaniaRecipeTypes.PURE_DAISY_TYPE)) {
				PureDaisyRecipe recipe = holder.value();
				if (recipe.getInput().test(input)) {
					return recipe.getOutput().pick(level.getRandom());
				}
			}
			return null;
		}
	}
}
