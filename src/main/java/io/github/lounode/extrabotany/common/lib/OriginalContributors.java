package io.github.lounode.extrabotany.common.lib;

import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;

/**
 * 原版额外植物学的贡献者名单，来自原版仓库的 {@code contributors.md}。
 * The list of contributors of the original Extra Botany, from {@code contributors.md} in the original repository.
 *
 * <p>原版在启动时从网上下载这份名单，并在 Boss 战里使用这些名字：空之律者的"支援者"和本我仆从。这里把名单直接写进代码，所以不需要联网，名字也不会丢失。
 * <p>The original downloads this list from the internet at startup and uses the names in the boss fights: the "supporters" of the Herrscher of the Void and the Ego Minions. This port has the list in the code, thus no network access is necessary and the names are not lost.
 *
 * <p>感谢这些玩家对原版模组的支持。
 * <p>Thanks to these players for their support of the original mod.
 */
public final class OriginalContributors {
	public static final List<String> NAMES = List.of(
			"ExtraMeteorP", "MalayP", "kohlth", "Glorious_B", "xRoBoTx", "Cyberpunk909", "xiaomipe", "big_penguin_ee", "Nyan_shinaro",
			"_BRS", "Amunesia", "huiyiguo", "hwk", "zizisama", "knight", "Amygdala", "LinkXinLuo", "wtpoi", "Eternego", "Mtdlr_",
			"Calamity_", "wang_hun", "Dark_Dancer", "Suripe_Z", "Thememories", "54886", "dbydd", "SM_Chicov", "Azitate", "CKATEPTb",
			"TheresaMOD3", "minatoapua", "NanayaShiki_", "Achsr", "silentsliver", "FOURNINE", "fox_jtx_huli", "Seven_Flowers", "han_lie",
			"truecyt", "HoshizoraShizuku", "Miyamori_Aoi", "yi_meng", "miku_Yunyan", "wu_shen_wu_ke");

	private OriginalContributors() {}

	/**
	 * 随机选出几个不重复的名字。
	 * Selects some different names at random.
	 */
	public static List<String> pick(RandomSource random, int count) {
		List<String> pool = new ArrayList<>(NAMES);
		List<String> picked = new ArrayList<>(count);
		for (int i = 0; i < count && !pool.isEmpty(); i++) {
			picked.add(pool.remove(random.nextInt(pool.size())));
		}
		return picked;
	}
}
