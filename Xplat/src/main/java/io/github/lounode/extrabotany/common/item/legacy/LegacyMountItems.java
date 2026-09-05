package io.github.lounode.extrabotany.common.item.legacy;

import io.github.lounode.extrabotany.common.entity.LegacyMount;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import vazkii.botania.common.item.equipment.bauble.BaubleItem;
import java.util.Map;

public final class LegacyMountItems {
    public static final Map<String, Item> ITEMS = Map.of("motor", new VehicleItem(false), "cosmic_car_key", new VehicleItem(true),
            "motor_accessory", new Accessory(false), "cosmic_car_key_accessory", new Accessory(true));
    private LegacyMountItems() {}
    private static Item.Properties properties() { return new Item.Properties().stacksTo(1).rarity(Rarity.EPIC); }
    public static final class Accessory extends BaubleItem {
        public final boolean flying;
        private Accessory(boolean flying) { super(properties()); this.flying = flying; }
        public LegacyMount create(Level level) { var mount = (flying ? LegacyMount.UFO : LegacyMount.MOTOR).create(level); mount.accessory(true); return mount; }
    }
    private static final class VehicleItem extends Item {
        private final boolean flying;
        private VehicleItem(boolean flying) { super(properties()); this.flying = flying; }
        @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            var stack = player.getItemInHand(hand);
            var hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY);
            if (hit.getType() != HitResult.Type.BLOCK) return InteractionResultHolder.pass(stack);
            var eye = player.getEyePosition(1);
            for (var entity : level.getEntities(player, player.getBoundingBox().expandTowards(player.getViewVector(1).scale(5)).inflate(1), EntitySelector.NO_SPECTATORS.and(entity -> entity.isPickable())))
                if (entity.getBoundingBox().inflate(entity.getPickRadius()).contains(eye)) return InteractionResultHolder.pass(stack);
            var mount = (flying ? LegacyMount.UFO : LegacyMount.MOTOR).create(level);
            mount.setPos(hit.getLocation()); mount.setYRot(player.getYRot()); if (!flying) mount.owner(player.getUUID());
            if (!level.noCollision(mount, mount.getBoundingBox().inflate(-.1))) return InteractionResultHolder.fail(stack);
            if (!level.isClientSide()) { level.addFreshEntity(mount); if (!player.getAbilities().instabuild) stack.shrink(1); }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
    }
}
