package io.github.lounode.extrabotany.fabric;

import vazkii.botania.xplat.XplatAbstractions;

import io.github.lounode.extrabotany.api.ExtraBotanyAPI;
import io.github.lounode.extrabotany.common.block.flower.functional.*;
import io.github.lounode.extrabotany.common.block.flower.generating.*;
import io.github.lounode.extrabotany.common.lib.LibMisc;
import io.github.lounode.extrabotany.xplat.ExtraBotanyConfig;

import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

import static io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.ConfigTypes.*;

import io.github.fablabsmc.fablabs.api.fiber.v1.builder.ConfigTreeBuilder;
import io.github.fablabsmc.fablabs.api.fiber.v1.exception.ValueDeserializationException;
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.ConfigTypes;
import io.github.fablabsmc.fablabs.api.fiber.v1.serialization.FiberSerialization;
import io.github.fablabsmc.fablabs.api.fiber.v1.serialization.JanksonValueSerializer;
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigTree;
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.PropertyMirror;

public class FabricExtraBotanyConfig {
	private static final Client CLIENT = new Client();
	private static final COMMON COMMON = new COMMON();

	public static void setup() {
		try {
			Files.createDirectory(Paths.get("config"));
		} catch (FileAlreadyExistsException ignored) {} catch (IOException e) {
			ExtraBotanyAPI.LOGGER.warn("Failed to make config dir", e);
		}

		JanksonValueSerializer serializer = new JanksonValueSerializer(false);
		ConfigTree common = COMMON.configure(ConfigTree.builder());
		setupConfig(common, Paths.get("config", LibMisc.MOD_ID + "-common.json5"), serializer);
		ExtraBotanyConfig.setCommon(COMMON);

		if (XplatAbstractions.INSTANCE.isPhysicalClient()) {
			ConfigTree client = CLIENT.configure(ConfigTree.builder());
			setupConfig(client, Paths.get("config", LibMisc.MOD_ID + "-client.json5"), serializer);
			ExtraBotanyConfig.setClient(CLIENT);
		}
		ExtraBotanyConfig.resetPatchouliFlags();
	}

	private static class Client implements ExtraBotanyConfig.ClientConfigAccess {
		public final PropertyMirror<Boolean> otakuMode = PropertyMirror.create(BOOLEAN);

		public ConfigTree configure(ConfigTreeBuilder builder) {
			builder.fork("client")
					.beginValue("otakuMode", BOOLEAN, false)
					.withComment("""
							设为 true 来开启二刺螈模式
							（将会启用一些浓度较高、发癫的文本显示）
							
							Set true to enable Otaku Mode.
							(Enables otaku-style text display)
							""")
					.finishValue(otakuMode::mirror)
					.finishBranch();
			return builder.build();
		}

		@Override
		public boolean otakuMode() {
			return otakuMode.getValue();
		}
	}

