package io.github.lounode.extrabotany.client.renderer.wing;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import vazkii.botania.client.render.AccessoryRenderer;

import io.github.lounode.extrabotany.common.item.relic.voidcore.CoreOfTheVoidItem;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 空之律者核心的饰品渲染器。它按照物品上的外观，在佩戴者的背上画出对应的翅膀。
 * The accessory renderer of the Core of the Void. It draws the wings for the appearance of the item on the back of the wearer.
 *
 * <p>外观的名字在 {@link CoreOfTheVoidItem} 里，两端都能用。翅膀的画法只在客户端，登记在这张表里。
 * <p>The names of the appearances are in {@link CoreOfTheVoidItem}, and the two sides can use them. The method to draw the wings is on the client only, and this table holds it.
 */
public final class CoreOfTheVoidRenderer implements AccessoryRenderer {
	private static final Map<String, WingRenderer> WINGS = new ConcurrentHashMap<>();

	static {
		registerWings(CoreOfTheVoidItem.VARIANT_HERRSCHER, new HerrscherWings());
		registerWings(CoreOfTheVoidItem.VARIANT_FLANDRE, new PairedWings(prefix("wing/flandre"), 0.2, 0.25F));
		registerWings(CoreOfTheVoidItem.VARIANT_JIM, new PairedWings(prefix("wing/jim"), 0.5, 1F));
		registerWings(CoreOfTheVoidItem.VARIANT_STEAMPUNK, new PairedWings(prefix("wing/steampunk"), 0.5, 1F));
	}

	/**
	 * 给一种外观登记翅膀。附属模组必须在客户端的模组构造阶段调用它，这样翅膀的模型才会和别的模型一起加载。
	 * Records the wings for one appearance. An add-on must call this in the mod construction phase on the client, so that the models of the wings load together with the other models.
	 */
	public static void registerWings(String variantId, WingRenderer wings) {
		WINGS.put(variantId, wings);
	}

	/**
	 * 把所有翅膀的模型交给模型注册事件。
	 * Gives the models of all wings to the model registration event.
	 */
	public static void collectModels(Consumer<ResourceLocation> output) {
		for (WingRenderer wings : WINGS.values()) {
			wings.models().forEach(output);
		}
	}

	@Override
	public void doRender(HumanoidModel<?> bipedModel, ItemStack stack, LivingEntity living, PoseStack ms,
			MultiBufferSource buffers, int light, float limbSwing, float limbSwingAmount, float partialTicks,
			float ageInTicks, float netHeadYaw, float headPitch) {
		WingRenderer wings = WINGS.get(CoreOfTheVoidItem.getVariant(stack));
		if (wings != null) {
			wings.render(bipedModel, stack, living, ms, buffers, light, partialTicks);
		}
	}
}
