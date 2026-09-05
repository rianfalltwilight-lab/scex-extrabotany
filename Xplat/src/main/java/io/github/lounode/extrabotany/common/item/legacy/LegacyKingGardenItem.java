package io.github.lounode.extrabotany.common.item.legacy;

import io.github.lounode.extrabotany.common.entity.LegacyFlowerWeapon;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import vazkii.botania.api.mana.ManaItemHandler;
import java.util.Arrays;
import java.util.List;

public final class LegacyKingGardenItem extends Item {
    public static final LegacyKingGardenItem INSTANCE = new LegacyKingGardenItem();
    private static final String[] FLOWERS = {"extrabotany:blood_enchantress", "extrabotany:sunshine_lily", "extrabotany:moonlight_lily", "",
            "extrabotany:stonesia", "botania:entropinnyum", "botania:dreadthorne", "botania:medumone", "botania:thermalily",
            "botania:tigerseye", "botania:bellethorne", "botania:heisei_dream", "extrabotany:annoyingflower", "extrabotany:manalink",
            "extrabotany:omniviolet", "extrabotany:bellflower", "extrabotany:tinkle"};
    private LegacyKingGardenItem() { super(new Properties().stacksTo(1).rarity(net.minecraft.world.item.Rarity.EPIC).fireResistant()); }
    public static CompoundTag data(ItemStack stack) { return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag(); }
    public static int[] types(ItemStack stack) { return data(stack).getIntArray("type"); }
    public static boolean charging(ItemStack stack) { return data(stack).getBoolean("charging"); }
    public static int flowerType(ItemStack stack) {
        if (stack.isEmpty()) return -1;
        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        for (int i = 0; i < FLOWERS.length; i++) if (id.equals(FLOWERS[i])) return i;
        return -1;
    }
    public static boolean addFlower(ItemStack stack, ItemStack flower) {
        int type = flowerType(flower);
        int[] current = types(stack);
        if (!stack.is(INSTANCE) || type < 0 || current.length >= 20) return false;
        int[] next = Arrays.copyOf(current, current.length + 1); next[current.length] = type;
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putIntArray("type", next));
        return true;
    }
    private static void reset(ItemStack stack) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> { tag.putBoolean("charging", false); tag.putInt("weapons_spawned", 0); });
    }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putBoolean("charging", true));
        player.startUsingItem(hand);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
    @Override public void onUseTick(Level level, LivingEntity living, ItemStack stack, int remaining) {
        int[] types = types(stack);
        int spawned = Math.max(0, data(stack).getInt("weapons_spawned"));
        if (level.isClientSide() || remaining == getUseDuration(stack, living) || spawned >= types.length) return;
        if (living instanceof Player player && !ManaItemHandler.instance().requestManaExactForTool(stack, player, 100, true)) return;
        var look = living.getLookAngle().multiply(1, 0, 1);
        if (look.lengthSqr() == 0) { double yaw = Math.toRadians(living.getYRot() + 90); look = new Vec3(Math.cos(yaw), 0, Math.sin(yaw)); }
        look = look.normalize().scale(-2);
        int row = spawned / 5;
        var origin = living.position().add(0, 1.6, 0).add(look).add(0, 0, row * .1);
        var axis = look.normalize().cross(new Vec3(-1, 0, -1)).normalize();
        if (axis.lengthSqr() == 0) axis = new Vec3(1, 0, 0);
        var vector = axis.scale(row * 3.5 + 5);
        var normal = look.normalize();
        double angle = spawned % 5 * Math.PI / 4 - Math.PI / 2;
        var offset = vector.scale(Math.cos(angle)).add(normal.cross(vector).scale(Math.sin(angle)))
                .add(normal.scale(normal.dot(vector) * (1 - Math.cos(angle))));
        if (offset.y < 0) offset = offset.multiply(1, -1, 1);
        var weapon = new LegacyFlowerWeapon(LegacyFlowerWeapon.TYPE, level);
        weapon.setOwner(living); weapon.setPos(origin.add(offset)); weapon.setYRot(living.getYRot());
        weapon.configure(types[spawned], spawned, Mth.wrapDegrees(-living.getYRot() + 180));
        level.addFreshEntity(weapon);
        level.playSound(null, weapon.blockPosition(), SoundEvents.TRIDENT_RETURN, SoundSource.PLAYERS, 1, 1 + level.random.nextFloat() * 3);
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putInt("weapons_spawned", spawned + 1));
    }
    @Override public void releaseUsing(ItemStack stack, Level level, LivingEntity living, int left) {
        if (data(stack).getInt("weapons_spawned") >= types(stack).length) reset(stack);
    }
    @Override public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!level.isClientSide() && !selected && charging(stack)) reset(stack);
        super.inventoryTick(stack, level, entity, slot, selected);
    }
    @Override public int getUseDuration(ItemStack stack, LivingEntity living) { return 72000; }
    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.BOW; }
    @Override public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flags) {
        int[] configured = types(stack);
        tooltip.add(Component.translatable("tooltip.extrabotany.king_garden.configuration", configured.length, 20).withStyle(ChatFormatting.GRAY));
        if (configured.length == 0) return;
        tooltip.add(Component.translatable("tooltip.extrabotany.king_garden.flowers").withStyle(ChatFormatting.DARK_GRAY));
        for (int i = 0; i < Math.min(5, configured.length); i++) {
            int type = configured[i];
            var name = type >= 0 && type < FLOWERS.length && !FLOWERS[type].isEmpty()
                    ? Component.translatable(BuiltInRegistries.BLOCK.get(ResourceLocation.parse(FLOWERS[type])).getDescriptionId())
                    : Component.literal("extrabotany:unknown");
            tooltip.add(Component.literal(" - ").append(name).withStyle(ChatFormatting.DARK_GRAY));
        }
        if (configured.length > 5) tooltip.add(Component.translatable("tooltip.extrabotany.king_garden.more", configured.length - 5).withStyle(ChatFormatting.DARK_GRAY));
    }
}
