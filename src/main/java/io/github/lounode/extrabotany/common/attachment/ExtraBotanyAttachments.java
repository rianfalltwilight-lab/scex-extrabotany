package io.github.lounode.extrabotany.common.attachment;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.attachment.AttachmentType;

import java.util.function.BiConsumer;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * 本模组的数据附加类型。数据附加是 NeoForge 1.21.1 用来代替实体能力的机制。
 * The data attachment types of this mod. Data attachments are the mechanism of NeoForge 1.21.1 that replaces entity capabilities.
 */
public final class ExtraBotanyAttachments {
	/**
	 * 雷之律者的能量。它保存在玩家存档里，只同步给玩家自己，并且在玩家死亡后清零。
	 * The energy of the Herrscher of Thunder. It is saved with the player, is only synchronized to that player, and goes back to zero after the player dies.
	 */
	public static final AttachmentType<HerrscherEnergy> HERRSCHER_ENERGY = AttachmentType.builder(() -> new HerrscherEnergy())
			.serialize(HerrscherEnergy.CODEC)
			.sync((holder, receiver) -> holder == receiver, HerrscherEnergy.STREAM_CODEC)
			.build();

	/**
	 * 薪炎的玩家状态。它保存在玩家存档里，只同步给玩家自己，并且在玩家死亡后重置。
	 * The player state of the Flamescion. It is saved with the player, is only synchronized to that player, and is reset after the player dies.
	 */
	public static final AttachmentType<FlamescionState> FLAMESCION_STATE = AttachmentType.builder(() -> new FlamescionState())
			.serialize(FlamescionState.CODEC)
			.sync((holder, receiver) -> holder == receiver, FlamescionState.STREAM_CODEC)
			.build();

	private ExtraBotanyAttachments() {}

	public static void register(BiConsumer<AttachmentType<?>, ResourceLocation> r) {
		r.accept(HERRSCHER_ENERGY, prefix("herrscher_energy"));
		r.accept(FLAMESCION_STATE, prefix("flamescion_state"));
	}
}
