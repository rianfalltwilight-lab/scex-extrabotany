package io.github.lounode.extrabotany.forge.client;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

import org.lwjgl.glfw.GLFW;

import io.github.lounode.extrabotany.common.lib.LibMisc;

/**
 * 本模组的按键绑定。
 * The key mappings of this mod.
 */
@EventBusSubscriber(modid = LibMisc.MOD_ID, value = Dist.CLIENT)
public final class ExtraBotanyKeyMappings {
	/**
	 * 技能键，默认是 R。它召唤饰品坐骑、控制飞碟的抓取，并且释放薪炎的终结技。
	 * The skill key, R by default. It summons the accessory mount, controls the catch of the Cosmic Car, and starts the ultimate of the Flamescion.
	 *
	 * <p>按键的 ID 保留旧名称，这样玩家已经保存的按键设置不会丢失。
	 * <p>The ID of the key keeps the old name, so that the key settings that players saved are not lost.
	 */
	public static final KeyMapping SKILL = new KeyMapping("key.extrabotany.legacy_skill", KeyConflictContext.IN_GAME,
			InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, "key.categories.extrabotany");

	private ExtraBotanyKeyMappings() {}

	@SubscribeEvent
	public static void register(RegisterKeyMappingsEvent event) {
		event.register(SKILL);
	}
}