	private static class COMMON implements ExtraBotanyConfig.ConfigAccess {
		public final PropertyMirror<Boolean> disableGaiaDisArm = PropertyMirror.create(BOOLEAN);
		public final PropertyMirror<Boolean> enableTelemetry = PropertyMirror.create(BOOLEAN);
		public final PropertyMirror<String> telemetryUUID = PropertyMirror.create(STRING);
		public final PropertyMirror<String> fakePlayerId = PropertyMirror.create(STRING);
		public final PropertyMirror<int[]> woodieniaRange = PropertyMirror.create(ConfigTypes.makeIntArray(INTEGER));
		public final PropertyMirror<Integer> woodieniaCooldown = PropertyMirror.create(INTEGER);
		public final PropertyMirror<Integer> woodieniaMaxMana = PropertyMirror.create(INTEGER);
		public final PropertyMirror<Integer> woodieniaWorkManaCost = PropertyMirror.create(INTEGER);
		public final PropertyMirror<Integer> reikarlilyMaxMana = PropertyMirror.create(INTEGER);
		public final PropertyMirror<Integer> reikarlilyProduceCooldown = PropertyMirror.create(INTEGER);
		public final PropertyMirror<Integer> reikarlilyProduceMana = PropertyMirror.create(INTEGER);
		public final PropertyMirror<Integer> reikarlilyPassiveGenerateTime = PropertyMirror.create(INTEGER);
		public final PropertyMirror<Integer> reikarlilyPassiveGenerateMana = PropertyMirror.create(INTEGER);
		public final PropertyMirror<Integer> reikarlilySpawnLightningCooldown = PropertyMirror.create(INTEGER);
		public final PropertyMirror<Integer> tradeOrchidMaxMana = PropertyMirror.create(INTEGER);
		public final PropertyMirror<Integer> tradeOrchidManaCost = PropertyMirror.create(INTEGER);
		public final PropertyMirror<Integer> tradeOrchidCooldown = PropertyMirror.create(INTEGER);
		public final PropertyMirror<Double> tradeOrchidDiscountPercentage = PropertyMirror.create(DOUBLE);
		public final PropertyMirror<Integer> bellflowerMaxMana = PropertyMirror.create(INTEGER);
		public final PropertyMirror<Double> bellflowerGenerateModify = PropertyMirror.create(DOUBLE);
		public final PropertyMirror<Integer> annoyingflowerMaxMana = PropertyMirror.create(INTEGER);
		public final PropertyMirror<Integer> annoyingflowerFishingCost = PropertyMirror.create(INTEGER);
		public final PropertyMirror<Integer> annoyingflowerCooldown = PropertyMirror.create(INTEGER);
		public final PropertyMirror<Integer> annoyingflowerFoodBoostMax = PropertyMirror.create(INTEGER);
		public final PropertyMirror<Integer> annoyingflowerFoodBoostTimes = PropertyMirror.create(INTEGER);
		public final PropertyMirror<Double> annoyingflowerFoodBoostCooldownMultiplier = PropertyMirror.create(DOUBLE);
		public final PropertyMirror<Integer> stonesiaMaxMana = PropertyMirror.create(INTEGER);
		public final PropertyMirror<Integer> stonesiaCooldown = PropertyMirror.create(INTEGER);
		public final PropertyMirror<Integer> edelweissMaxMana = PropertyMirror.create(INTEGER);
		public final PropertyMirror<Integer> edelweissCooldown = PropertyMirror.create(INTEGER);
		public final PropertyMirror<Integer> resoncundMaxMana = PropertyMirror.create(INTEGER);
		public final PropertyMirror<Integer> resoncundLossPerHeard = PropertyMirror.create(INTEGER);
		public final PropertyMirror<Integer> sunshineLilyMaxMana = PropertyMirror.create(INTEGER);
		public final PropertyMirror<Integer> sunshineLilyProduceMana = PropertyMirror.create(INTEGER);
		public final PropertyMirror<Integer> moonlightLilyMaxMana = PropertyMirror.create(INTEGER);
		public final PropertyMirror<Integer> moonlightLilyProduceMana = PropertyMirror.create(INTEGER);
		public final PropertyMirror<Integer> twinstarMaxMana = PropertyMirror.create(INTEGER);
		public final PropertyMirror<Integer> twinstarMaxTemperature = PropertyMirror.create(INTEGER);
		public final PropertyMirror<Integer> twinstarMinTemperature = PropertyMirror.create(INTEGER);
		public final PropertyMirror<Integer> omnivioletMaxMana = PropertyMirror.create(INTEGER);
		public final PropertyMirror<Integer> tinkleMaxMana = PropertyMirror.create(INTEGER);
		public final PropertyMirror<Integer> tinkleProduceMana = PropertyMirror.create(INTEGER);
		public final PropertyMirror<Integer> bloodEnchantressMaxMana = PropertyMirror.create(INTEGER);
		public final PropertyMirror<Integer> bloodEnchantressProduceMana = PropertyMirror.create(INTEGER);
		public final PropertyMirror<Integer> serenitianRange = PropertyMirror.create(INTEGER);
		public final PropertyMirror<Integer> mirrowtuniaMaxMana = PropertyMirror.create(INTEGER);
		public final PropertyMirror<Integer> mirrowtuniaEffectCost = PropertyMirror.create(INTEGER);
		public final PropertyMirror<Integer> necrofleurMaxMana = PropertyMirror.create(INTEGER);
		public final PropertyMirror<Integer> manalinkTransferSpeed = PropertyMirror.create(INTEGER);
		public final PropertyMirror<Integer> enchanterTransformCost = PropertyMirror.create(INTEGER);
		public final PropertyMirror<Integer> enchanterConsumeSpeed = PropertyMirror.create(INTEGER);
		public final PropertyMirror<List<String>> gaiaSpawnUnCheckList = PropertyMirror.create(ConfigTypes.makeList(ConfigTypes.STRING));

