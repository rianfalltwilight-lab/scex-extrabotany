package io.github.lounode.extrabotany.client.gui;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import vazkii.botania.api.block.WandHUD;
import vazkii.botania.api.block_entity.BindableSpecialFlowerBlockEntity;
import vazkii.botania.api.block_entity.FunctionalFlowerBlockEntity;
import vazkii.botania.api.block_entity.GeneratingFlowerBlockEntity;

import io.github.lounode.extrabotany.common.block.block_entity.ManaMachineBlockEntity;
import io.github.lounode.extrabotany.common.block.flower.functional.AnnoyingFlowerBlockEntity;
import io.github.lounode.extrabotany.common.block.flower.functional.EnchanterBlockEntity;
import io.github.lounode.extrabotany.common.block.flower.functional.ManalinkBlockEntity;
import io.github.lounode.extrabotany.common.block.flower.functional.StardustLotusBlockEntity;
import io.github.lounode.extrabotany.common.block.flower.generating.BellflowerBlockEntity;

import java.util.function.Function;

import static io.github.lounode.extrabotany.common.block.block_entity.ExtraBotanyBlockEntities.*;
import static io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks.*;

/**
 * 登记森林法杖的界面：每种方块实体用哪一个界面。
 * Records the displays of the Wand of the Forest: which display each block entity type uses.
 *
 * <p>界面是客户端的代码，所以这张表在客户端的包里，方块实体的类不引用它。
 * <p>A display is client code, thus this table is in the client package, and the block entity classes do not refer to it.
 */
public final class WandHuds {

	private WandHuds() {}

	/**
	 * 接收一条登记：一个界面的构造方法，以及使用这个界面的方块实体类型。
	 * Receives one record: the constructor of a display, and the block entity types that use this display.
	 */
	@FunctionalInterface
	public interface Registrar {
		void accept(Function<BlockEntity, WandHUD> factory, BlockEntityType<?>... types);
	}

	public static void register(Registrar registrar) {
		// 没有自己的数值要显示的花，使用植物魔法的默认界面。
		// The flowers that have no value of their own to show use the default display of Botania.
		registrar.accept(be -> new BindableSpecialFlowerBlockEntity.BindableFlowerWandHud<>((FunctionalFlowerBlockEntity) be),
				TRADE_ORCHID, WOODIENIA, SERENITIAN, MIRROWTUNIA, NECROFLEUR, NECROFLEUR_CHIBI);
		registrar.accept(be -> new BindableSpecialFlowerBlockEntity.BindableFlowerWandHud<>((GeneratingFlowerBlockEntity) be),
				REIKARLILY, STONESIA, EDELWEISS, RESONCUND, SUNSHINE_LILY, MOONLIGHT_LILY,
				TWINSTAR, OMNIVIOLET, TINKLE, BLOOD_ENCHANTRESS);

		registrar.accept(be -> new StardustLotusWandHud((StardustLotusBlockEntity) be), STARDUST_LOTUS);
		registrar.accept(be -> new BellflowerWandHud((BellflowerBlockEntity) be), BELLFLOWER);
		registrar.accept(be -> new AnnoyingFlowerWandHud((AnnoyingFlowerBlockEntity) be), ANNOYINGFLOWER);
		registrar.accept(be -> new ManalinkWandHud((ManalinkBlockEntity) be), MANALINK);
		registrar.accept(be -> new EnchanterWandHud((EnchanterBlockEntity) be), ENCHANTER);

		registrar.accept(be -> new ManaMachineWandHud((ManaMachineBlockEntity) be),
				MANA_BUFFER, QUANTUM_MANA_BUFFER, MANA_GENERATOR, MANA_LIQUEFACTION);
	}
}
