package io.github.lounode.extrabotany.common.item.legacy;

import io.github.lounode.extrabotany.common.entity.LegacySubspace;
import io.github.lounode.extrabotany.common.brew.ExtraBotanyMobEffects;
import io.github.lounode.extrabotany.common.sounds.ExtraBotanySounds;
import io.github.lounode.extrabotany.xplat.EXplatAbstractions;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import vazkii.botania.api.mana.ManaItemHandler;
import java.util.List;

public final class LegacySubspaceSpearItem extends LegacyRelicSword {
    public LegacySubspaceSpearItem() {
        super(Tiers.DIAMOND, new Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant().attributes(
                SwordItem.createAttributes(Tiers.DIAMOND, 8, -1.6F)
                        .withModifierAdded(Attributes.BLOCK_INTERACTION_RANGE, reach("block"), EquipmentSlotGroup.MAINHAND)
                        .withModifierAdded(Attributes.ENTITY_INTERACTION_RANGE, reach("entity"), EquipmentSlotGroup.MAINHAND)), 600);
    }
    private static AttributeModifier reach(String type) { return new AttributeModifier(ResourceLocation.parse("extrabotany:spear_of_subspace_" + type + "_reach"), 2, AttributeModifier.Operation.ADD_VALUE); }
    @Override protected void perform(Player player, Entity target) {
        var portal = new LegacySubspace(LegacySubspace.TYPE, player.level()); portal.setOwner(player);
        portal.configure(1, 24, 5, 10, .4F + player.getRandom().nextFloat() * .15F, Mth.wrapDegrees(-player.getYRot() + 180));
        portal.setPos(player.getX(), player.getY() + 2.5 + player.getRandom().nextFloat() * .2, player.getZ());
        portal.setYRot(player.getYRot()); player.level().addFreshEntity(portal);
    }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand); return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }
    @Override public int getUseDuration(ItemStack stack, LivingEntity entity) { return 200; }
    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.NONE; }
    @Override public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (level.isClientSide() || !(entity instanceof Player player) || !stack.is(this)) return;
        var relic = EXplatAbstractions.INSTANCE.findRelic(stack);
        if (relic == null || !relic.isRightPlayer(player)) return;
        boolean paid = ManaItemHandler.instance().requestManaExactForTool(stack, player, 10000, true);
        player.getCooldowns().addCooldown(this, paid ? 600 : 1200);
        if (!paid) return;
        player.setSprinting(true); player.setDeltaMovement(player.getDeltaMovement().add(0, 1.5, 0)); player.hurtMarked = true;
        player.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.ETERNITY, 120));
        level.playSound(null, player.blockPosition(), ExtraBotanySounds.SPEAR_OF_SUBSPACE_USE, SoundSource.PLAYERS, 1.6F, 1);
        var look = player.getLookAngle().multiply(1, 0, 1);
        if (look.lengthSqr() == 0) { double yaw = Math.toRadians(player.getYRot() + 90); look = new Vec3(Math.cos(yaw), 0, Math.sin(yaw)); }
        look = look.normalize().scale(-2); var axis = look.normalize().cross(new Vec3(-1, 0, -1)).normalize();
        if (axis.lengthSqr() == 0) axis = new Vec3(1, 0, 0);
        for (int i = 0; i < 24; i++) {
            int row = i / 8; var origin = player.position().add(0, 1.6, 0).add(look).add(0, 0, row * .1);
            var offset = axis.scale(row * 3.5 + 5).xRot((float) (i % 8 * Math.PI / 7 - Math.PI / 2));
            if (offset.y < 0) offset = offset.multiply(1, -1, 1);
            var portal = new LegacySubspace(LegacySubspace.TYPE, level); portal.setOwner(player);
            portal.configure(0, 120, 15 + level.random.nextInt(12), 10 + level.random.nextInt(10), 1 + level.random.nextFloat(), Mth.wrapDegrees(-player.getYRot() + 180));
            portal.setPos(origin.add(offset).add(0, -.5 + level.random.nextFloat(), 0)); portal.setYRot(player.getYRot()); level.addFreshEntity(portal);
        }
    }
    @Override public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flags) {
        tooltip.add(Component.translatable("tooltip.extrabotany.spear_of_subspace").withStyle(ChatFormatting.GRAY)); super.appendHoverText(stack, context, tooltip, flags);
    }
}
