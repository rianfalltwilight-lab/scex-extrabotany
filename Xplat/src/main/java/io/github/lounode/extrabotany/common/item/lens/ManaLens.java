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
			ItemEntity outputEntity = new ItemEntity(level, entity.getX(), entity.getY() + 0.5F, entity.getZ() + 0.5F, output);
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
		var recipes = ExtraBotanyRecipeTypes.getRecipes(level, BotaniaRecipeTypes.MANA_INFUSION_TYPE).values().stream()
				.filter(r -> r instanceof ManaInfusionRecipe)
				.toList();
		for (var recipe : recipes) {
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
