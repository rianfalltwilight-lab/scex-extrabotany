package io.github.lounode.extrabotany.common.impl;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorMaterial;

import io.github.lounode.extrabotany.api.ExtraBotanyAPI;
import io.github.lounode.extrabotany.api.item.CoreOfTheVoidVariant;
import io.github.lounode.extrabotany.api.item.VoidArchivesVariant;
import io.github.lounode.extrabotany.common.item.material.ArmorsMaterial;

import java.util.LinkedHashMap;
import java.util.Map;

public class ExtraBotanyAPIImpl implements ExtraBotanyAPI {
	private final Map<String, CoreOfTheVoidVariant> covVariants = new LinkedHashMap<>();
	private final Map<String, VoidArchivesVariant> voidArchivesVariants = new LinkedHashMap<>();

	@Override
	public Holder<ArmorMaterial> getPleiadsMaidCombatArmorMaterial() {
		return ArmorsMaterial.PLEIADS_MAID_COMBAT;
	}

	@Override
	public Holder<ArmorMaterial> getStarryIdolArmorMaterial() {
		return ArmorsMaterial.STARRY_IDOL;
	}

	@Override
	public Holder<ArmorMaterial> getGoblinSlayerArmorMaterial() {
		return ArmorsMaterial.GOBLIN_SLAYER;
	}

	@Override
	public Holder<ArmorMaterial> getShadowWarriorArmorMaterial() {
		return ArmorsMaterial.SHADOW_WARRIOR;
	}

	@Override
	public void registerCOVVariant(CoreOfTheVoidVariant variant) {
		this.covVariants.put(variant.getId(), variant);
	}

	@Override
	public void registerVoidArchivesVariant(VoidArchivesVariant variant) {
		this.voidArchivesVariants.put(variant.getId(), variant);
	}

	@Override
	public Map<String, CoreOfTheVoidVariant> getCOVVariants() {
		return this.covVariants;
	}

	@Override
	public Map<String, VoidArchivesVariant> getVoidArchivesVariants() {
		return this.voidArchivesVariants;
	}
}
