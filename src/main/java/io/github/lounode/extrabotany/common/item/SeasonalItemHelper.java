package io.github.lounode.extrabotany.common.item;

import java.time.LocalDate;

/**
 * 判断今天是否在圣诞节期间（12 月 16 日到 1 月 2 日）。糖果类物品用它来换名字和贴图。
 * Tells if today is in the Christmas period (December 16 to January 2). The candy items use it to change the name and the texture.
 */
public final class SeasonalItemHelper {
	private SeasonalItemHelper() {}

	private static final long CHECK_INTERVAL_MILLIS = 60_000;
	private static long nextCheck;
	private static boolean christmas;

	/**
	 * 今天是否在圣诞节期间。物品渲染每一帧都会调用它，所以结果缓存一分钟。
	 * Tells if today is in the Christmas period. The item rendering calls this on each frame, thus the result is kept for one minute.
	 */
	public static boolean isChristmas() {
		long now = System.currentTimeMillis();
		if (now >= nextCheck) {
			christmas = isChristmas(LocalDate.now());
			nextCheck = now + CHECK_INTERVAL_MILLIS;
		}
		return christmas;
	}

	public static boolean isChristmas(LocalDate date) {
		return (date.getMonthValue() == 12 && date.getDayOfMonth() >= 16)
				|| (date.getMonthValue() == 1 && date.getDayOfMonth() <= 2);
	}
}
