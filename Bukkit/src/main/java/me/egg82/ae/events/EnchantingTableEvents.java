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

    private double highestWeight = 0;
    private final NavigableMap<Double, AdvancedEnchantment> customEnchants = new TreeMap<>();

    public EnchantingTableEvents(Plugin plugin) {
        for (AdvancedEnchantment enchant : AdvancedEnchantment.values()) {
            highestWeight += 1;
            customEnchants.put(highestWeight, enchant);
        }

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

        Map<GenericEnchantment, Integer> currentEnchants = getEnchants(event.getEnchantsToAdd());
        Map<GenericEnchantment, Integer> newEnchants = new HashMap<>();

        BukkitEnchantableItem item = BukkitEnchantableItem.fromItemStack(event.getItem());

        for (Map.Entry<GenericEnchantment, Integer> kvp : currentEnchants.entrySet()) {
            if (Math.random() < cachedConfig.get().getEnchantChance()) {
                int tries = 0;
                AdvancedEnchantment newEnchant;
                do {
                    // Get a new random (custom) enchant to replace the vanilla one
                    newEnchant = getNextEnchant();
                    tries++;

                    if (
                            newEnchant != null // We don't want nulls
                            && !newEnchant.isCurse() // We don't want curses
                            && kvp.getValue() >= newEnchant.getMinLevel() && kvp.getValue() <= newEnchant.getMaxLevel() // We want enchants that fit the level
                            && event.getEnchanter().hasPermission("ae.enchant." + newEnchant.getName()) // We don't want enchants we don't have perms to use
                            && newEnchant.canEnchant(item) // We don't want enchants that conflict with the item, or that conflict with enchants currently on the item
                            && !conflicts(newEnchant, currentEnchants, kvp.getKey()) // We don't want enchants that conflict with Bukkit enchants that will be applied (except the one we're replacing)
                            && !conflicts(newEnchant, newEnchants) // We don't want enchants that conflict with other new enchants
                    ) {
                        break;
                    }
                } while (tries < 100);

                if (newEnchant == null) {
                    // Too many tries (and failures) - skip this one
                    continue;
                }

                // This all works because we're iterating through a copy of the map
                event.getEnchantsToAdd().remove((Enchantment) kvp.getKey().getConcrete());
                newEnchants.put(newEnchant, kvp.getValue());
            }
        }

        // Add all the new (custom) enchants
        item.setEnchantmentLevels(newEnchants);
    }

    private AdvancedEnchantment getNextEnchant() {
        // Select least-recently used enchant with random (weighted random)
        Map.Entry<Double, AdvancedEnchantment> entry = customEnchants.lowerEntry(rand.nextDouble() * highestWeight);
        if (entry == null) {
            return null;
        }

        // Increase weight (decrease chance) of selected item
        highestWeight += Math.max(highestWeight, entry.getKey() + 1);
        customEnchants.remove(entry.getKey(), entry.getValue());
        customEnchants.put(entry.getKey() + 1, entry.getValue());
        return entry.getValue();
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
