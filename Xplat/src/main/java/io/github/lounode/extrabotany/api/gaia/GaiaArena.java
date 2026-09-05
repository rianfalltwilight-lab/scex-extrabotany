package io.github.lounode.extrabotany.api.gaia;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.client.fx.SparkleParticleData;
import vazkii.botania.client.fx.WispParticleData;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.entity.GaiaGuardianEntity;
import vazkii.botania.common.entity.MagicLandmineEntity;
import vazkii.botania.common.entity.PixieEntity;
import vazkii.botania.common.helper.MathHelper;
import vazkii.botania.common.helper.PlayerHelper;
import vazkii.botania.common.helper.VecHelper;
import vazkii.botania.common.lib.BotaniaTags;
import vazkii.botania.common.proxy.Proxy;

import io.github.lounode.extrabotany.common.entity.MagicLandMineEntity;
import io.github.lounode.extrabotany.common.entity.gaia.Gaia;
import io.github.lounode.extrabotany.common.lib.RegistryHelper;
import io.github.lounode.extrabotany.common.telemetry.ExtraBotanyTelemetry;
import io.github.lounode.extrabotany.xplat.ExtraBotanyConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static vazkii.botania.common.helper.PlayerHelper.isTruePlayer;

public class GaiaArena {
	public static final Codec<GaiaArena> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			GlobalPos.CODEC.fieldOf("center").forGetter(GaiaArena::center),
			Codec.FLOAT.fieldOf("radius").forGetter(GaiaArena::radius),
			Codec.INT.fieldOf("height").forGetter(GaiaArena::height))
			.apply(instance, GaiaArena::of)
	);

	public static final float ARENA_RANGE = 12F;
	public static final int ARENA_HEIGHT = 5;
	private static final TagKey<Block> BLACKLIST = BotaniaTags.Blocks.GAIA_GUARDIAN_IMMUNE;
	public static final List<BlockPos> PYLON_LOCATIONS = ImmutableList.of(
			new BlockPos(4, 1, 4),
			new BlockPos(4, 1, -4),
			new BlockPos(-4, 1, 4),
			new BlockPos(-4, 1, -4)
	);

	public static final Predicate<BlockInWorld>[][][] ARENA_PATTERN = BlockPatternExtendBuilder.start()
			.aisle(
					"P_______P",
					"_________",
					"_________",
					"_________",
					"_________",
					"_________",
					"_________",
					"_________",
					"P_______P"
			)
			.aisle(
					"_________",
					"_________",
					"_________",
					"_________",
					"____B____",
					"_________",
					"_________",
					"_________",
					"_________"
			)
			.aisle(
					"_________",
					"_________",
					"_________",
					"___III___",
					"___III___",
					"___III___",
					"_________",
					"_________",
					"_________"
			)
			.where('_', BlockInWorld.hasState(BlockStatePredicate.ANY))
			.where('B', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.BEACON)))
			.where('I', BlockInWorld.hasState(BlockTagPredicate.forTag(BlockTags.BEACON_BASE_BLOCKS)))
			.where('P', BlockInWorld.hasState(BlockStatePredicate.forBlock(BotaniaBlocks.GAIA_PYLON)))
			.createPattern();
	public static final Predicate<BlockInWorld>[][][] PYLONS_PATTERN = BlockPatternExtendBuilder.start()
			.aisle(
					"P_______P",
					"_________",
					"_________",
					"_________",
					"_________",
					"_________",
					"_________",
					"_________",
					"P_______P"
			)
			.where('_', BlockInWorld.hasState(BlockStatePredicate.ANY))
			.where('P', BlockInWorld.hasState(BlockStatePredicate.forBlock(BotaniaBlocks.GAIA_PYLON)))
			.createPattern();

	private static List<String> CACHED_BYPASS_CONFIG = new ArrayList<>();
	private static final List<String> CACHED_BYPASS_MODIDS = new ArrayList<>();
	private static final List<String> CACHED_BYPASS_ITEMS = new ArrayList<>();

	private final GlobalPos center;
	private final float radius;
	private final int height;

	//

	private GaiaArena(GlobalPos center, float radius, int height) {
		this.center = center;
		this.radius = radius;
		this.height = height;
	}

	public static GaiaArena of(GlobalPos center, float radius, int height) {
		return new GaiaArena(center, radius, height);
	}

	public static GaiaArena of(GlobalPos center) {
		return GaiaArena.of(center, ARENA_RANGE, ARENA_HEIGHT);
	}

	public GlobalPos center() {
		return this.center;
	}

	public float radius() {
		return this.radius;
	}

	public int height() {
		return this.height;
	}

	public boolean isSameCenter(GaiaArena other) {
		return this.center.equals(other.center);
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		} else if (other != null && this.getClass() == other.getClass()) {
			GaiaArena gaiaArena = (GaiaArena) other;
			return (Objects.equals(this.center, gaiaArena.center) &&
					Objects.equals(this.radius, gaiaArena.radius) &&
					Objects.equals(this.height, gaiaArena.height));
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(new Object[] { this.center, this.radius, this.height });
	}

	@Override
	public String toString() {
		return "GaiaArena{" + "center=" + this.center +
				", radius=" + this.radius +
				", height=" + this.height +
				'}';
	}

	public void tick(Gaia gaia) {
		if (gaia.level().isClientSide()) {
			particles(gaia);
		}

		List<Player> players = getPlayersAround(gaia.level());

		if (players.isEmpty() && !gaia.level().players().isEmpty()) {
			gaia.discard();
			ExtraBotanyTelemetry.Event.onGaiaBattleFinish(gaia);
		}

		if (!gaia.level().isClientSide()) {
			for (Player player : players) {
				keepInsideArena(player);
			}
		}
	}

	public void cleanup(Level level) {
		if (level.isClientSide()) {
			return;
		}

		// Clear wither from nearby players
		for (Player player : getPlayersAround(level)) {
			if (player.getEffect(MobEffects.WITHER) != null) {
				player.removeEffect(MobEffects.WITHER);
			}
		}

		// Stop all the pixies leftover from the fight
		for (PixieEntity pixie : level.getEntitiesOfClass(PixieEntity.class, getArenaBB(), p -> p.isAlive() && p.isGaia())) {
			pixie.spawnAnim();
			pixie.discard();
		}

		for (MagicLandmineEntity landmine : level.getEntitiesOfClass(MagicLandmineEntity.class, getArenaBB())) {
			landmine.discard();
		}

		for (MagicLandMineEntity mine : level.getEntitiesOfClass(MagicLandMineEntity.class, getArenaBB())) {
			mine.discard();
		}
	}

	public void keepInsideArena(Player player) {
		if (MathHelper.pointDistanceSpace(player.getX(), player.getY(), player.getZ(), center().pos().getX() + 0.5, center().pos().getY() + 0.5, center().pos().getZ() + 0.5) >= radius()) {
			Vec3 sourceVector = new Vec3(center().pos().getX() + 0.5, center().pos().getY() + 0.5, center().pos().getZ() + 0.5);
			Vec3 playerVector = VecHelper.fromEntityCenter(player);
			Vec3 motion = sourceVector.subtract(playerVector).normalize();

			player.setDeltaMovement(motion.x, 0.2, motion.z);
			player.hurtMarked = true;
		}
	}

	public void particles(Gaia gaia) {
		for (int i = 0; i < 360; i += 8) {
			float r = 0.6F;
			float g = 0F;
			float b = 0.2F;
			float m = 0.15F;
			float mv = 0.35F;

			float rad = i * (float) Math.PI / 180F;
			double x = center().pos().getX() + 0.5 - Math.cos(rad) * radius();
			double y = center().pos().getY() + 0.5;
			double z = center().pos().getZ() + 0.5 - Math.sin(rad) * radius();

			WispParticleData data = WispParticleData.wisp(0.5F, r, g, b);
			gaia.level().addParticle(data, x, y, z, (float) (Math.random() - 0.5F) * m, (float) (Math.random() - 0.5F) * mv, (float) (Math.random() - 0.5F) * m);
		}

		if (gaia.getInvulTime() > 10) {
			Vec3 pos = VecHelper.fromEntityCenter(gaia).subtract(0, 0.2, 0);
			for (BlockPos arr : PYLON_LOCATIONS) {
				Vec3 pylonPos = new Vec3(center().pos().getX() + arr.getX(), center().pos().getY() + arr.getY(), center().pos().getZ() + arr.getZ());
				double worldTime = gaia.tickCount;
				worldTime /= 5;

				float rad = 0.75F + (float) Math.random() * 0.05F;
				double xp = pylonPos.x + 0.5 + Math.cos(worldTime) * rad;
				double zp = pylonPos.z + 0.5 + Math.sin(worldTime) * rad;

				Vec3 partPos = new Vec3(xp, pylonPos.y, zp);
				Vec3 mot = pos.subtract(partPos).scale(0.04);

				float r = 0.7F + (float) Math.random() * 0.3F;
				float g = (float) Math.random() * 0.3F;
				float b = 0.7F + (float) Math.random() * 0.3F;

				WispParticleData data = WispParticleData.wisp(0.25F + (float) Math.random() * 0.1F, r, g, b, 1);
				gaia.level().addParticle(data, partPos.x, partPos.y, partPos.z, 0, -(-0.075F - (float) Math.random() * 0.015F), 0);
				WispParticleData data1 = WispParticleData.wisp(0.4F, r, g, b);
				gaia.level().addParticle(data1, partPos.x, partPos.y, partPos.z, (float) mot.x, (float) mot.y, (float) mot.z);
			}
		}
	}

	/// Useful Checks
	public boolean checksVanilla(Player player, Level world, BlockPos pos) {
		if (!(world.getBlockEntity(pos) instanceof BeaconBlockEntity) ||
				!isTruePlayer(player) ||
				countGaiaAround(world, GaiaGuardianEntity.class) > 0) {
			return false;
		}
		if (!checkDifficulty(world)) {
			if (!world.isClientSide()) {
				player.sendSystemMessage(Component.translatable("botaniamisc.peacefulNoob").withStyle(ChatFormatting.RED));
			}
			return false;
		}

		if (!checkPylons(player, world)) {
			return false;
		}

		//check arena shape
		var invalidArenaBlocks = checkArea(world);
		if (!invalidArenaBlocks.isEmpty()) {
			if (world.isClientSide) {
				warnInvalidBlocks(world, invalidArenaBlocks);
				showRadius(world);
			} else {
				player.sendSystemMessage(Component.translatable("botaniamisc.badArena").withStyle(ChatFormatting.RED));
			}

			return false;
		}
		return true;
	}

	public boolean checksModern(Player player, Level world, ItemStack stack) {
		if (!(world.getBlockEntity(center().pos()) instanceof BeaconBlockEntity) ||
				!isTruePlayer(player) ||
				(countGaiaAround(world, Gaia.class) + countGaiaAround(world, GaiaGuardianEntity.class)) > 0) {
			return false;
		}

		if (!checkDifficulty(world)) {
			if (!world.isClientSide()) {
				player.sendSystemMessage(Component.translatable("botaniamisc.peacefulNoob").withStyle(ChatFormatting.RED));
			}
			return false;
		}

		if (world.isClientSide()) {
			if (!checkStructure(world, ARENA_PATTERN)) {
				return false;
			}
		} else {
			var result = checkStructureNeedsMatchResult(world, ARENA_PATTERN);
			if (result instanceof BlockPatternExtend.BlockPatternMatchFail fail) {
				if (!world.isClientSide()) {
					List<BlockPos> invalidPylonBlocks = fail.getFailedBlocks().keySet().stream().toList();
					warnInvalidBlocksServer((ServerPlayer) player, (ServerLevel) world, invalidPylonBlocks);
					player.sendSystemMessage(Component.translatable("message.extrabotany.chat.bad_struct", stack.getItem().getDescription()).withStyle(ChatFormatting.RED));
				}
				return false;
			}
		}

		var invalidArenaBlocks = checkArea(world);
		if (!invalidArenaBlocks.isEmpty()) {
			if (world.isClientSide) {
				warnInvalidBlocks(world, invalidArenaBlocks);
				showRadius(world);
			} else {
				//warnInvalidBlocksServer((ServerPlayer) player, (ServerLevel) world, invalidPylonBlocks);
				player.sendSystemMessage(Component.translatable("botaniamisc.badArena").withStyle(ChatFormatting.RED));
			}

			return false;
		}

		return true;
	}

	public static boolean checkDifficulty(Level world) {
		return world.getDifficulty() != Difficulty.PEACEFUL;
	}

	public boolean checkPylons(Player player, Level world) {
		return checkPylons(player, world, center().pos(), PYLON_LOCATIONS);
	}

	public static boolean checkPylons(Player player, Level world, BlockPos beaconPos, List<BlockPos> pylonLocations) {
		List<BlockPos> invalidPylonBlocks = getInvalidPylons(world, beaconPos, pylonLocations);
		if (!invalidPylonBlocks.isEmpty()) {
			if (world.isClientSide()) {
				warnInvalidBlocks(world, invalidPylonBlocks);
			} else {
				player.sendSystemMessage(Component.translatable("botaniamisc.needsCatalysts").withStyle(ChatFormatting.RED));
			}

			return false;
		}
		return true;
	}

	public boolean checkStructure(Level world, Predicate<BlockInWorld>[][][] pattern) {
		return checkStructure(world, center().pos(), pattern);
	}

	public static boolean checkStructure(Level world, BlockPos beaconPos, Predicate<BlockInWorld>[][][] pattern) {
		return new BlockPatternExtend(pattern).findFlat(world, beaconPos) != null;
	}

	public BlockPatternExtend.MatchResult checkStructureNeedsMatchResult(Level world, Predicate<BlockInWorld>[][][] pattern) {
		return checkStructureNeedsMatchResult(world, center().pos(), pattern);
	}

	@SuppressWarnings("deprecation")
	public static BlockPatternExtend.MatchResult checkStructureNeedsMatchResult(Level world, BlockPos beaconPos, Predicate<BlockInWorld>[][][] pattern) {
		return new BlockPatternExtend(pattern).findFlatWithFailResult(world, beaconPos);
	}

	public List<BlockPos> checkArea(Level world) {
		return checkArena(world, center().pos(), radius(), height(), BLACKLIST);
	}

	public static List<BlockPos> checkArena(Level world, BlockPos beaconPos, float arenaRange, float arenaHeight, TagKey<Block> blackList) {
		return getInvalidArenaBlocks(world, beaconPos, arenaRange, arenaHeight, blackList);
	}

	public static List<BlockPos> getInvalidArenaBlocks(Level world, BlockPos beaconPos, float arenaRange, float arenaHeight, TagKey<Block> blackList) {
		List<BlockPos> trippedPositions = new ArrayList<>();
		int range = (int) Math.ceil(arenaRange);
		BlockPos pos;

		for (int x = -range; x <= range; x++) {
			for (int z = -range; z <= range; z++) {
				if (Math.abs(x) == 4 && Math.abs(z) == 4 || MathHelper.pointDistancePlane(x, z, 0, 0) > arenaRange) {
					continue; // Ignore pylons and out of circle
				}

				boolean hasFloor = false;

				for (int y = -2; y <= arenaHeight; y++) {
					if (x == 0 && y == 0 && z == 0) {
						continue; //the beacon
					}

					pos = beaconPos.offset(x, y, z);

					BlockState state = world.getBlockState(pos);

					boolean allowBlockHere = y < 0;
					boolean isBlockHere = !state.getCollisionShape(world, pos).isEmpty();

					if (allowBlockHere && isBlockHere) //floor is here! good
					{
						hasFloor = true;
					}

					if (y == 0 && !hasFloor) //column is entirely missing floor
					{
						trippedPositions.add(pos.below());
					}

					if (!allowBlockHere && isBlockHere && !state.is(blackList)) //ceiling is obstructed in this column
					{
						trippedPositions.add(pos);
					}
				}
			}
		}

		return trippedPositions;
	}

	public boolean checkInventory(Level level) {
		List<Player> players = getPlayersAround(level);
		for (Player player : players) {
			if (!checkInventoryPass(player)) {
				return false;
			}
		}
		return true;
	}

	public boolean checkGuardianInventoryStrict(Level level, net.minecraft.world.item.Item bypass) {
		return getPlayersAround(level).stream().allMatch(player -> bypass != null && playerHasItem(player, bypass) || checkGuardianInventoryPass(player));
	}
	public boolean checkGuardianInventoryStrict(Level level, ResourceLocation bypassAdvancement) {
		return getPlayersAround(level).stream().allMatch(player -> bypassAdvancement != null && player instanceof ServerPlayer server
				&& PlayerHelper.hasAdvancement(server, bypassAdvancement) || checkGuardianInventoryPass(player));
	}
	public static boolean playerHasItem(Player player, net.minecraft.world.item.Item item) {
		for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) if (player.getInventory().getItem(slot).is(item)) return true;
		var worn = vazkii.botania.common.handler.EquipmentHandler.getAllWorn(player);
		if (worn != null) for (int slot = 0; slot < worn.getContainerSize(); slot++) if (worn.getItem(slot).is(item)) return true;
		return false;
	}
	public static boolean checkGuardianInventoryPass(Player player) {
		if (player.isCreative() || !ExtraBotanyConfig.common().guardianItemCheck()) return true;
		for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) if (!checkGuardianAllowed(player.getInventory().getItem(slot))) return false;
		var worn = vazkii.botania.common.handler.EquipmentHandler.getAllWorn(player);
		if (worn != null) for (int slot = 0; slot < worn.getContainerSize(); slot++) if (!checkGuardianAllowed(worn.getItem(slot))) return false;
		return true;
	}
	private static final java.util.Set<String> GUARDIAN_ALLOWED_ITEMS = java.util.Set.of("enigmaticlegacy:cursed_ring", "enigmaticlegacyplus:cursed_ring",
			"celestial_artifacts:catastrophe_scroll", "celestial_artifacts:chaotic_etching", "celestial_artifacts:origin_etching", "celestial_artifacts:life_etching",
			"celestial_artifacts:truth_etching", "celestial_artifacts:desire_etching", "celestial_artifacts:nihility_etching", "celestial_artifacts:end_etching");
	public static boolean checkGuardianAllowed(ItemStack stack) {
		if (stack.isEmpty()) return true;
		var id = BuiltInRegistries.ITEM.getKey(stack.getItem());
		return java.util.Set.of("minecraft", "botania", "extrabotany", "mythicbotany").contains(id.getNamespace()) || GUARDIAN_ALLOWED_ITEMS.contains(id.toString());
	}
	public static boolean checkInventoryPass(Player player) {
		if (player.isCreative() || ExtraBotanyConfig.common().disableGaiaDisArm() || !ExtraBotanyConfig.common().guardianItemCheck()) {
			return true;
		}
		for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
			final ItemStack stack = player.getInventory().getItem(i);
			if (!checkFeasibility(stack)) {
				return false;
			}
		}
		return true;
	}

	public static boolean checkFeasibility(ItemStack stack) {
		if (stack.isEmpty()) {
			return true;
		}
		if (CACHED_BYPASS_CONFIG != ExtraBotanyConfig.common().gaiaSpawnUnCheckList()) {
			updateCache(ExtraBotanyConfig.common().gaiaSpawnUnCheckList());
		}

		ResourceLocation itemLocation = RegistryHelper.getRegistryName(stack.getItem());
		String modid = itemLocation.getNamespace();
		if (CACHED_BYPASS_MODIDS.contains(modid)) {
			return true;
		}
		if (CACHED_BYPASS_ITEMS.contains(itemLocation.toString())) {
			return true;
		}

		return false;
	}

	public static void updateCache(List<String> newBypassList) {
		CACHED_BYPASS_CONFIG = newBypassList;
		CACHED_BYPASS_MODIDS.clear();
		CACHED_BYPASS_ITEMS.clear();

		for (var s : CACHED_BYPASS_CONFIG) {
			if (!s.contains(":")) {
				CACHED_BYPASS_MODIDS.add(s);
			} else {
				ResourceLocation itemLocation = ResourceLocation.tryParse(s);
				if (itemLocation == null || !BuiltInRegistries.ITEM.containsKey(itemLocation)) {
					continue;
				}
				CACHED_BYPASS_ITEMS.add(itemLocation.toString());
			}
		}
	}

	public int countGaiaAround(Level world, Class<? extends Entity> gaiaClass) {
		return world.getEntitiesOfClass(gaiaClass, getArenaBB()).size();
	}

	public static int countGaiaAround(Level world, BlockPos center, float radius, Class<? extends Entity> gaiaClass) {
		return world.getEntitiesOfClass(gaiaClass, getArenaBB(center, radius)).size();
	}

	@NotNull
	public AABB getArenaBB() {
		return getArenaBB(radius());
	}

	@NotNull
	public AABB getArenaBB(double radius) {
		return getArenaBB(center().pos(), radius);
	}

	@NotNull
	public static AABB getArenaBB(BlockPos center, double radius) {
		return new AABB(
				center.getX() + 0.5,
				center.getY() + 0.5,
				center.getZ() + 0.5,
				center.getX() + 0.5,
				center.getY() + 0.5,
				center.getZ() + 0.5
		).inflate(radius);
	}

	public void showRadius(Level world) {
		showRadius(world, center().pos(), radius());
	}

	public static void showRadius(Level world, BlockPos center, double radius) {
		SparkleParticleData data = SparkleParticleData.sparkle(5F, 1, 0, 1, 120);
		for (int i = 0; i < 360; i += 8) {
			float rad = i * (float) Math.PI / 180F;
			double wx = center.getX() + 0.5 - Math.cos(rad) * radius;
			double wy = center.getY() + 0.5;
			double wz = center.getZ() + 0.5 - Math.sin(rad) * radius;
			Proxy.INSTANCE.addParticleForceNear(world, data, wx, wy, wz, 0, 0, 0);
		}
	}

	public List<Player> getPlayersAround(Level level) {
		return PlayerHelper.getRealPlayersIn(level, getArenaBB(radius() + 3.0D));
	}

	public static void warnInvalidBlocks(Level world, Iterable<BlockPos> invalidPositions) {
		WispParticleData data = WispParticleData.wisp(0.5F, 1, 0.2F, 0.2F, 8, false);
		for (BlockPos pos_ : invalidPositions) {
			world.addParticle(data, pos_.getX() + 0.5, pos_.getY() + 0.5, pos_.getZ() + 0.5, 0, 0, 0);
		}
	}

	public static void warnInvalidBlocksServer(ServerPlayer player, ServerLevel level, Iterable<BlockPos> invalidPositions) {
		WispParticleData data = WispParticleData.wisp(0.5F, 1, 0.2F, 0.2F, 8, false);
		for (BlockPos pos_ : invalidPositions) {
			level.sendParticles(player, data, false, pos_.getX() + 0.5, pos_.getY() + 0.5, pos_.getZ() + 0.5, 1, 0, 0, 0, 0);
			//level.sendParticles(player, data, false, pos_.getX() + 0.5, pos_.getY() + 0.5, pos_.getZ(), 1, 0,0,0);
			//.addParticle(data, pos_.getX() + 0.5, pos_.getY() + 0.5, pos_.getZ() + 0.5, 0, 0, 0);
		}
	}

	public static List<BlockPos> getInvalidPylons(Level world, BlockPos beaconPos, List<BlockPos> pylonLocations) {
		List<BlockPos> invalidPylonBlocks = new ArrayList<>();

		for (BlockPos coords : pylonLocations) {
			BlockPos pos_ = beaconPos.offset(coords);

			BlockState state = world.getBlockState(pos_);
			if (!state.is(BotaniaBlocks.GAIA_PYLON)) {
				invalidPylonBlocks.add(pos_);
			}
		}

		return invalidPylonBlocks;
	}
}
