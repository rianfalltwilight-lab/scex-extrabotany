package io.github.lounode.extrabotany.forge.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.botania.api.mana.ManaItem;
import vazkii.botania.common.item.BotaniaItems;
import top.theillusivec4.curios.api.CuriosApi;

@GameTestHolder("extrabotany")
@PrefixGameTestTemplate(false)
public final class ItemUseGameTests {
    private static Player player(GameTestHelper h, ItemStack stack) {
        var p = h.makeMockPlayer(GameType.SURVIVAL);
        p.setPos(h.absolutePos(new BlockPos(3, 4, 3)).getCenter());
        p.setNoGravity(true);
        p.setItemInHand(InteractionHand.MAIN_HAND, stack);
        for (int i = 0; i < 30; i++) p.tick();
        return p;
    }
    private static ManaItem mana(Player p) {
        var tablet = new ItemStack(BotaniaItems.MANA_TABLET);
        p.getInventory().setItem(2, tablet);
        var mana = ManaItem.LOOKUP.find(tablet); mana.addMana(50000); return mana;
    }
    @GameTest(template = "empty", timeoutTicks = 30)
    public static void conquestHasUsableSlotAndRealCapability(GameTestHelper h) {
        var item = io.github.lounode.extrabotany.common.item.ExtraBotanyItems.gemOfConquest;
        var stack = new ItemStack(item); var p = player(h, ItemStack.EMPTY);
        h.assertTrue(stack.is(ItemTags.create(ResourceLocation.parse("curios:curio"))), "Conquest gem lacks a slot usable without third party charm slots");
        var curio = CuriosApi.getCurio(stack).orElseThrow();
        var inventory = CuriosApi.getCuriosInventory(p).orElseThrow();
        var entry = inventory.getCurios().entrySet().stream().filter(e -> e.getValue().getStacks().getSlots() > 0).findFirst().orElseThrow();
        var context = new top.theillusivec4.curios.api.SlotContext(entry.getKey(), p, 0, false, true);
        h.assertTrue(curio.canEquip(context), "Conquest gem Curios capability refused equip");
        var modifiers = curio.getAttributeModifiers(context, ResourceLocation.parse("extrabotany:test_slot"));
        h.assertTrue(modifiers.isEmpty(), "The original gem gives no attribute modifier");
        entry.getValue().getStacks().setStackInSlot(0, stack);
        h.runAfterDelay(2, () -> {
            h.assertTrue(!vazkii.botania.common.handler.EquipmentHandler.findOrEmpty(item, p).isEmpty(), "Equipped conquest gem cannot be found after cache tick");
            h.succeed();
        });
    }
}
