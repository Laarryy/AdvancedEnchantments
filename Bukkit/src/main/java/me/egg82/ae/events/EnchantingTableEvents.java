package me.egg82.ae.events;

import java.util.*;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.BukkitEnchantableItem;
import me.egg82.ae.api.BukkitEnchantment;
import me.egg82.ae.api.GenericEnchantment;
import me.egg82.ae.extended.CachedConfigValues;
import me.egg82.ae.utils.ConfigUtil;
import ninja.egg82.events.BukkitEventFilters;
import ninja.egg82.events.BukkitEvents;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventPriority;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.plugin.Plugin;

public class EnchantingTableEvents extends EventHolder {
    private final Plugin plugin;
    private final Random rand = new Random();

    public EnchantingTableEvents(Plugin plugin) {
        this.plugin = plugin;

        events.add(
                BukkitEvents.subscribe(plugin, EnchantItemEvent.class, EventPriority.NORMAL)
                        .filter(BukkitEventFilters.ignoreCancelled())
                        .handler(this::addEnchants)
        );
        events.add(
                BukkitEvents.subscribe(plugin, EnchantItemEvent.class, EventPriority.HIGH)
                        .filter(BukkitEventFilters.ignoreCancelled())
                        .handler(this::rewriteItem)
        );
    }

    private void addEnchants(EnchantItemEvent event) {
        Optional<CachedConfigValues> cachedConfig = ConfigUtil.getCachedConfig();
        if (!cachedConfig.isPresent() || cachedConfig.get().getEnchantChance() == 0.0d) {
            return;
        }

        List<AdvancedEnchantment> customEnchants = new ArrayList<>(AdvancedEnchantment.values());
        Map<GenericEnchantment, Integer> currentEnchants = getEnchants(event.getEnchantsToAdd());
        Map<GenericEnchantment, Integer> newEnchants = new HashMap<>();

        BukkitEnchantableItem item = BukkitEnchantableItem.fromItemStack(event.getItem());

        for (Map.Entry<GenericEnchantment, Integer> kvp : currentEnchants.entrySet()) {
            if (Math.random() < cachedConfig.get().getEnchantChance()) {
                int tries = 0;
                AdvancedEnchantment newEnchant;
                do {
                    // Get a new random (custom) enchant to replace the vanilla one
                    newEnchant = customEnchants.get(rand.nextInt(customEnchants.size()));
                    tries++;

                    // We don't want curses
                    // We don't want enchants we don't have perms to use
                    // We don't want enchants that conflict with the item
                    // We don't want enchants that conflict with enchants currently on the item
                    // We don't want enchants that conflict with Bukkit enchants that will be applied (except the one we're replacing)
                    // We don't want enchants that conflict with other new enchants
                    if (!newEnchant.isCurse() && event.getEnchanter().hasPermission("ae.enchant." + newEnchant.getName()) && newEnchant.canEnchant(item) && !conflicts(newEnchant, currentEnchants, kvp.getKey()) && !conflicts(newEnchant, newEnchants)) {
                        break;
                    }
                } while (tries < 100);

                if (tries >= 100) {
                    // Too many tries (and failures) - skip this one
                    continue;
                }

                // This all works because we're iterating through a copy of the map
                event.getEnchantsToAdd().remove((Enchantment) kvp.getKey().getConcrete());
                int newLevel = Math.max(newEnchant.getMinLevel(), Math.min(newEnchant.getMaxLevel(), kvp.getValue())); // Clamp value
                newEnchants.put(newEnchant, newLevel);
            }
        }

        // Add all the new (custom) enchants
        item.setEnchantmentLevels(newEnchants);
    }

    private Map<GenericEnchantment, Integer> getEnchants(Map<Enchantment, Integer> original) {
        Map<GenericEnchantment, Integer> retVal = new HashMap<>();
        for (Map.Entry<Enchantment, Integer> kvp : original.entrySet()) {
            retVal.put(BukkitEnchantment.fromEnchant(kvp.getKey()), kvp.getValue());
        }
        return retVal;
    }

    private boolean conflicts(GenericEnchantment newEnchant, Map<GenericEnchantment, Integer> otherEnchants, GenericEnchantment currentEnchant) {
        for (Map.Entry<GenericEnchantment, Integer> kvp : otherEnchants.entrySet()) {
            if (kvp.getKey().equals(currentEnchant)) {
                continue;
            }

            if (newEnchant.conflictsWith(kvp.getKey())) {
                return true;
            }
        }

        return false;
    }

    private boolean conflicts(GenericEnchantment newEnchant, Map<GenericEnchantment, Integer> otherEnchants) {
        for (Map.Entry<GenericEnchantment, Integer> kvp : otherEnchants.entrySet()) {
            if (newEnchant.conflictsWith(kvp.getKey()) || kvp.getKey().conflictsWith(newEnchant)) {
                return true;
            }
        }

        return false;
    }

    private void rewriteItem(EnchantItemEvent event) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            BukkitEnchantableItem item = BukkitEnchantableItem.fromItemStack(event.getItem());
            item.rewriteEnchantMeta();
        }, 1L);
    }
}
