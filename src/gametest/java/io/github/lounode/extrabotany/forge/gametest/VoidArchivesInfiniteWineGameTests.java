package io.github.lounode.extrabotany.forge.gametest;

import io.github.lounode.extrabotany.common.brew.BrewUtil;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.item.relic.void_archives.variants.InfiniteWine;
import io.github.lounode.extrabotany.xplat.EXplatAbstractions;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.botania.api.mana.ManaItem;
import vazkii.botania.common.brew.BotaniaBrews;
import vazkii.botania.common.item.BotaniaItems;

import java.util.UUID;

@GameTestHolder("extrabotany")
@PrefixGameTestTemplate(false)
public final class VoidArchivesInfiniteWineGameTests {
    private VoidArchivesInfiniteWineGameTests() {}

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void wineRequiresOwnerAndFullManaBeforeApplyingEffects(GameTestHelper h) {
        var wine = wine(h);
        var player = player(h);
        var mana = supplyMana(player, 20_000);
        EXplatAbstractions.INSTANCE.findRelic(wine).bindToUUID(UUID.randomUUID());
        player.setHealth(1);
        InfiniteWine.INSTANCE.finishUsingItem(wine, h.getLevel(), player);
        h.assertTrue(player.getHealth() == 1 && mana.getMana() == 20_000,
                "A foreign Void Archives wine bypassed its ownership or mana gate");

        EXplatAbstractions.INSTANCE.findRelic(wine).bindToUUID(player.getUUID());
        mana.addMana(-20_000);
        InfiniteWine.INSTANCE.finishUsingItem(wine, h.getLevel(), player);
        h.assertTrue(player.getHealth() == 1 && mana.getMana() == 0,
                "Void Archives wine applied effects after a failed 20,000 mana request");
        h.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void wineConsumesManaOnceAndAmplifiesInstantEffectOnce(GameTestHelper h) {
        var wine = wine(h);
        var player = player(h);
        player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);
        player.setHealth(1);
        var mana = supplyMana(player, 20_000);
        EXplatAbstractions.INSTANCE.findRelic(wine).bindToUUID(player.getUUID());
        BrewUtil.setBrew(wine, BotaniaBrews.MENDING);

        var brewEffect = BotaniaBrews.MENDING.getPotionEffects(wine).getFirst();
        var expected = player.getHealth();
        brewEffect.getEffect().value().applyInstantenousEffect(player, player, player,
                brewEffect.getAmplifier() + InfiniteWine.INSTANCE.getAmplifierAddition(), 1F);
        expected = player.getHealth();
        player.setHealth(1);
        InfiniteWine.INSTANCE.finishUsingItem(wine, h.getLevel(), player);
        h.assertTrue(player.getHealth() == expected,
                "Void Archives wine applied the instant amplifier more than once");
        h.assertTrue(mana.getMana() == 0, "Void Archives wine did not consume exactly 20,000 mana");
        h.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void wineKeepsLastingEffectDurationAndSingleAmplifier(GameTestHelper h) {
        var wine = wine(h);
        var player = player(h);
        var mana = supplyMana(player, 20_000);
        EXplatAbstractions.INSTANCE.findRelic(wine).bindToUUID(player.getUUID());
        BrewUtil.setBrew(wine, io.github.lounode.extrabotany.common.brew.ExtraBotanyBrews.revolution);
        InfiniteWine.INSTANCE.finishUsingItem(wine, h.getLevel(), player);
        var haste = player.getEffect(MobEffects.DIG_SPEED);
        h.assertTrue(haste != null && haste.getAmplifier() == 3 && haste.getDuration() == 2700,
                "Void Archives wine changed the lasting brew amplifier or duration");
        h.assertTrue(mana.getMana() == 0, "Lasting wine effect did not consume exactly 20,000 mana");
        h.succeed();
    }

    private static ItemStack wine(GameTestHelper h) {
        var stack = new ItemStack(ExtraBotanyItems.voidArchives);
        EXplatAbstractions.INSTANCE.findRelic(stack).bindToUUID(UUID.randomUUID());
        BrewUtil.setBrew(stack, BotaniaBrews.MENDING);
        return stack;
    }

    private static Player player(GameTestHelper h) {
        return h.makeMockPlayer(GameType.SURVIVAL);
    }

    private static ManaItem supplyMana(Player player, int amount) {
        var tablet = new ItemStack(BotaniaItems.MANA_TABLET);
        var mana = ManaItem.LOOKUP.find(tablet);
        mana.addMana(amount);
        player.getInventory().setItem(2, tablet);
        return mana;
    }
}
