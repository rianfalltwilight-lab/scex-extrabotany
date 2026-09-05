package io.github.lounode.extrabotany.api.gaia;

import java.util.UUID;

public interface ColorfulBossBar {
	default int getPlayerCount() {
		return 0;
	}

	UUID getBossInfoUuid();

	default boolean displayPlayerCount() {
		return getPlayerCount() > 0;
	}

	default float getGrainIntensity() {
		return 0;
	}
}
