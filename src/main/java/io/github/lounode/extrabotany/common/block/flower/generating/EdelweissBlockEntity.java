package io.github.lounode.extrabotany.common.block.flower.generating;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import io.github.lounode.extrabotany.common.block.flower.ExtraGeneratingFlowerBlockEntity;
import vazkii.botania.api.block_entity.RadiusDescriptor;
import vazkii.botania.common.helper.PlayerHelper;

import io.github.lounode.extrabotany.api.recipe.EdelweissRecipe;
import io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks;
import io.github.lounode.extrabotany.common.crafting.ExtraBotanyRecipeTypes;
import io.github.lounode.extrabotany.common.lib.LibAdvancementNames;
import io.github.lounode.extrabotany.xplat.ExtraBotanyConfig;

import java.util.List;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 雪绒花。它吃掉紧挨着它的实体来产生魔力。哪些实体能吃、能产生多少魔力，由雪绒花配方决定（默认只有雪傀儡）。
 * Edelweiss. It eats the entities adjacent to it to make mana. The Edelweiss recipes tell which entities it can eat and how much mana they give (by default, only the snow golem).
 *
 * <p>来源：重制版（Lounode）。它对应 ExtraMeteorP 1.12 的 {@code SubTileEdelweiss}。
 * <p>Source: Reburn (Lounode). It corresponds to {@code SubTileEdelweiss} of ExtraMeteorP 1.12.
 */
public class EdelweissBlockEntity extends ExtraGeneratingFlowerBlockEntity {

	private static final String TAG_COOLDOWN = "cooldown";

	private static final int RANGE = 1;

	public static final int MAX_MANA = 12800;
	public static final int COOLDOWN = 40;

	private int cooldown;
	// Scoped to one tick only; public lookups outside a tick always read the current manager.
	@Nullable
	private List<RecipeHolder<EdelweissRecipe>> tickRecipes;

	public EdelweissBlockEntity(BlockPos pos, BlockState state) {
		super(ExtrabotanyFlowerBlocks.EDELWEISS, pos, state);
	}

	@Override
	public void tickFlower() {
		super.tickFlower();
		if (getLevel().isClientSide()) {
			return;
		}
		if (getCooldown() > 0) {
			setCooldown(getCooldown() - 1);
			return;
		}
		if (getMana() >= getMaxMana()) {
			return;
		}

		if (isIdleBackoff()) {
			return;
		}
		var nearby = getLevel().getEntitiesOfClass(Entity.class, new AABB(getEffectivePos()).inflate(RANGE));
		if (nearby.isEmpty()) {
			// Nothing to eat: this all-entity AABB query ran every tick. Sleep briefly.
			backoffIdle(IDLE_BACKOFF_TICKS);
			return;
		}
		Entity toEat;
		int mana;
		var previousRecipes = tickRecipes;
		tickRecipes = ExtraBotanyRecipeTypes.getRecipeHolders(getLevel(), ExtraBotanyRecipeTypes.EDELWEISS_RECIPE_TYPE);
		try {
			List<Entity> entitiesNearby = nearby.stream()
					.filter(entity -> getManaFromEntity(entity) > 0)
					.toList();
			if (entitiesNearby.isEmpty()) {
				backoffIdle(IDLE_BACKOFF_TICKS);
				return;
			}
			toEat = entitiesNearby.get(getLevel().getRandom().nextInt(entitiesNearby.size()));
			// Keep the virtual call, including the original second lookup for the selected entity.
			mana = getManaFromEntity(toEat);
		} finally {
			tickRecipes = previousRecipes;
		}
		toEat.discard();

		if (EntityType.SNOW_GOLEM == toEat.getType()) {
			grantAdvancementToNearby();
			Vec3 pos = toEat.position();
			((ServerLevel) getLevel()).sendParticles(
					new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SNOW_BLOCK.defaultBlockState()),
					pos.x, pos.y, pos.z,
					20,
					0.5, 0.5, 0.5,
					0.1
			);
			getLevel().playSound(null, getEffectivePos(), SoundEvents.SNOW_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
		}

		addMana(mana);
		setCooldown(getAfterWorkCooldown());
		getLevel().playSound(null, getEffectivePos(), SoundEvents.GENERIC_DRINK, SoundSource.BLOCKS, 0.01F, 0.5F + (float) Math.random() * 0.5F);
		syncThrottled();
	}

	public void grantAdvancementToNearby() {
		var players = PlayerHelper.getRealPlayersIn(getLevel(), new AABB(getBlockPos()).inflate(16));
		for (var player : players) {
			if (PlayerHelper.hasAdvancement((ServerPlayer) player, prefix("main/" + LibAdvancementNames.ITS_ONLY_THE_FAIRY_TALE))) {
				continue;
			}
			PlayerHelper.grantCriterion((ServerPlayer) player, prefix("main/" + LibAdvancementNames.ITS_ONLY_THE_FAIRY_TALE), "code_triggered");
		}
	}

	public int getManaFromEntity(Entity entity) {
		var recipes = tickRecipes != null ? tickRecipes
				: ExtraBotanyRecipeTypes.getRecipeHolders(getLevel(), ExtraBotanyRecipeTypes.EDELWEISS_RECIPE_TYPE);
		for (var holder : recipes) {
			EdelweissRecipe recipe = holder.value();
			if (recipe.getInput().matches(entity.getType())) {
				return recipe.getManaOutput();
			}
		}
		return 0;
	}

	public int getAfterWorkCooldown() {
		return ExtraBotanyConfig.common().edelweissCooldown();
	}

	@Override
	public int getMaxMana() {
		return ExtraBotanyConfig.common().edelweissMaxMana();
	}

	public int getCooldown() {
		return cooldown;
	}

	public void setCooldown(int cooldown) {
		this.cooldown = cooldown;
	}

	@Override
	public int getColor() {
		return 0X4169E1;
	}

	@Override
	public @Nullable RadiusDescriptor getRadius() {
		return RadiusDescriptor.Rectangle.square(getEffectivePos(), RANGE);
	}

	@Override
	public void readFromPacketNBT(CompoundTag cmp) {
		super.readFromPacketNBT(cmp);

		setCooldown(cmp.getInt(TAG_COOLDOWN));
	}

	@Override
	public void writeToPacketNBT(CompoundTag cmp) {
		super.writeToPacketNBT(cmp);

		cmp.putInt(TAG_COOLDOWN, getCooldown());
	}
}
