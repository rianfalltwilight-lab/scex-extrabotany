package dev.scex.audit;

import com.google.gson.GsonBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.TreeMap;

/** Read actual frozen registries from an isolated server; stop after saving the dump. */
@Mod("scex_registry_audit")
public final class RegistryAudit {
    public RegistryAudit() { NeoForge.EVENT_BUS.addListener(RegistryAudit::started); }

    private static void started(ServerStartedEvent event) {
        String fixture = System.getProperty("scex.audit.fixture", "none");
        if (!fixture.equals("none")) fixture(event, fixture.equals("seed"));
        var result = new TreeMap<String, Object>();
        for (Registry<?> registry : BuiltInRegistries.REGISTRY) {
            var entries = new TreeMap<String, String>();
            registry.keySet().stream().filter(id -> id.getNamespace().equals("extrabotany"))
                    .forEach(id -> entries.put(id.toString(), registry.get(id).getClass().getName()));
            if (!entries.isEmpty()) result.put(registry.key().location().toString(), entries);
        }
        try {
            Files.writeString(Path.of("registry-dump.json"), new GsonBuilder().setPrettyPrinting().create().toJson(result));
        } catch (java.io.IOException ex) { throw new IllegalStateException("Registry evidence write failed", ex); }
        if (!Boolean.getBoolean("scex.audit.client")) event.getServer().execute(() -> event.getServer().halt(false));
    }

    private static void fixture(ServerStartedEvent event, boolean seed) {
        var level = event.getServer().overworld();
        var ids = BuiltInRegistries.ITEM.keySet().stream().filter(id -> id.getNamespace().equals("extrabotany") && !id.getPath().equals("enchanted_soil")).sorted().toList();
        var evidence = new TreeMap<String, Object>();
        if (ids.size() != 242) throw new IllegalStateException("Unexpected fixture item count " + ids.size());
        for (int index = 0; index < ids.size(); index++) {
            var pos = new net.minecraft.core.BlockPos(200 + index / 27, 90, 200);
            if (seed && index % 27 == 0) level.setBlockAndUpdate(pos, net.minecraft.world.level.block.Blocks.BARREL.defaultBlockState());
            if (!(level.getBlockEntity(pos) instanceof net.minecraft.world.Container container)) throw new IllegalStateException("Missing fixture container " + pos);
            var id = ids.get(index);
            if (seed) {
                var stack = new net.minecraft.world.item.ItemStack(BuiltInRegistries.ITEM.get(id));
                net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA, stack, tag -> {
                    tag.putString("scex_old_fixture", id.toString()); tag.putIntArray("unknown_payload", new int[]{3, -11, 90210});
                });
                container.setItem(index % 27, stack); container.setChanged();
            }
            var stack = container.getItem(index % 27);
            if (!stack.is(BuiltInRegistries.ITEM.get(id)) || !stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY)
                    .copyTag().getString("scex_old_fixture").equals(id.toString())
                    || !java.util.Arrays.equals(stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA).copyTag().getIntArray("unknown_payload"), new int[]{3, -11, 90210})) throw new IllegalStateException("Old item lost " + id);
            evidence.put(id.toString(), stack.save(level.registryAccess()).toString());
        }
        try { Files.writeString(Path.of(seed ? "old-fixture-seeded.json" : "old-fixture-reloaded.json"), new GsonBuilder().setPrettyPrinting().create().toJson(evidence)); }
        catch (java.io.IOException ex) { throw new IllegalStateException(ex); }
        System.out.println("SCEX_OLD_WORLD_FIXTURE " + (seed ? "SEEDED" : "RELOADED") + " items=" + evidence.size());
    }
}
