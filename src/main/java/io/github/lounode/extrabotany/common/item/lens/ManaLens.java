package io.github.lounode.extrabotany.common.item.lens;

import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.internal.ManaBurst;
import vazkii.botania.api.mana.BurstProperties;
import vazkii.botania.api.recipe.ManaInfusionRecipe;
import vazkii.botania.common.crafting.BotaniaRecipeTypes;
import vazkii.botania.common.item.lens.Lens;

import io.github.lounode.extrabotany.common.crafting.ExtraBotanyRecipeTypes;

import java.util.Objects;

/**
 * 魔力透镜：魔力。脉冲经过掉在地上的物品时，直接对它们进行魔力灌注，消耗脉冲里的魔力。物品下面的方块决定可以使用哪些催化配方。
 * Mana Lens: Mana. When the burst goes through dropped items, it does the mana infusion on them directly and uses the mana of the burst. The block below the item tells which catalyst recipes can be used.
 */
public class ManaLens extends Lens {

	@Override
	public void apply(ItemStack stack, BurstProperties props) {
		props.maxMana = 1000;
		props.motionModifier *= 0.5F;
		props.manaLossPerTick *= 0.2F;
		props.color = Objects.requireNonNull(ChatFormatting.AQUA.getColor());
	}

	@Override
	public void updateBurst(ManaBurst burst, ItemStack stack) {
		Projectile burstEntity = burst.entity();
		if (burstEntity.level().isClientSide()) {
			return;
		}
		if (burst.isFake()) {
			return;
		}
		Level level = burstEntity.level();
		var entities = level.getEntitiesOfClass(ItemEntity.class, getAABB(burst));

		for (var entity : entities) {
			if (entity.hasPickUpDelay()) {
				continue;
			}
			int mana = burst.getMana();
			ItemStack stack1 = entity.getItem();
			BlockState state = level.getBlockState(entity.blockPosition().below());
			ManaInfusionRecipe recipe = getRecipe(level, stack1, state);
			if (recipe == null) {
				continue;
			}
			int manaToConsume = recipe.getManaToConsume();
			if (mana < manaToConsume) {
				continue;
			}

			burst.setMana(mana - manaToConsume);
			stack1.shrink(1);

			ItemStack output = recipe.getResultItem(level.registryAccess()).copy();
			ItemEntity outputEntity = new ItemEntity(level, entity.getX(), entity.getY() + 0.5F, entity.getZ(), output);
			outputEntity.setPickUpDelay(50);
			level.addFreshEntity(outputEntity);
		}
	}

	public AABB getAABB(ManaBurst burst) {
		Projectile burstEntity = burst.entity();
		return new AABB(burstEntity.getX(), burstEntity.getY(), burstEntity.getZ(),
				burstEntity.xOld, burstEntity.yOld, burstEntity.zOld).inflate(1);
	}

	@Nullable
	public static ManaInfusionRecipe getRecipe(Level level, ItemStack stack, BlockState state) {
		for (var holder : ExtraBotanyRecipeTypes.getRecipeHolders(level, BotaniaRecipeTypes.MANA_INFUSION_TYPE)) {
			ManaInfusionRecipe recipe = holder.value();
			boolean flag1 = recipe.matches(stack);
			boolean flag2 = true;
			if (recipe.getRecipeCatalyst() != null && !recipe.getRecipeCatalyst().test(state)) {
				flag2 = false;
			}
			if (flag1 && flag2) {
				return recipe;
			}
		}
		return null;
	}
}