		public ConfigTree configure(ConfigTreeBuilder builder) {
			builder

					.fork("server")
					.fork("telemetry")

					.beginValue("enableTelemetry", BOOLEAN, true)
					.withComment("""
							我们使用遥测数据来提供更好的游玩体验。
							以下数据在您的游玩过程中将被收集：
								- 盖亚三通过率
								- 其他...
							
							您可以在 https://github.com/Lounode/Extrabotany 上找到更多
							如果您不想被收集这些数据，在下方将配置项改为 false
							
							We use telemetry data to provide a better gameplay experience.
							The following data will be collected during your play session:
								- Gaia III completion rate
								- etc...
							
							Find more on: https://github.com/Lounode/Extrabotany
							If you prefer not to participate, set the option below to false.
							""")
					.finishValue(enableTelemetry::mirror)

					.beginValue("telemetryUUID", STRING, UUID.randomUUID().toString())
					.withComment("""
							遥测数据UUID
							The UUID of the telemetry data
							""")
					.finishValue(telemetryUUID::mirror)
					.finishBranch()//End telemetry

					.fork("gaia")
					.beginValue("disableGaiaDisarm", BOOLEAN, false)
					.withComment("""
							设为 true 来禁用盖亚的缴械技能
							Set true to disable Gaia's disarm
							""")
					.finishValue(disableGaiaDisArm::mirror)
					.beginValue("gaiaSpawnUnCheckList", ConfigTypes.makeList(STRING), List.of("minecraft", "botania", "extrabotany"))
					.withComment("""
							盖亚三生成时不检查的ModID或者物品
							示例：minecraft, sophisticatedbackpacks:backpack
							Items or ModIDs that gaia ignore to check when spawn
							e.g. minecraft, sophisticatedbackpacks:backpack""")
					.finishValue(gaiaSpawnUnCheckList::mirror)
					.finishBranch()//End gaia

					.fork("fakePlayer")
					.beginValue("fakePlayerId", STRING, "[Extrabotany]")
					.withComment("""
							假玩家ID（用于权限配置）
							Fake Player ID (for permission configuration)
							""")
					.finishValue(fakePlayerId::mirror)
					.finishBranch()//End fakePlayer

					.fork("flower")
					//TradeOrchid
					.fork("tradeOrchid").withComment("""
							商友兰
							Trade Orchid""")
					.beginValue("maxMana", INTEGER, TradeOrchidBlockEntity.MAX_MANA)
					.withComment("""
							最大魔力值
							Maximum mana""")
					.finishValue(tradeOrchidMaxMana::mirror)
					.beginValue("manaCost", INTEGER, TradeOrchidBlockEntity.MANA_PER_USE)
					.withComment("""
							每只村民消耗的魔力量
							Mana cost per villager""")
					.finishValue(tradeOrchidManaCost::mirror)
					.beginValue("cooldown", INTEGER, TradeOrchidBlockEntity.COOLDOWN)
					.withComment("""
							冷却时间(ticks)
							Cooldown time in ticks""")
					.finishValue(tradeOrchidCooldown::mirror)
					.beginValue("discountPercentage", DOUBLE, TradeOrchidBlockEntity.DISCOUNT_RATE)
					.withComment("""
							折扣百分比(仅支持精确到两位小数)
							(例如：0.85 = 八五折)
							Discount percentage (max precision: 0.01)
							(e.g., 0.85 = 15% off)""")
					.finishValue(tradeOrchidDiscountPercentage::mirror)
					.finishBranch()//End tradeOrchid
					//Woodienia
					.fork("woodienia").withComment("""
							伐木花
							Woodienia""")
					.beginValue("maxMana", INTEGER, 10_000)
					.withComment("""
							最大魔力
							Maximum Mana""")
					.finishValue(woodieniaMaxMana::mirror)
					.beginValue("workManaCost", INTEGER, 200)
					.withComment("""
							破坏原木的魔力消耗
							Cost when break Logs""")
					.finishValue(woodieniaWorkManaCost::mirror)
					.beginValue("range", ConfigTypes.makeIntArray(INTEGER), new int[] { 8, 16, 8 })
					.withComment("""
							以自身为中心的工作范围（±X轴，+Y轴，±Z轴）
							Working range centered on self (±X axis, +Y axis, ±Z axis)""")
					.finishValue(woodieniaRange::mirror)
					.beginValue("cooldown", INTEGER, 10)
					.withComment("""
							工作间隔
							Cooldown interval""")
					.finishValue(woodieniaCooldown::mirror)
					.finishBranch()//End woodienia

					//Reikarlily
					.fork("reikarlily").withComment("""
							雷卡兰
							Reikarlily""")
					.beginValue("maxMana", INTEGER, ReikarlilyBlockEntity.MAX_MANA)
					.withComment("""
							最大魔力
							Maximum Mana""")
					.finishValue(reikarlilyMaxMana::mirror)
					.beginValue("produceCooldown", INTEGER, ReikarlilyBlockEntity.COOLDOWN)
					.withComment("""
							雷击后再次产出魔力的冷却时间
							Cooldown time for regenerating mana after a lightning strike""")
					.finishValue(reikarlilyProduceCooldown::mirror)
					.beginValue("produceMana", INTEGER, ReikarlilyBlockEntity.PRODUCE_MANA)
					.withComment("""
							雷击生成的魔力量
							Mana generated per lightning strike""")
					.finishValue(reikarlilyProduceMana::mirror)
					.beginValue("passiveGenerateTime", INTEGER, ReikarlilyBlockEntity.RESIDUAL_HEAT_AFTER_PRODUCE)
					.withComment("""
							雷击后被动生成魔力的时间
							Passive mana generation duration after lightning strike""")
					.finishValue(reikarlilyPassiveGenerateTime::mirror)
					.beginValue("passiveGenerateMana", INTEGER, ReikarlilyBlockEntity.RESIDUAL_HEAT_PRODUCE_MANA)
					.withComment("""
							雷击后每Tick被动生成的魔力量
							Mana generated per passive tick""")
					.finishValue(reikarlilyPassiveGenerateMana::mirror)
					.beginValue("spawnLightningCooldown", INTEGER, ReikarlilyBlockEntity.SPAWN_LIGHTNING_COOLDOWN)
					.withComment("""
							雨天生成闪电的冷却时间
							Cooldown for spawning lightning when raining""")
					.finishValue(reikarlilySpawnLightningCooldown::mirror)
					.finishBranch()//End reikarlily

					.fork("bellflower").withComment("""
							风铃草
							Bellflower""")
					.beginValue("maxMana", INTEGER, BellflowerBlockEntity.MAX_MANA)
					.withComment("""
							最大魔力值
							Maximum mana""")
					.finishValue(bellflowerMaxMana::mirror)
					.beginValue("generateModify", DOUBLE, BellflowerBlockEntity.GENERATE_MODIFY)
					.withComment("""
							魔力生成修正值
							Mana generation modifier""")
					.finishValue(bellflowerGenerateModify::mirror)
					.finishBranch() // End bellflower
					.fork("annoyingflower").withComment("""
							神烦花
							Annoying Flower""")
					.beginValue("maxMana", INTEGER, AnnoyingFlowerBlockEntity.MAX_MANA)
					.withComment("""
							最大魔力值
							Maximum mana""")
					.finishValue(annoyingflowerMaxMana::mirror)
					.beginValue("fishingCost", INTEGER, AnnoyingFlowerBlockEntity.FISHING_COST)
					.withComment("""
							钓鱼消耗的魔力量
							Mana cost per fishing""")
					.finishValue(annoyingflowerFishingCost::mirror)
					.beginValue("cooldown", INTEGER, AnnoyingFlowerBlockEntity.COOLDOWN_AFTER_WORK)
					.withComment("""
							工作冷却时间(ticks)
							Cooldown time in ticks""")
					.finishValue(annoyingflowerCooldown::mirror)
					.beginValue("foodBoostMax", INTEGER, AnnoyingFlowerBlockEntity.FOOD_BOOST_MAX)
					.withComment("""
							最大食物加成次数
							Maximum food boost""")
					.finishValue(annoyingflowerFoodBoostMax::mirror)
					.beginValue("foodBoostPerEat", INTEGER, AnnoyingFlowerBlockEntity.FOOD_BOOST_TIMES)
					.withComment("""
							每次进食增加的次数
							Boost added per food consumed""")
					.finishValue(annoyingflowerFoodBoostTimes::mirror)
					.beginValue("foodBoostCooldownMultiplier", DOUBLE, AnnoyingFlowerBlockEntity.FOOD_BOOST_COOLDOWN_MULTIPLIER)
					.withComment("""
							食物加成时的冷却时间乘数
							Cooldown multiplier when food boosted""")
					.finishValue(annoyingflowerFoodBoostCooldownMultiplier::mirror)
					.finishBranch() // End annoyingflower
					.fork("stonesia").withComment("""
							石中姬
							Stonesia""")
					.beginValue("maxMana", INTEGER, StonesiaBlockEntity.MAX_MANA)
					.withComment("""
							最大魔力值
							Maximum mana""")
					.finishValue(stonesiaMaxMana::mirror)
					.beginValue("cooldown", INTEGER, StonesiaBlockEntity.COOLDOWN)
					.withComment("""
							冷却时间(ticks)
							Cooldown time in ticks""")
					.finishValue(stonesiaCooldown::mirror)
					.finishBranch() // End stonesia

					.fork("edelweiss").withComment("""
							雪绒花
							Edelweiss""")
					.beginValue("maxMana", INTEGER, EdelweissBlockEntity.MAX_MANA)
					.withComment("""
							最大魔力值
							Maximum mana""")
					.finishValue(edelweissMaxMana::mirror)
					.beginValue("cooldown", INTEGER, EdelweissBlockEntity.COOLDOWN)
					.withComment("""
							冷却时间(ticks)
							Cooldown time in ticks""")
					.finishValue(edelweissCooldown::mirror)
					.finishBranch() // End edelweiss
					.fork("resoncund").withComment("""
							回音花
							Resoncund""")
					.beginValue("maxMana", INTEGER, ResoncundBlockEntity.MAX_MANA)
					.withComment("""
							最大魔力值
							Maximum mana""")
					.finishValue(resoncundMaxMana::mirror)
					.beginValue("lossPerHeard", INTEGER, ResoncundBlockEntity.MANA_LOSS_PER_HEARD)
					.withComment("""
							同种声音重复听到后的魔力生产衰减
							Mana produce decreases per same sound heard""")
					.finishValue(resoncundLossPerHeard::mirror)
					.finishBranch() // End resoncund
					.fork("sunshineLily").withComment("""
							日曜百合
							Sunshine Lily""")
					.beginValue("maxMana", INTEGER, SunshineLilyBlockEntity.MAX_MANA)
					.withComment("""
							最大魔力值
							Maximum mana""")
					.finishValue(sunshineLilyMaxMana::mirror)
					.beginValue("produceMana", INTEGER, SunshineLilyBlockEntity.MANA_PER_GENERATE)
					.withComment("""
							每次生成的魔力量
							Mana generated per produce""")
					.finishValue(sunshineLilyProduceMana::mirror)
					.finishBranch()//End sunshineLily

					.fork("moonlightLily").withComment("""
							月耀百合
							Moonlight Lily""")
					.beginValue("maxMana", INTEGER, MoonlightLilyBlockEntity.MAX_MANA)
					.withComment("""
							最大魔力值
							Maximum mana""")
					.finishValue(moonlightLilyMaxMana::mirror)
					.beginValue("produceMana", INTEGER, MoonlightLilyBlockEntity.MANA_PER_GENERATE)
					.withComment("""
							每次生成的魔力量
							Mana generated per produce""")
					.finishValue(moonlightLilyProduceMana::mirror)
					.finishBranch()//End moonlightLily

					.fork("twinstar").withComment("""
							双子兰
							Twinstar""")
					.beginValue("maxMana", INTEGER, TwinstarBlockEntity.MAX_MANA)
					.withComment("""
							最大魔力值
							Maximum mana""")
					.finishValue(twinstarMaxMana::mirror)
					.beginValue("maxTemperatureCap", INTEGER, TwinstarBlockEntity.TEMPERATURE_MAX)
					.withComment("""
							最大接受流体温度
							Maximum accepted temperature cap for liquid""")
					.finishValue(twinstarMaxTemperature::mirror)
					.beginValue("minTemperatureCap", INTEGER, TwinstarBlockEntity.TEMPERATURE_MIN)
					.withComment("""
							最低接受流体温度
							Minimum accepted temperature cap for liquid""")
					.finishValue(twinstarMinTemperature::mirror)
					.finishBranch()//End twinstar

					.fork("omniviolet").withComment("""
							全知瑾
							Omniviolet""")
					.beginValue("maxMana", INTEGER, OmnivioletBlockEntity.MAX_MANA)
					.withComment("""
							最大魔力值
							Maximum mana""")
					.finishValue(omnivioletMaxMana::mirror)
					.finishBranch()//End omniviolet

					.fork("tinkle").withComment("""
							叮当花
							Tinkle""")
					.beginValue("maxMana", INTEGER, TinkleBlockEntity.MAX_MANA)
					.withComment("""
							最大魔力值
							Maximum mana""")
					.finishValue(tinkleMaxMana::mirror)
					.beginValue("produceMana", INTEGER, TinkleBlockEntity.PRODUCE_MANA)
					.withComment("""
							每次生成的魔力量
							Mana generated per produce""")
					.finishValue(tinkleProduceMana::mirror)
					.finishBranch()//End tinkle

					.fork("bloodEnchantress").withComment("""
							鲜血妖姬
							Blood Enchantress""")
					.beginValue("maxMana", INTEGER, BloodEnchantressBlockEntity.MAX_MANA)
					.withComment("""
							最大魔力值
							Maximum mana""")
					.finishValue(bloodEnchantressMaxMana::mirror)
					.beginValue("produceMana", INTEGER, BloodEnchantressBlockEntity.PRODUCE_MANA)
					.withComment("""
							每次生成的魔力量
							Mana generated per produce""")
					.finishValue(bloodEnchantressProduceMana::mirror)
					.finishBranch()//End bloodEnchantress

					.fork("serenitian").withComment("""
							永寂龙胆
							Serenitian""")
					.beginValue("range", INTEGER, SerenitianBlockEntity.RANGE)
					.withComment("""
							作用范围(方块半径)
							Working range in blocks""")
					.finishValue(serenitianRange::mirror)
					.finishBranch()//End serenitian
					.fork("mirrowtunia").withComment("""
							镜姬
							Mirrowtunia""")
					.beginValue("maxMana", INTEGER, MirrowtuniaBlockEntity.MAX_MANA)
					.withComment("""
							最大魔力值
							Maximum mana""")
					.finishValue(mirrowtuniaMaxMana::mirror)
					.beginValue("effectCost", INTEGER, MirrowtuniaBlockEntity.EFFECT_COST)
					.withComment("""
							施加效果消耗的魔力量
							Mana cost per effect""")
					.finishValue(mirrowtuniaEffectCost::mirror)
					.finishBranch() // End mirrowtunia

					.fork("necrofleur").withComment("""
							死之花
							Necrofleur""")
					.beginValue("maxMana", INTEGER, NecrofleurBlockEntity.MAX_MANA)
					.withComment("""
							最大魔力值
							Maximum mana""")
					.finishValue(necrofleurMaxMana::mirror)
					.finishBranch() // End necrofleur

					.fork("manalink").withComment("""
							魔链星
							Manalink""")
					.beginValue("transferSpeed", INTEGER, ManalinkBlockEntity.TRANSFER_SPEED)
					.withComment("""
							魔力传输速度
							Mana transfer speed""")
					.finishValue(manalinkTransferSpeed::mirror)
					.finishBranch() // End manalink

					.fork("enchanter").withComment("""
							蕴魔瑾
							Enchanter""")
					.beginValue("transformCost", INTEGER, EnchanterBlockEntity.TRANSFORM_COST)
					.withComment("""
							转换草方块为蕴魔土的总消耗
							Total mana cost to transform grass to enchanted soil""")
					.finishValue(enchanterTransformCost::mirror)
					.beginValue("consumeSpeed", INTEGER, EnchanterBlockEntity.CONSUME_SPEED)
					.withComment("""
							魔力消耗速度
							Mana consume speed""")
					.finishValue(enchanterConsumeSpeed::mirror)
					.finishBranch() // End enchanter

					.finishBranch()//End flower

					.finishBranch();//End server

			return builder.build();
		}

