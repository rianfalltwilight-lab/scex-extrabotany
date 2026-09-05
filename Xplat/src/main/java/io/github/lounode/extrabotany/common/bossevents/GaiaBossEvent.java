package io.github.lounode.extrabotany.common.bossevents;

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
