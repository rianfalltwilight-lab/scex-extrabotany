package io.github.lounode.extrabotany.common.item.legacy;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import vazkii.botania.common.item.equipment.bauble.BaubleItem;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Legacy accessory transforms refer to the exact imported models and textures. */
public final class LegacyCosmetics extends BaubleItem {
    public record Transform(boolean head, float x, float y, float z, float sx, float sy, float sz) {}
    public static final Map<String, LegacyCosmetics> ITEMS = new LinkedHashMap<>();
    public final String id;
    public final Transform transform;
    static {
        add("fox_ear", true, 0, -.8F, -.1F, .8F, -.8F, -.8F);
        add("fox_mask", true, .02F, -.3F, -.3F, .66F, -.65F, -.65F);
        add("pylon", true, 0, -.8F, -.1F, .5F, -.5F, -.5F);
        for (String biome : List.of("jungle", "ocean", "snowfield", "standard")) add("goggle_" + biome, true, 0, -.2F, -.3F, .55F, -.55F, -.55F);
        add("black_glasses", true, 0, -.2F, -.3F, .55F, .55F, -.55F);
        add("thug_life", true, 0, -.2F, -.3F, .7F, -.7F, -.7F);
        add("super_crown", true, 0, -.7F, -.1F, .65F, -.65F, -.65F);
        add("mask", true, 0, -.3F, -.3F, .65F, -.65F, -.65F);
        add("red_scarf", false, 0, .16F, -.15F, .55F, -.55F, -.55F);
    }
    private static void add(String id, boolean head, float x, float y, float z, float sx, float sy, float sz) {
        ITEMS.put(id, new LegacyCosmetics(id, new Transform(head, x, y, z, sx, sy, sz)));
    }
    private LegacyCosmetics(String id, Transform transform) {
        super(new Properties().stacksTo(1)); this.id = id; this.transform = transform;
    }
    @Override public boolean hasRender(ItemStack stack, LivingEntity living) { return super.hasRender(stack, living) && living instanceof Player; }
    @Override public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flags) {
        super.appendHoverText(stack, context, tooltip, flags);
        if (id.equals("fox_mask")) for (int i = 0; i < 3; i++) tooltip.add(Component.translatable("extrabotany.foxmaskinfo" + i).withStyle(ChatFormatting.ITALIC));
    }
}