		@Override
		public boolean disableGaiaDisArm() {
			return disableGaiaDisArm.getValue();
		}

		@Override
		public boolean enableTelemetry() {
			return enableTelemetry.getValue();
		}

		@Override
		public String telemetryUUID() {
			return telemetryUUID.getValue();
		}

		@Override
		public String fakePlayerId() {
			return fakePlayerId.getValue();
		}

		@Override
		public int[] woodieniaRange() {
			return woodieniaRange.getValue();
		}

		@Override
		public int woodieniaCooldown() {
			return woodieniaCooldown.getValue();
		}

		@Override
		public int woodieniaMaxMana() {
			return woodieniaMaxMana.getValue();
		}

		@Override
		public int woodieniaWorkManaCost() {
			return woodieniaWorkManaCost.getValue();
		}

		@Override
		public int reikarlilyMaxMana() {
			return reikarlilyMaxMana.getValue();
		}

		@Override
		public int reikarlilyProduceCooldown() {
			return reikarlilyProduceCooldown.getValue();
		}

		@Override
		public int reikarlilyProduceMana() {
			return reikarlilyProduceMana.getValue();
		}

		@Override
		public int reikarlilyPassiveGenerateTime() {
			return reikarlilyPassiveGenerateTime.getValue();
		}

		@Override
		public int reikarlilyPassiveGenerateMana() {
			return reikarlilyPassiveGenerateMana.getValue();
		}

