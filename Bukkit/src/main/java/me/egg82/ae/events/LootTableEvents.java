package me.egg82.ae.events;

import com.destroystokyo.paper.loottable.LootableInventoryReplenishEvent;
import java.util.Optional;
import me.egg82.ae.core.CustomLootTable;
import me.egg82.ae.extended.CachedConfigValues;
import me.egg82.ae.utils.ConfigUtil;
import ninja.egg82.events.BukkitEventFilters;
import ninja.egg82.events.BukkitEvents;
import org.bukkit.Bukkit;
import org.bukkit.event.EventPriority;
import org.bukkit.loot.LootTable;
import org.bukkit.plugin.Plugin;

public class LootTableEvents extends EventHolder {
    private final Plugin plugin;

    public LootTableEvents(Plugin plugin) {
        this.plugin = plugin;

        events.add(
                BukkitEvents.subscribe(plugin, LootableInventoryReplenishEvent.class, EventPriority.NORMAL)
                .filter(BukkitEventFilters.ignoreCancelled())
                .handler(this::addEnchants)
        );
    }

    private void addEnchants(LootableInventoryReplenishEvent event) {
        Optional<CachedConfigValues> cachedConfig = ConfigUtil.getCachedConfig();
        if (!cachedConfig.isPresent() || (cachedConfig.get().getLootEnchantChance() == 0.0d && cachedConfig.get().getLootCurseChance() == 0.0d)) {
            return;
        }

        if (!event.getInventory().hasLootTable()) {
            return;
        }

        long seed = event.getInventory().getSeed();
        LootTable oldTable = event.getInventory().getLootTable();
        event.getInventory().setLootTable(new CustomLootTable(plugin, oldTable, event.getPlayer(), cachedConfig.get().getLootEnchantChance(), cachedConfig.get().getLootCurseChance()), seed);

        Bukkit.getScheduler().runTaskLater(plugin, () -> event.getInventory().setLootTable(oldTable, seed), 1L);
    }
}
