package io.github.lounode.extrabotany.data;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

import vazkii.botania.common.handler.BotaniaSounds;

import io.github.lounode.extrabotany.common.lib.LibMisc;
import io.github.lounode.extrabotany.data.sound.SoundDefinition;
import io.github.lounode.extrabotany.data.sound.SoundDefinitionsProvider;

import java.util.Set;
import java.util.stream.Collectors;

import static io.github.lounode.extrabotany.common.sounds.ExtraBotanySounds.*;

public class SoundProvider extends SoundDefinitionsProvider {

	public SoundProvider(PackOutput output, String modId) {
		super(output, modId);
	}

	@Override
	public void registerSounds() {
		Set<SoundEvent> soundEvents = BuiltInRegistries.SOUND_EVENT.stream().filter(i -> LibMisc.MOD_ID.equals(BuiltInRegistries.SOUND_EVENT.getKey(i).getNamespace()))
				.collect(Collectors.toSet());

		take(soundEvents, EXCALIBUR_ATTACK);
		take(soundEvents, FAILNAUGHT_SHOOT);
		take(soundEvents, FEATHER_OF_JINGWEI_SHOOT);
		take(soundEvents, PLAYER_BACKFIRE);
		take(soundEvents, MUSIC_GAIA3);
		// scex.1 registered this dormant event, but shipped no ego.ogg and disabled ego BGM.
		take(soundEvents, MUSIC_EGO);
		this.add(MUSIC_EGO, SoundDefinitionsProvider.definition().subtitle(title(MUSIC_EGO)));
		take(soundEvents, MUSIC_HERRSCHER);
		this.add(MUSIC_HERRSCHER, SoundDefinitionsProvider.definition().with(
				SoundDefinitionsProvider.sound(ResourceLocation.parse("extrabotany:music/salvation")).stream().volume(1.3F)));
		take(soundEvents, REWARD_BAG_OPEN);
		take(soundEvents, PANDORAS_BOX_OPEN);

		this.add(PANDORAS_BOX_OPEN, SoundDefinitionsProvider.definition()
				.subtitle(title(PANDORAS_BOX_OPEN))
				.with(
						SoundDefinitionsProvider.sound(SoundEvents.ARMOR_EQUIP_CHAIN.value().getLocation(), SoundDefinition.SoundType.EVENT)
				)
		);

		this.add(REWARD_BAG_OPEN, SoundDefinitionsProvider.definition()
				.subtitle(title(REWARD_BAG_OPEN))
				.with(
						SoundDefinitionsProvider.sound(SoundEvents.ARMOR_EQUIP_LEATHER.value().getLocation(), SoundDefinition.SoundType.EVENT)
				)
		);

		this.add(EXCALIBUR_ATTACK, SoundDefinitionsProvider.definition()
				.subtitle(title(EXCALIBUR_ATTACK))
				.with(
						SoundDefinitionsProvider.sound(BotaniaSounds.TERRABLADE.getLocation())
								.volume(0.4f)
								.pitch(1.4f)
				)
		);

		this.add(FAILNAUGHT_SHOOT, SoundDefinitionsProvider.definition()
				.subtitle(title(FAILNAUGHT_SHOOT))
				.with(
						SoundDefinitionsProvider.sound(BotaniaSounds.TERRABLADE.getLocation())
								.volume(0.4f)
								.pitch(1.4f)
				)
		);

		this.add(FEATHER_OF_JINGWEI_SHOOT, SoundDefinitionsProvider.definition()
				.subtitle(title(FEATHER_OF_JINGWEI_SHOOT))
				.with(
						SoundDefinitionsProvider.sound(SoundEvents.FIRE_EXTINGUISH.getLocation(), SoundDefinition.SoundType.EVENT)
								.volume(0.2f)
				)
		);

		this.add(PLAYER_BACKFIRE, SoundDefinitionsProvider.definition()
				.subtitle(title(PLAYER_BACKFIRE))
				.with(
						SoundDefinitionsProvider.sound(SoundEvents.PLAYER_HURT_FREEZE.getLocation(), SoundDefinition.SoundType.EVENT)
				)
		);

		this.add(MUSIC_GAIA3, SoundDefinitionsProvider.definition()
				.with(
						SoundDefinitionsProvider.sound(relocateOggPath(MUSIC_GAIA3.getLocation()))
								.stream()
								.volume(1.3F)
				)
		);

		take(soundEvents, WALKING_CANE_USE);
		this.add(WALKING_CANE_USE, SoundDefinitionsProvider.definition()
				.subtitle(title(WALKING_CANE_USE))
				.with(
						SoundDefinitionsProvider.sound(SoundEvents.FISHING_BOBBER_RETRIEVE.getLocation(), SoundDefinition.SoundType.EVENT)
				)
		);

		take(soundEvents, ARMOR_EQUIP_MAID.value());
		this.add(ARMOR_EQUIP_MAID.value(), SoundDefinition.definition()
				.subtitle(title(ARMOR_EQUIP_MAID.value()))
				.with(
						SoundDefinitionsProvider.sound(BotaniaSounds.EQUIP_MANAWEAVE.value().getLocation(), SoundDefinition.SoundType.EVENT)
				)
		);

		take(soundEvents, ARMOR_EQUIP_IDOL.value());
		this.add(ARMOR_EQUIP_IDOL.value(), SoundDefinition.definition()
				.subtitle(title(ARMOR_EQUIP_IDOL.value()))
				.with(
						SoundDefinitionsProvider.sound(BotaniaSounds.EQUIP_MANAWEAVE.value().getLocation(), SoundDefinition.SoundType.EVENT)
				)
		);

		take(soundEvents, HAMMER_USE);
		this.add(HAMMER_USE, SoundDefinition.definition()
				.subtitle(title(HAMMER_USE))
				.with(
						SoundDefinitionsProvider.sound(BotaniaSounds.TERRA_SHATTERER_MODE.getLocation(), SoundDefinition.SoundType.EVENT)
				)
		);
		take(soundEvents, ARMOR_EQUIP_GOBLIN.value());
		this.add(ARMOR_EQUIP_GOBLIN.value(), SoundDefinition.definition()
				.subtitle(title(ARMOR_EQUIP_GOBLIN.value()))
				.with(
						SoundDefinitionsProvider.sound(SoundEvents.ARMOR_EQUIP_IRON.value().getLocation(), SoundDefinition.SoundType.EVENT)
				)
		);
		take(soundEvents, ARMOR_EQUIP_WARRIOR.value());
		this.add(ARMOR_EQUIP_WARRIOR.value(), SoundDefinition.definition()
				.subtitle(title(ARMOR_EQUIP_WARRIOR.value()))
				.with(
						SoundDefinitionsProvider.sound(SoundEvents.ARMOR_EQUIP_CHAIN.value().getLocation(), SoundDefinition.SoundType.EVENT)
				)
		);

		for (SoundEvent soundEvent : soundEvents) {
			this.add(soundEvent, defaultDefinition(soundEvent));
		}
	}

	public String title(SoundEvent soundEvent) {
		return "subtitles." + soundEvent.getLocation();
	}

	protected void take(Set<SoundEvent> form, SoundEvent soundEvent) {
		//this.add(soundEvent, definition);
		form.remove(soundEvent);
	}

	protected SoundDefinition copyDefinition(SoundEvent soundEvent, SoundEvent... copyFrom) {
		SoundDefinition definition = SoundDefinitionsProvider.definition()
				.subtitle("subtitles." + soundEvent.getLocation());

		for (SoundEvent sound : copyFrom) {
			definition.with(SoundDefinitionsProvider.sound(sound.getLocation()));
		}

		return definition;

	}

	protected SoundDefinition defaultDefinition(SoundEvent soundEvent) {
		return SoundDefinitionsProvider.definition()
				.subtitle("subtitles." + soundEvent.getLocation())
				.with(
						SoundDefinitionsProvider.sound(relocateOggPath(soundEvent.getLocation()))
				);
	}

	protected ResourceLocation relocateOggPath(ResourceLocation location) {
		String namespace = location.getNamespace();
		String originPath = location.getPath();

		String path = originPath.replace('.', '/');

		return ResourceLocation.tryBuild(namespace, path);
	}
}
