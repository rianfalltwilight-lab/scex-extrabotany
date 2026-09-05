package io.github.lounode.extrabotany.common.item.equipment.bauble;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
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
			}
		}

		@Nullable
		private static BlockState getDaisyRecipe(Level level, BlockState input) {
			for (Recipe<?> r : ExtraBotanyRecipeTypes.getRecipes(level, BotaniaRecipeTypes.PURE_DAISY_TYPE).values()) {
				if (!(r instanceof PureDaisyRecipe recipe)) {
					continue;
				}
				if (recipe.getInput().test(input)) {
					return recipe.getOutput().pick(level.getRandom());
				}
			}
			return null;
		}
	}
}