		@Override
		public int reikarlilySpawnLightningCooldown() {
			return reikarlilySpawnLightningCooldown.getValue();
		}

		@Override
		public int tradeOrchidMaxMana() {
			return tradeOrchidMaxMana.getValue();
		}

		@Override
		public int tradeOrchidCooldown() {
			return tradeOrchidCooldown.getValue();
		}

		@Override
		public int tradeOrchidManaCost() {
			return tradeOrchidManaCost.getValue();
		}

		@Override
		public double tradeOrchidDiscountPercentage() {
			return tradeOrchidDiscountPercentage.getValue();
		}

		@Override
		public int bellflowerMaxMana() {
			return bellflowerMaxMana.getValue();
		}

		@Override
		public double bellflowerGenerateModify() {
			return bellflowerGenerateModify.getValue();
		}

		@Override
		public int annoyingflowerMaxMana() {
			return annoyingflowerMaxMana.getValue();
		}

		@Override
		public int annoyingflowerFishingCost() {
			return annoyingflowerFishingCost.getValue();
		}

		@Override
		public int annoyingflowerCooldown() {
			return annoyingflowerCooldown.getValue();
		}

		@Override
		public int annoyingflowerFoodBoostMax() {
			return annoyingflowerFoodBoostMax.getValue();
		}

