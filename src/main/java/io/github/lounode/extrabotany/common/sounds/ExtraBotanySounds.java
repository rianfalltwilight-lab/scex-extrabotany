package io.github.lounode.extrabotany.common.sounds;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import vazkii.botania.common.helper.RegistryHelper;

import java.util.ArrayList;
import java.util.List;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class ExtraBotanySounds {
	private static final List<RegistryHelper.HolderProxy<SoundEvent>> EVENTS = new ArrayList<>();
	public static final SoundEvent MOTOR_CYCLONE = makeSoundEvent("entity.motor.cyclone");
	public static final SoundEvent MOTOR_RIDE_ON = makeSoundEvent("entity.motor.ride_on");
	public static final SoundEvent KEY_OF_TRUTH_SHOOT = makeSoundEvent("entity.key_of_truth.shoot");
	public static final SoundEvent THUNDER_SLASH = makeSoundEvent("entity.thunder_slash.slash");
	public static final SoundEvent FLAMESCION_ULT = makeSoundEvent("item.flamescion_weapon.ult");
	public static final SoundEvent SPEAR_OF_SUBSPACE_USE = makeSoundEvent("item.spear_of_subspace.use");
	public static final SoundEvent MUSIC_EGO = makeSoundEvent("music.ego");
	public static final SoundEvent CAMERA_USE = makeSoundEvent("item.camera.use");
	public static final SoundEvent CAMERA_CHARGE = makeSoundEvent("item.camera.charge");
	public static final SoundEvent CAMERA_FOCUS = makeSoundEvent("item.camera.focus");
	public static final SoundEvent EXCALIBUR_ATTACK = makeSoundEvent("item.excalibur.attack");
	public static final SoundEvent FAILNAUGHT_SHOOT = makeSoundEvent("item.failnaught.shoot");
	public static final SoundEvent FEATHER_OF_JINGWEI_SHOOT = makeSoundEvent("item.feather_of_jingwei.shoot");
	public static final SoundEvent PLAYER_BACKFIRE = makeSoundEvent("entity.player.hurt_backfire");
	public static final SoundEvent REWARD_BAG_OPEN = makeSoundEvent("item.reward_bag.open");
	public static final SoundEvent PANDORAS_BOX_OPEN = makeSoundEvent("item.pandoras_box.open");
	public static final SoundEvent WALKING_CANE_USE = makeSoundEvent("item.walking_cane.use");

	public static final SoundEvent MUSIC_GAIA3 = makeSoundEvent("music.gaia3");
	public static final SoundEvent MUSIC_HERRSCHER = makeSoundEvent("music.herrscher");
	public static final Holder<SoundEvent> ARMOR_EQUIP_IDOL = makeSoundEventHolder("item.armor.equip_idol");
	public static final SoundEvent HAMMER_USE = makeSoundEvent("item.hammer.use");
	// 对照原版审计 E7：1.12 ModSounds.annoyingflower，王之花园召唤烦人花时播放。重燃的风铃草铃声没有地方使用，已删除。
	// Parity audit E7: ModSounds.annoyingflower of 1.12, played when the King's Garden summons the annoying flower. The bellflower ring of Reburn was used nowhere and is removed.
	public static final SoundEvent FLOWER_WEAPON_ANNOYING = makeSoundEvent("entity.flower_weapon.annoying_flower");

	private static SoundEvent makeSoundEvent(String name) {
		return makeSoundEventHolder(name).value();
	}

	private static Holder<SoundEvent> makeSoundEventHolder(String name) {
		ResourceLocation id = prefix(name);
		RegistryHelper.HolderProxy<SoundEvent> proxy = RegistryHelper.holderProxy(
				Registries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
		EVENTS.add(proxy);
		return proxy;
	}

	public static void init(Registry<SoundEvent> registry) {
		EVENTS.forEach(proxy -> proxy.register(registry));
	}
}
