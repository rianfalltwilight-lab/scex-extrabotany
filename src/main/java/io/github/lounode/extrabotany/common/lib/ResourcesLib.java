package io.github.lounode.extrabotany.common.lib;

/**
 * 贴图路径的常量。盔甲物品在公共代码里给出自己的贴图路径，所以这个类不在客户端的包里。
 * The constants of texture paths. The armor items give their texture path in common code, thus this class is not in the client package.
 */
public class ResourcesLib {

	public static final String PREFIX_MOD = LibMisc.MOD_ID + ":";
	public static final String ENDING_JSON = ".json";

	public static final String PREFIX_SHADER = "shader/";
	public static final String PREFIX_MODELS = "models/";

	public static final String PREFIX_GUI = PREFIX_MOD + "textures/gui/";
	public static final String PREFIX_MODEL = PREFIX_MOD + "textures/models/";
	public static final String PREFIX_MISC = PREFIX_MOD + "textures/misc/";
	public static final String PREFIX_ARMOR = PREFIX_MODEL + "armor/";

	public static final String MODEL_INVISIBLE_ARMOR = "botania:textures/model/armor_invisible.png";

	public static final String MODEL_STARRY_IDOL = PREFIX_ARMOR + "starry_idol.png";
	public static final String MODEL_PLEIADES_COMBAT_MAID = PREFIX_ARMOR + "pleiades_combat_maid.png";
	public static final String MODEL_SHADOW_WARRIOR = PREFIX_ARMOR + "shadow_warrior.png";
	public static final String MODEL_GOBLIN_SLAYER = PREFIX_ARMOR + "goblin_slayer.png";
}