		@Override
		public int annoyingflowerFoodBoostTimes() {
			return annoyingflowerFoodBoostTimes.getValue();
		}

		@Override
		public double annoyingflowerFoodBoostCooldownMultiplier() {
			return annoyingflowerFoodBoostCooldownMultiplier.getValue();
		}

		@Override
		public int stonesiaMaxMana() {
			return stonesiaMaxMana.getValue();
		}

		@Override
		public int stonesiaCooldown() {
			return stonesiaCooldown.getValue();
		}

		@Override
		public int edelweissMaxMana() {
			return edelweissMaxMana.getValue();
		}

		@Override
		public int edelweissCooldown() {
			return edelweissCooldown.getValue();
		}

		@Override
		public int resoncundMaxMana() {
			return resoncundMaxMana.getValue();
		}

		@Override
		public int resoncundLossPerHeard() {
			return resoncundLossPerHeard.getValue();
		}

		@Override
		public int sunshineLilyMaxMana() {
			return sunshineLilyMaxMana.getValue();
		}

		@Override
		public int sunshineLilyProduceMana() {
			return sunshineLilyProduceMana.getValue();
		}

		@Override
		public int moonlightLilyMaxMana() {
			return moonlightLilyMaxMana.getValue();
		}

		@Override
		public int moonlightLilyProduceMana() {
			return moonlightLilyProduceMana.getValue();
		}

