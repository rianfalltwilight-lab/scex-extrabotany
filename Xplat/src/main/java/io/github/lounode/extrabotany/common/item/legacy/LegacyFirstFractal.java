package io.github.lounode.extrabotany.common.item.legacy;

import io.github.lounode.extrabotany.common.entity.LegacyPhantomSword;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.handler.BotaniaSounds;

public final class LegacyFirstFractal extends LegacyRelicSword {
    public LegacyFirstFractal() {
        super(Tiers.NETHERITE, new Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant().attributes(
                SwordItem.createAttributes(Tiers.NETHERITE, 7, -1.6F)
                        .withModifierAdded(Attributes.MOVEMENT_SPEED, modifier("movement_speed", .3, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), EquipmentSlotGroup.MAINHAND)
                        .withModifierAdded(Attributes.ATTACK_SPEED, modifier("attack_speed", .15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), EquipmentSlotGroup.MAINHAND)
                        .withModifierAdded(Attributes.ENTITY_INTERACTION_RANGE, modifier("interaction_range", 5, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)), 640);
    }
    private static AttributeModifier modifier(String id, double amount, AttributeModifier.Operation operation) {
        return new AttributeModifier(ResourceLocation.parse("extrabotany:first_fractal_" + id), amount, operation);
    }
    @Override protected int manaCost(Player player) { return player.getAbilities().instabuild ? 0 : 640; }
    @Override protected boolean canUse(Player player) {
        if (!(player instanceof ServerPlayer server)) return true;
        var advancement = server.server.getAdvancements().get(ResourceLocation.parse("extrabotany:main/herrscher_defeat"));
        if (advancement != null && server.getAdvancements().getOrStartProgress(advancement).isDone()) return true;
        player.displayClientMessage(Component.translatable("extrabotany.message.advancement_required").withStyle(ChatFormatting.RED), true);
        return false;
    }
    @Override protected void perform(Player player, Entity target) {
        var aim = resolveTarget(player, target, 80);
        double angle = -Math.PI + Math.PI * 2 * player.getRandom().nextDouble();
        for (int i = 0; i < 4; i++) {
            double pitch = 1.8849555921538759 * player.getRandom().nextDouble();
            var start = aim.add(13 * Math.sin(pitch) * Math.cos(angle), 13 * Math.cos(pitch), 13 * Math.sin(pitch) * Math.sin(angle));
            player.level().addFreshEntity(LegacyPhantomSword.create(player, start, aim, i == 3 ? 0 : 5 + i * 5, i == 3 ? 9 : player.getRandom().nextInt(10)));
            if (i < 3) angle += Math.PI * 2 * player.getRandom().nextDouble() * .08 + 1.0681415022205298;
        }
        player.level().playSound(null, player.blockPosition(), BotaniaSounds.TERRABLADE, SoundSource.PLAYERS, .4F, 1.4F);
    }
    @Override public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (!level.isClientSide() && entity instanceof Player player && stack.isDamaged()
                && ManaItemHandler.instance().requestManaExactForTool(stack, player, 320, true)) stack.setDamageValue(stack.getDamageValue() - 1);
    }
}
