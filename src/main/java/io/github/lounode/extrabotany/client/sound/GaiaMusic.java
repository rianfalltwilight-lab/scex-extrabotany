package io.github.lounode.extrabotany.client.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;

import io.github.lounode.extrabotany.common.entity.gaia.Gaia;

/**
 * 盖亚守护者战斗时循环播放的音乐。声音的位置是守护者的家，守护者死亡或者消失之后音乐停止。
 * The music that loops during the fight with a Guardian of Gaia. The position of the sound is the home of the guardian, and the music stops after the guardian dies or goes away.
 *
 * <p>每种守护者用 {@link Gaia#getBGM()} 给出自己的音乐。
 * <p>Each type of guardian gives its own music with {@link Gaia#getBGM()}.
 */
public final class GaiaMusic extends AbstractTickableSoundInstance {
	private final Gaia guardian;

	private GaiaMusic(Gaia guardian) {
		super(guardian.getBGM(), SoundSource.RECORDS, SoundInstance.createUnseededRandom());
		this.guardian = guardian;
		BlockPos home = guardian.getHome().pos();
		this.x = home.getX();
		this.y = home.getY();
		this.z = home.getZ();
		this.looping = true;
	}

	public static void play(Gaia guardian) {
		Minecraft.getInstance().getSoundManager().play(new GaiaMusic(guardian));
	}

	@Override
	public void tick() {
		if (!guardian.isAlive()) {
			stop();
		}
	}
}