		@Override
		public int twinstarMaxMana() {
			return twinstarMaxMana.getValue();
		}

		@Override
		public int twinstarMaxTemperature() {
			return twinstarMaxTemperature.getValue();
		}

		@Override
		public int twinstarMinTemperature() {
			return twinstarMinTemperature.getValue();
		}

		@Override
		public int omnivioletMaxMana() {
			return omnivioletMaxMana.getValue();
		}

		@Override
		public int tinkleMaxMana() {
			return tinkleMaxMana.getValue();
		}

		@Override
		public int tinkleProduceMana() {
			return tinkleProduceMana.getValue();
		}

		@Override
		public int bloodEnchantressMaxMana() {
			return bloodEnchantressMaxMana.getValue();
		}

		@Override
		public int bloodEnchantressProduceMana() {
			return bloodEnchantressProduceMana.getValue();
		}

		@Override
		public int serenitianRange() {
			return serenitianRange.getValue();
		}

		@Override
		public int mirrowtuniaMaxMana() {
			return mirrowtuniaMaxMana.getValue();
		}

		@Override
		public int mirrowtuniaEffectCost() {
			return mirrowtuniaEffectCost.getValue();
		}

		@Override
		public int necrofleurMaxMana() {
			return necrofleurMaxMana.getValue();
		}

