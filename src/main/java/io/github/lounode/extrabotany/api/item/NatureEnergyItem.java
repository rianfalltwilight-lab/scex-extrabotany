package io.github.lounode.extrabotany.api.item;

/**
 * 能保存自然能量的物品。自然能量在祭坛上积累。
 * An item that can hold nature energy. Nature energy is collected on a pedestal.
 */
public interface NatureEnergyItem {

	/**
	 * Gets the amount of nature energy this item contains
	 */
	long getEnergy();

	/**
	 * Gets the max amount of nature energy this item can hold.
	 */
	long getMaxEnergy();

	/**
	 * Adds nature energy to this item.
	 */
	boolean addEnergy(long energy);

}
