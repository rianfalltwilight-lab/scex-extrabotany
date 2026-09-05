package io.github.lounode.extrabotany.common.item;

import java.time.LocalDate;

/** Calendar contract retained for the deployed candy items (December 16–January 2). */
public final class SeasonalItemHelper {
	private SeasonalItemHelper() {}

	public static boolean isChristmas() {
		return isChristmas(LocalDate.now());
	}

	public static boolean isChristmas(LocalDate date) {
		return (date.getMonthValue() == 12 && date.getDayOfMonth() >= 16)
				|| (date.getMonthValue() == 1 && date.getDayOfMonth() <= 2);
	}
}
