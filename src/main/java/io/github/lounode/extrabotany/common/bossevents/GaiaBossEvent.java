package io.github.lounode.extrabotany.common.bossevents;

/**
 * 盖亚血条在普通血条之外的数据：参战的玩家数，以及 Boss 的无敌时间。客户端用无敌时间算出血条的噪点强度。
 * The data that a Gaia boss bar has in addition to a usual boss bar: the number of players in the fight, and the invulnerability time of the boss. The client calculates the grain intensity of the bar from the invulnerability time.
 */
public interface GaiaBossEvent {

	int getPlayerCount();

	void setPlayerCount(int playerCount);

	default boolean displayPlayerCount() {
		return getPlayerCount() > 0;
	}

	int getGrainTime();

	void setGrainTime(int time);

	default float getGrainIntensity() {
		return getGrainTime() > 20 ? 1F : Math.max(0.5F, getGrainTime() / 20F);
	}
}
