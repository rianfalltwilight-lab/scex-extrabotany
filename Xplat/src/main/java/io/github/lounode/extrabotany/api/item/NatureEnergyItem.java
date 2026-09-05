package io.github.lounode.extrabotany.api.item;

/**
 * An item that has this capability can contain nature energy.
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
