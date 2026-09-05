package io.github.lounode.extrabotany.xplat;

import vazkii.botania.api.BotaniaAPI;

import java.util.List;

public class ExtraBotanyConfig {
	private static ConfigAccess config = null;
	private static ClientConfigAccess clientConfig = null;

	public static ConfigAccess common() {
		return config;
	}

	public static ClientConfigAccess client() {
		return clientConfig;
	}

	public static void setCommon(ConfigAccess access) {
		if (config != null) {
			BotaniaAPI.LOGGER.warn("ConfigAccess was replaced! Old {} New {}", config.getClass().getName(), access.getClass().getName());
		}

		config = access;
	}

	public static void setClient(ClientConfigAccess access) {
		if (clientConfig != null) {
			BotaniaAPI.LOGGER.warn("ClientConfigAccess was replaced! Old {} New {}", clientConfig.getClass().getName(), access.getClass().getName());
		}

		clientConfig = access;
	}

	public static void resetPatchouliFlags() {

	}

	public interface ClientConfigAccess {
		boolean otakuMode();
	}

	public interface ConfigAccess {
		default int legacyMachineSetting(String key, int fallback) { return fallback; }
		default boolean legacyMachineEnabled(String key) { return true; }
		boolean disableGaiaDisArm();
		default boolean guardianItemCheck() { return true; }
		List<String> gaiaSpawnUnCheckList();
		boolean enableTelemetry();
		String telemetryUUID();
		String fakePlayerId();

		//Flowers
		//Generating

		//Reikarlily
		int reikarlilyMaxMana();
		int reikarlilyProduceCooldown();
		int reikarlilyProduceMana();
		int reikarlilySpawnLightningCooldown();
		int reikarlilyPassiveGenerateTime();
		int reikarlilyPassiveGenerateMana();

		//Bellflower
		int bellflowerMaxMana();
		double bellflowerGenerateModify();

		//Stonesia
		int stonesiaMaxMana();
		int stonesiaCooldown();

		//Edelweiss
		int edelweissMaxMana();
		int edelweissCooldown();

		//Resoncund
		int resoncundMaxMana();
		int resoncundLossPerHeard();

		//SunshineLily
		int sunshineLilyMaxMana();
		int sunshineLilyProduceMana();

		//MoonlightLily
		int moonlightLilyMaxMana();
		int moonlightLilyProduceMana();

		//Twinstar
		int twinstarMaxMana();
		int twinstarMaxTemperature();
		int twinstarMinTemperature();

		//Omniviolet
		int omnivioletMaxMana();

		//Tinkle
		int tinkleMaxMana();
		int tinkleProduceMana();

		//BloodEnchantress
		int bloodEnchantressMaxMana();
		int bloodEnchantressProduceMana();

		//Functional
		//TradeOrchid
		int tradeOrchidMaxMana();
		int tradeOrchidManaCost();
		int tradeOrchidCooldown();
		double tradeOrchidDiscountPercentage();

		//Woodienia
		int[] woodieniaRange();
		int woodieniaCooldown();
		int woodieniaMaxMana();
		int woodieniaWorkManaCost();

		//Annoyingflower
		int annoyingflowerMaxMana();
		int annoyingflowerFishingCost();
		int annoyingflowerCooldown();
		int annoyingflowerFoodBoostMax();
		int annoyingflowerFoodBoostTimes();
		double annoyingflowerFoodBoostCooldownMultiplier();

		//Serenitian
		int serenitianRange();

		//Mirrowtunia
		int mirrowtuniaMaxMana();
		int mirrowtuniaEffectCost();

		//Necrofleur
		int necrofleurMaxMana();

		//Manalink
		int manalinkTransferSpeed();

		//Enchanter
		int enchanterTransformCost();
		int enchanterConsumeSpeed();

	}
}