		@Override
		public int manalinkTransferSpeed() {
			return manalinkTransferSpeed.getValue();
		}

		@Override
		public int enchanterTransformCost() {
			return enchanterTransformCost.getValue();
		}

		@Override
		public int enchanterConsumeSpeed() {
			return enchanterConsumeSpeed.getValue();
		}

		@Override
		public List<String> gaiaSpawnUnCheckList() {
			return gaiaSpawnUnCheckList.getValue();
		}
	}

	private static void writeDefaultConfig(ConfigTree config, Path path, JanksonValueSerializer serializer) {
		try (OutputStream s = new BufferedOutputStream(Files.newOutputStream(path, StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW))) {
			FiberSerialization.serialize(config, s, serializer);
		} catch (FileAlreadyExistsException ignored) {} catch (IOException e) {
			ExtraBotanyAPI.LOGGER.error("Error writing default config", e);
		}
	}

	private static void setupConfig(ConfigTree config, Path p, JanksonValueSerializer serializer) {
		writeDefaultConfig(config, p, serializer);

		try (InputStream s = new BufferedInputStream(Files.newInputStream(p))) {
			FiberSerialization.deserialize(config, s, serializer);
		} catch (IOException | ValueDeserializationException e) {
			ExtraBotanyAPI.LOGGER.error("Error loading config from {}", p, e);
		}

		try (OutputStream s = new BufferedOutputStream(Files.newOutputStream(p, StandardOpenOption.TRUNCATE_EXISTING))) {
			FiberSerialization.serialize(config, s, serializer);
		} catch (IOException e) {
			ExtraBotanyAPI.LOGGER.error("Error writing merged config to {}", p, e);
		}
	}
}
