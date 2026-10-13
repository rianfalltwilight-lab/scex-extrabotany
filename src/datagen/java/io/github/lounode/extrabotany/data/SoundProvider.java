package io.github.lounode.extrabotany.data;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.SoundDefinition;
import net.neoforged.neoforge.common.data.SoundDefinitionsProvider;

import vazkii.botania.common.handler.BotaniaSounds;

import io.github.lounode.extrabotany.common.lib.LibMisc;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static io.github.lounode.extrabotany.common.sounds.ExtraBotanySounds.*;

/**
 * 生成 {@code sounds.json}：每个声音事件播放哪个声音。
 * Makes {@code sounds.json}: the sound that each sound event plays.
 *
 * <p>默认规则：声音事件 {@code a.b.c} 播放文件 {@code sounds/a/b/c.ogg}，字幕的翻译键是 {@code subtitles.extrabotany:a.b.c}。借用别的声音的事件在 {@link #registerSounds()} 里单独列出。
 * <p>The default rule: the sound event {@code a.b.c} plays the file {@code sounds/a/b/c.ogg}, and the translation key of the subtitle is {@code subtitles.extrabotany:a.b.c}. The events that use the sound of something else are listed one by one in {@link #registerSounds()}.
 *
 * <p>基类会检查每个文件和每个被借用的声音事件是否存在，缺少时生成失败。
 * <p>The base class examines if each file and each borrowed sound event exists, and the generation fails when one is missing.
 */
public class SoundProvider extends SoundDefinitionsProvider {

	public SoundProvider(PackOutput output, ExistingFileHelper helper) {
		super(output, LibMisc.MOD_ID, helper);
	}

	@Override
	public void registerSounds() {
		Set<SoundEvent> remaining = new LinkedHashSet<>();
		for (SoundEvent event : BuiltInRegistries.SOUND_EVENT) {
			if (LibMisc.MOD_ID.equals(event.getLocation().getNamespace())) {
				remaining.add(event);
			}
		}

		// 三首 Boss 音乐：流式播放，没有字幕。
		// The three boss music tracks: they are streamed and have no subtitle.
		for (SoundEvent music : List.of(MUSIC_GAIA3, MUSIC_EGO, MUSIC_HERRSCHER)) {
			remaining.remove(music);
			add(music, definition().with(sound(filePath(music)).stream().volume(1.3F)));
		}

		// 借用另一个声音事件的事件。
		// The events that use another sound event.
		borrow(remaining, PANDORAS_BOX_OPEN, SoundEvents.ARMOR_EQUIP_CHAIN.value(), 1F);
		borrow(remaining, REWARD_BAG_OPEN, SoundEvents.ARMOR_EQUIP_LEATHER.value(), 1F);
		borrow(remaining, FEATHER_OF_JINGWEI_SHOOT, SoundEvents.FIRE_EXTINGUISH, 0.2F);
		borrow(remaining, PLAYER_BACKFIRE, SoundEvents.PLAYER_HURT_FREEZE, 1F);
		borrow(remaining, WALKING_CANE_USE, SoundEvents.FISHING_BOBBER_RETRIEVE, 1F);
		borrow(remaining, HAMMER_USE, BotaniaSounds.TERRA_SHATTERER_MODE, 1F);
		borrow(remaining, ARMOR_EQUIP_MAID.value(), BotaniaSounds.EQUIP_MANAWEAVE.value(), 1F);
		borrow(remaining, ARMOR_EQUIP_IDOL.value(), BotaniaSounds.EQUIP_MANAWEAVE.value(), 1F);
		borrow(remaining, ARMOR_EQUIP_GOBLIN.value(), SoundEvents.ARMOR_EQUIP_IRON.value(), 1F);
		borrow(remaining, ARMOR_EQUIP_WARRIOR.value(), SoundEvents.ARMOR_EQUIP_CHAIN.value(), 1F);

		// 王者圣剑和百中弓使用泰拉之刃的声音文件，音量更小、音调更高。
		// Excalibur and the Failnaught use the sound file of the Terra Blade, at a lower volume and a higher pitch.
		for (SoundEvent event : List.of(EXCALIBUR_ATTACK, FAILNAUGHT_SHOOT)) {
			remaining.remove(event);
			add(event, definition().subtitle(subtitle(event))
					.with(sound(BotaniaSounds.TERRABLADE.getLocation()).volume(0.4F).pitch(1.4F)));
		}

		for (SoundEvent event : remaining) {
			add(event, definition().subtitle(subtitle(event)).with(sound(filePath(event))));
		}
	}

	private void borrow(Set<SoundEvent> remaining, SoundEvent event, SoundEvent source, float volume) {
		remaining.remove(event);
		SoundDefinition.Sound sound = sound(source.getLocation(), SoundDefinition.SoundType.EVENT);
		if (volume != 1F) {
			sound.volume(volume);
		}
		add(event, definition().subtitle(subtitle(event)).with(sound));
	}

	private static String subtitle(SoundEvent event) {
		return "subtitles." + event.getLocation();
	}

	/**
	 * 声音事件的默认文件：把名字里的点换成斜杠。
	 * The default file of a sound event: the dots in the name become slashes.
	 */
	private static ResourceLocation filePath(SoundEvent event) {
		ResourceLocation id = event.getLocation();
		return ResourceLocation.fromNamespaceAndPath(id.getNamespace(), id.getPath().replace('.', '/'));
	}
}
