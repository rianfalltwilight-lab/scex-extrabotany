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
import net.minecraft.world.item.crafting.Recipe;
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

public class EdelweissBlockEntity extends ExtraGeneratingFlowerBlockEntity {

	private static final String TAG_COOLDOWN = "cooldown";

	private static final int RANGE = 1;

	public static final int MAX_MANA = 12800;
	public static final int COOLDOWN = 40;

	private int cooldown;

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

		List<Entity> entitiesNearby = getLevel().getEntitiesOfClass(Entity.class, new AABB(getEffectivePos()).inflate(RANGE)).stream()
				.filter(entity -> getManaFromEntity(entity) > 0)
				.toList();

		if (entitiesNearby.isEmpty()) {
			return;
		}

		Entity toEat = entitiesNearby.get(getLevel().getRandom().nextInt(entitiesNearby.size()));
		int mana = getManaFromEntity(toEat);
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
		sync();
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
		for (Recipe<?> r : ExtraBotanyRecipeTypes.getRecipes(getLevel(), ExtraBotanyRecipeTypes.EDELWEISS_RECIPE_TYPE).values()) {
			if (!(r instanceof EdelweissRecipe recipe)) {
				continue;
			}
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
