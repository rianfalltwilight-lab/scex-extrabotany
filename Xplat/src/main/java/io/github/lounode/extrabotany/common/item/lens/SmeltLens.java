package io.github.lounode.extrabotany.common.item.lens;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import vazkii.botania.api.internal.ManaBurst;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.block.block_entity.mana.ManaSpreaderBlockEntity;
import vazkii.botania.common.entity.ManaBurstEntity;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.item.lens.Lens;
import vazkii.botania.common.item.lens.LensItem;
import vazkii.botania.xplat.BotaniaConfig;

import java.util.List;
import java.util.Optional;

import static vazkii.botania.common.item.lens.BoreLens.canHarvest;

public class SmeltLens extends Lens {
	@Override
	public boolean collideBurst(ManaBurst burst, HitResult rtr, boolean isManaBlock, boolean shouldKill, ItemStack stack) {
		Entity entity = burst.entity();
		Level world = entity.level();

		if (world.isClientSide || rtr.getType() != HitResult.Type.BLOCK) {
			return false;
		}

		BlockPos collidePos = ((BlockHitResult) rtr).getBlockPos();
		BlockState state = world.getBlockState(collidePos);

		ItemStack composite = ((LensItem) stack.getItem()).getCompositeLens(stack);
		boolean warpItems = !composite.isEmpty() && composite.is(BotaniaItems.WARP_LENS);
		ItemStack sourceLens = burst.getSourceLens();
		boolean canWarp = warpItems || sourceLens.is(BotaniaItems.WARP_LENS);

		if (canWarp && (state.is(BotaniaBlocks.FORCE_RELAY) || state.is(Blocks.PISTON) || state.is(Blocks.MOVING_PISTON) || state.is(Blocks.PISTON_HEAD))) {
			return false;
		}

		int harvestLevel = BotaniaConfig.common().harvestLevelBore();

		BlockEntity tile = world.getBlockEntity(collidePos);

		float hardness = state.getDestroySpeed(world, collidePos);
		int mana = burst.getMana();

		Optional<BlockPos> source = burst.getBurstSourcePosition()
				.filter(pos -> pos.dimension().equals(world.dimension()))
				.map(net.minecraft.core.GlobalPos::pos);
		if (!isManaBlock
				&& canHarvest(harvestLevel, state)
				&& hardness != -1
				&& (burst.isFake() || mana >= 24)) {
			if (!burst.hasAlreadyCollidedAt(collidePos)) {
				if (!burst.isFake()) {
					List<ItemStack> items = Block.getDrops(state, (ServerLevel) world, collidePos, tile);

					world.removeBlock(collidePos, false);
					world.gameEvent(entity, GameEvent.BLOCK_DESTROY, collidePos);
					if (BotaniaConfig.common().blockBreakParticles()) {
						world.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, collidePos, Block.getId(state));
					}

					boolean sourceless = source.isEmpty();
					boolean doWarp = warpItems && !sourceless;
					Vec3 dropPosition;
					if (doWarp && world.getBlockEntity(source.orElseThrow()) instanceof ManaSpreaderBlockEntity spreader) {
						Vec3 sourceVec = Vec3.atCenterOf(source.orElseThrow());
						/* NB: this looks backwards but it's right. spreaders take rotX/rotY to respectively mean
						* "rotation *parallel* to the X and Y axes", while vanilla's methods take XRot/YRot
						* to respectively mean "rotation *around* the X and Y axes".
						* See also the ManaBurstEntity constructor.
						* TODO consider renaming our versions to match vanilla
						*/
						float xRot = spreader.getRotationY();
						float yRot = -(spreader.getRotationX() + 90F);
						Vec3 inverseSpreaderDirection = ManaBurstEntity.calculateBurstVelocity(xRot, yRot).normalize().reverse();
						dropPosition = sourceVec.add(inverseSpreaderDirection);
					} else {
						dropPosition = Vec3.atCenterOf(collidePos);
					}

					if (world.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS)) {
						for (ItemStack stack_ : items) {
							var recipe = world.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(stack_), world).orElse(null);
							if (recipe != null && !recipe.value().getResultItem(world.registryAccess()).isEmpty()) {
								stack_ = recipe.value().getResultItem(world.registryAccess()).copy();
							}

							ItemEntity itemEntity = new ItemEntity(world, dropPosition.x, dropPosition.y, dropPosition.z, stack_);
							itemEntity.setDefaultPickUpDelay();
							world.addFreshEntity(itemEntity);
						}
					}

					burst.setMana(mana - 24);
				}
			}

			shouldKill = false;
		}

		return shouldKill;
	}
}
