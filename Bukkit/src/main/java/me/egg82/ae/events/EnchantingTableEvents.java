package me.egg82.ae.events;

import java.util.*;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.BukkitEnchantableItem;
import me.egg82.ae.api.BukkitEnchantment;
import me.egg82.ae.api.GenericEnchantment;
import me.egg82.ae.extended.CachedConfigValues;
import me.egg82.ae.utils.ConfigUtil;
import me.egg82.ae.utils.EnchantmentUtil;
import ninja.egg82.events.BukkitEventFilters;
import ninja.egg82.events.BukkitEvents;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventPriority;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.plugin.Plugin;

public class EnchantingTableEvents extends EventHolder {
    private final Plugin plugin;

    public EnchantingTableEvents(Plugin plugin) {
        this.plugin = plugin;

        events.add(
                BukkitEvents.subscribe(plugin, EnchantItemEvent.class, EventPriority.NORMAL)
                        .filter(BukkitEventFilters.ignoreCancelled())
                        .handler(this::addEnchants)
        );
    }

    private void addEnchants(EnchantItemEvent event) {
        Optional<CachedConfigValues> cachedConfig = ConfigUtil.getCachedConfig();
        if (!cachedConfig.isPresent() || cachedConfig.get().getEnchantChance() == 0.0d) {
            return;
        }

        Map<GenericEnchantment, Integer> currentEnchants = getEnchants(event.getEnchantsToAdd());
        Map<GenericEnchantment, Integer> newEnchants = new HashMap<>();

        boolean rewritten = false;
        BukkitEnchantableItem item = BukkitEnchantableItem.fromItemStack(event.getItem());

        for (Map.Entry<GenericEnchantment, Integer> kvp : currentEnchants.entrySet()) {
            if (Math.random() <= cachedConfig.get().getEnchantChance()) {
                if (ConfigUtil.getDebugOrFalse()) {
                    logger.info("[Enchanting Table] Trying to replace vanilla enchant " + kvp.getKey().getName() + " with custom enchant on " + event.getItem());
                }

                int tries = 0;
                AdvancedEnchantment newEnchant;
                do {
                    // Get a new random (custom) enchant to replace the vanilla one
                    newEnchant = EnchantmentUtil.getNextEnchant();
                    tries++;

                    if (
                            newEnchant != null // We don't want nulls
                            && kvp.getValue() >= newEnchant.getMinLevel() && kvp.getValue() <= newEnchant.getMaxLevel() // We want enchants that fit the level
                            && event.getEnchanter().hasPermission("ae.enchant." + newEnchant.getName()) // We don't want enchants we don't have perms to use
                            && newEnchant.canEnchant(item) // We don't want enchants that conflict with the item, or that conflict with enchants currently on the item
                            && !conflicts(newEnchant, currentEnchants, kvp.getKey()) // We don't want enchants that conflict with Bukkit enchants that will be applied (except the one we're replacing)
                            && !conflicts(newEnchant, newEnchants) // We don't want enchants that conflict with other new enchants
                    ) {
                        break;
                    }
                    newEnchant = null; // Set enchant to null to prevent adding. The "break" above will skip this
                } while (tries < 100);

                if (newEnchant == null) {
                    // Too many tries (and failures) - skip this one
                    if (ConfigUtil.getDebugOrFalse()) {
                        logger.warn("[Enchanting Table] Could not replace vanilla enchant " + kvp.getKey().getName());
                    }
                    continue;
                }

                rewritten = true;
                // This all works because we're iterating through a copy of the map
                event.getEnchantsToAdd().remove((Enchantment) kvp.getKey().getConcrete());
                newEnchants.put(newEnchant, kvp.getValue());

                if (ConfigUtil.getDebugOrFalse()) {
                    logger.info("[Enchanting Table] Successfully replaced vanilla enchant " + kvp.getKey().getName() + " with custom enchant " + newEnchant.getName());
                }
            }
        }

        // Add all the new (custom) enchants
        item.setEnchantmentLevels(newEnchants);

        if (rewritten) {
            Bukkit.getScheduler().runTaskLater(plugin, item::rewriteEnchantMeta, 1L);
        }
    }

    private Map<GenericEnchantment, Integer> getEnchants(Map<Enchantment, Integer> original) {
        Map<GenericEnchantment, Integer> retVal = new HashMap<>();
        for (Map.Entry<Enchantment, Integer> kvp : original.entrySet()) {
            retVal.put(BukkitEnchantment.fromEnchant(kvp.getKey()), kvp.getValue());
        }
        return retVal;
    }

    private boolean conflicts(GenericEnchantment newEnchant, Map<GenericEnchantment, Integer> otherEnchants, GenericEnchantment currentEnchant) {
        if (ConfigUtil.getDebugOrFalse()) {
            logger.info("Checking if enchant " + newEnchant.getName() + " is compatible with newly-added vanilla enchants.");
        }

        for (Map.Entry<GenericEnchantment, Integer> kvp : otherEnchants.entrySet()) {
            if (kvp.getKey().equals(currentEnchant)) {
                continue;
            }

            if (newEnchant.conflictsWith(kvp.getKey())) {
                if (ConfigUtil.getDebugOrFalse()) {
                    logger.info("Enchant " + newEnchant.getName() + " conflicts with other enchant " + kvp.getKey().getName() + " on item.");
                }
                return true;
            }
        }

        if (ConfigUtil.getDebugOrFalse()) {
            logger.info("Compatible: true");
        }
        return false;
    }

    private boolean conflicts(GenericEnchantment newEnchant, Map<GenericEnchantment, Integer> otherEnchants) {
        if (ConfigUtil.getDebugOrFalse()) {
            logger.info("Checking if enchant " + newEnchant.getName() + " is compatible with newly-added custom enchants.");
        }

        for (Map.Entry<GenericEnchantment, Integer> kvp : otherEnchants.entrySet()) {
            if (newEnchant.conflictsWith(kvp.getKey()) || kvp.getKey().conflictsWith(newEnchant)) {
                if (ConfigUtil.getDebugOrFalse()) {
                    logger.info("Enchant " + newEnchant.getName() + " conflicts with other enchant " + kvp.getKey().getName() + " on item.");
                }
                return true;
            }
        }

        if (ConfigUtil.getDebugOrFalse()) {
            logger.info("Compatible: true");
        }
        return false;
    }
}
