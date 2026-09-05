package io.github.lounode.extrabotany.common.block.flower.functional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import io.github.lounode.extrabotany.common.block.flower.ExtraFunctionalFlowerBlockEntity;
import vazkii.botania.api.block_entity.RadiusDescriptor;
import vazkii.botania.common.item.equipment.tool.ToolCommons;
import vazkii.botania.xplat.BotaniaConfig;

import io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks;
import io.github.lounode.extrabotany.common.util.PlayerUtil;
import io.github.lounode.extrabotany.xplat.ExtraBotanyConfig;

import java.util.*;

public class WoodieniaBlockEntity extends ExtraFunctionalFlowerBlockEntity {

	public static final String TAG_COOLDOWN = "cooldown";
	public static final String TAG_OWNER_UUID = "ownerUUID";

	private int cooldown;
	@Nullable
	private UUID ownerUUID;

	public WoodieniaBlockEntity(BlockPos pos, BlockState state) {
		super(ExtrabotanyFlowerBlocks.WOODIENIA, pos, state);
	}

	@Override
	public void tickFlower() {
		super.tickFlower();
		if (getLevel().isClientSide()) {
			return;
		}
		if (ticksExisted % 20 == 0) {
			sync();
		}
		if (isPowered()) {
			return;
		}
		if (getCooldown() > 0) {
			cooldown--;
			return;
		}
		if (getMana() < getManaPerUse()) {
			return;
		}

		BlockPos breakCoords = getValidBreakPos();

		if (breakCoords == null) {
			return;
		}

		Player player = null;
		//TODO 需要更多测试来确定是否可行
		if (getOwnerUUID().isPresent()) {
			player = PlayerUtil.createFakePlayer((ServerLevel) getLevel(), getOwnerUUID().get());
		}

		breakBlock(getLevel(), breakCoords, player);
		setCooldown(getAfterEatCooldown());
		addMana(-getManaPerUse());
		sync();
	}

	@Nullable
	public BlockPos getValidBreakPos() {
		Map<BlockPos, Float> coordsMap = new HashMap<>();
		Random rng = new Random();
		BlockPos pos = getEffectivePos();

		for (BlockPos pos_ : BlockPos.betweenClosed(pos.offset(-getRange().getX(), 0, -getRange().getZ()),
				pos.offset(getRange().getX(), getRange().getY(), getRange().getZ()))) {
			BlockState state = getLevel().getBlockState(pos_);
			if (state.is(BlockTags.LOGS)) {
				coordsMap.put(pos_.immutable(), 2.0f * rng.nextFloat());
			}
		}

		if (coordsMap.isEmpty()) {
			return null;
		}

		float maxDistance = 0F;
		for (float distance : coordsMap.values()) {
			maxDistance = Math.max(maxDistance, distance);
		}

		float finalMaxDistance = maxDistance;
		coordsMap.values().removeIf(dist -> dist < finalMaxDistance - 1f);
		List<BlockPos> coords = new ArrayList<>(coordsMap.keySet());

		BlockPos breakCoords = coords.get(level.getRandom().nextInt(coords.size()));
		return breakCoords;
	}

	protected static void breakBlock(Level level, BlockPos pos, @Nullable Player player) {
		BlockState state = level.getBlockState(pos);

		if (player != null) {
			ToolCommons.removeBlockWithDrops(player, ItemStack.EMPTY, level, pos, (b) -> true);
		} else {
			level.destroyBlock(pos, true);
		}

		if (BotaniaConfig.common().blockBreakParticles()) {
			level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(state));
		}
		level.gameEvent(null, GameEvent.BLOCK_DESTROY, pos);
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
		super.setPlacedBy(level, pos, state, placer, stack);
		if (placer instanceof Player player) {
			setOwnerUUID(player.getGameProfile().getId());
		}
	}

	public int getCooldown() {
		return cooldown;
	}

	public void setCooldown(int ticks) {
		this.cooldown = ticks;
	}

	public int getAfterEatCooldown() {
		return ExtraBotanyConfig.common().woodieniaCooldown();
	}

	public int getManaPerUse() {
		return ExtraBotanyConfig.common().woodieniaWorkManaCost();
	}

	public Vec3i getRange() {
		int[] range = ExtraBotanyConfig.common().woodieniaRange();
		return new Vec3i(range[0], range[1], range[2]);
	}

	@Override
	public int getMaxMana() {
		return ExtraBotanyConfig.common().woodieniaMaxMana();
	}

	@Override
	public int getColor() {
		return 0x664422;
	}

	@Override
	public @Nullable RadiusDescriptor getRadius() {
		return new RadiusDescriptor.Rectangle(getEffectivePos(), new AABB(
				Vec3.atLowerCornerOf(getEffectivePos().offset(-getRange().getX(), 0, -getRange().getZ())),
				Vec3.atLowerCornerOf(getEffectivePos().offset(getRange().getX() + 1, 0, getRange().getZ() + 1))));
	}

	@Override
	public void writeToPacketNBT(CompoundTag cmp) {
		super.writeToPacketNBT(cmp);

		cmp.putInt(TAG_COOLDOWN, getCooldown());
		getOwnerUUID().ifPresent(uuid -> cmp.putUUID(TAG_OWNER_UUID, uuid));
	}

	@Override
	public void readFromPacketNBT(CompoundTag cmp) {
		super.readFromPacketNBT(cmp);

		cooldown = cmp.getInt(TAG_COOLDOWN);
		if (cmp.contains(TAG_OWNER_UUID)) {
			setOwnerUUID(cmp.getUUID(TAG_OWNER_UUID));
		}
	}

	public Optional<UUID> getOwnerUUID() {
		return ownerUUID == null ? Optional.empty() : Optional.of(ownerUUID);
	}

	public void setOwnerUUID(UUID uuid) {
		this.ownerUUID = uuid;
	}
}
