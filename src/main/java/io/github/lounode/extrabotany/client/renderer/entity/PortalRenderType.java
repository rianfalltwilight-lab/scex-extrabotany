package io.github.lounode.extrabotany.client.renderer.entity;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.Util;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import vazkii.botania.client.core.helper.CoreShaders;

import java.util.function.Function;

/**
 * 裂隙和传送门使用的渲染类型：半透明、不剔除背面，并且使用植物魔法的光晕着色器。
 * The render type for rifts and portals: translucent, with no back-face culling, and with the halo shader of Botania.
 *
 * <p>来源：ExtraMeteorP 1.12 的 {@code RenderSubspace} 和 {@code RenderFlowerWeapon} 都使用 {@code ShaderHelper.halo}。
 * <p>Source: {@code RenderSubspace} and {@code RenderFlowerWeapon} of ExtraMeteorP 1.12 both use {@code ShaderHelper.halo}.
 */
final class PortalRenderType extends RenderType {
	private static final String NAME = "extrabotany_portal";
	private static final int BUFFER_SIZE = 256;

	private static final Function<ResourceLocation, RenderType> TYPES = Util.memoize(texture -> create(
			NAME, DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS, BUFFER_SIZE, false, true,
			CompositeState.builder()
					.setShaderState(new ShaderStateShard(CoreShaders::halo))
					.setTextureState(new TextureStateShard(texture, false, false))
					.setCullState(NO_CULL)
					.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
					.createCompositeState(false)));

	// 这个构造方法只是为了能访问 RenderType 里受保护的成员。它不会被调用。
	// This constructor is only here to get access to the protected members of RenderType. It is not called.
	private PortalRenderType() {
		super(NAME, DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS, BUFFER_SIZE, false, true, () -> {}, () -> {});
	}

	static RenderType get(ResourceLocation texture) {
		return TYPES.apply(texture);
	}
}
