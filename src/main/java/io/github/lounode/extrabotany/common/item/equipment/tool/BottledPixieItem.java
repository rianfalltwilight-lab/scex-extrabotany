package io.github.lounode.extrabotany.common.item.equipment.tool;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.EventHooks;

import vazkii.botania.api.mana.ManaItemHandler;

import io.github.lounode.extrabotany.common.entity.ExtraBotanyEntityType;
import io.github.lounode.extrabotany.common.entity.PetPixieEntity;

/**
 * 瓶中精灵。右键消耗 300 点魔力，召唤一只为玩家战斗的宠物精灵。冷却 12 秒。
 * Bottled Pixie. A use takes 300 mana and summons a pet pixie that fights for the player. The cooldown is 12 seconds.
 *
 * <p>来源：ExtraMeteorP 1.12 {@code ItemBottledPixie}。
 * <p>Source: ExtraMeteorP 1.12 {@code ItemBottledPixie}.
 */
public class BottledPixieItem extends Item {
	private static final int MANA_PER_USE = 300;
	private static final int COOLDOWN_TICKS = 240;
	private static final float PIXIE_DAMAGE = 3.5F;

	public BottledPixieItem(Properties props) {
		super(props);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!(level instanceof ServerLevel serverLevel)) {
			return InteractionResultHolder.success(stack);
		}
		if (!ManaItemHandler.instance().requestManaExactForTool(stack, player, MANA_PER_USE, true)) {
			return InteractionResultHolder.fail(stack);
		}

		PetPixieEntity pixie = new PetPixieEntity(ExtraBotanyEntityType.PET_PIXIE, serverLevel);
		pixie.setPos(player.getX(), player.getY() + 1.5, player.getZ());
		pixie.setProps(player, PIXIE_DAMAGE);
		EventHooks.finalizeMobSpawn(pixie, serverLevel, serverLevel.getCurrentDifficultyAt(pixie.blockPosition()),
				MobSpawnType.MOB_SUMMONED, null);
		serverLevel.addFreshEntity(pixie);
		player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
		return InteractionResultHolder.success(stack);
	}
}
