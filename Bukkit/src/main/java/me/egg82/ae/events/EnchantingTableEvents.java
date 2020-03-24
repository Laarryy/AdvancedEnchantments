package me.egg82.ae.events;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.BukkitEnchantableItem;
import me.egg82.ae.api.BukkitEnchantment;
import me.egg82.ae.api.GenericEnchantment;
import me.egg82.ae.extended.CachedConfigValues;
import me.egg82.ae.services.material.MaterialLookup;
import me.egg82.ae.services.sound.SoundLookup;
import me.egg82.ae.utils.ConfigUtil;
import me.egg82.ae.utils.EnchantmentUtil;
import me.egg82.ae.utils.InventoryUtil;
import ninja.egg82.events.BukkitEventFilters;
import ninja.egg82.events.BukkitEvents;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventPriority;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.Plugin;

public class EnchantingTableEvents extends EventHolder {
    private final Plugin plugin;

    private static Material enchantedBookMaterial;
    private static boolean hasEnchantID;
    private static Sound enchantSound;

    static {
        Optional<Material> m = MaterialLookup.get("ENCHANTED_BOOK");
        if (!m.isPresent()) {
            throw new RuntimeException("Could not get enchanted book material.");
        }
        enchantedBookMaterial = m.get();

        Optional<Sound> s = SoundLookup.get("BLOCK_ENCHANTMENT_TABLE_USE");
        if (!s.isPresent()) {
            throw new RuntimeException("Could not get enchanting table sound.");
        }
        enchantSound = s.get();

        try {
            InventoryView.Property.valueOf("ENCHANT_ID1");
            hasEnchantID = true;
        } catch (IllegalArgumentException ignored) {
            hasEnchantID = false;
        }
    }

    public EnchantingTableEvents(Plugin plugin) {
        this.plugin = plugin;

        events.add(
                BukkitEvents.subscribe(plugin, EnchantItemEvent.class, EventPriority.NORMAL)
                        .filter(BukkitEventFilters.ignoreCancelled())
                        .filter(e -> e.getItem().getType() != enchantedBookMaterial) // TODO: Make books work
                        .handler(this::addEnchants)
        );

        if (hasEnchantID) {
            if (ConfigUtil.getDebugOrFalse()) {
                logger.info("Set to rewrite enchanting table properties.");
            }
            events.add(
                    BukkitEvents.subscribe(plugin, InventoryClickEvent.class, EventPriority.NORMAL)
                            .filter(BukkitEventFilters.ignoreCancelled())
                            .filter(e -> e.getView().getType() == InventoryType.ENCHANTING)
                            .filter(this::isTopOrMoved)
                            .handler(this::setWindow)
            );
            events.add(
                    BukkitEvents.subscribe(plugin, InventoryDragEvent.class, EventPriority.NORMAL)
                            .filter(BukkitEventFilters.ignoreCancelled())
                            .filter(e -> e.getView().getType() == InventoryType.ENCHANTING)
                            .filter(this::isTopOrMoved)
                            .handler(this::setWindow)
            );
        }
    }

    private boolean isTopOrMoved(InventoryClickEvent event) {
        if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY && event.getView().getBottomInventory().equals(InventoryUtil.getClickedInventory(event))) {
            return true;
        } else if (
                (
                        event.getAction() == InventoryAction.PLACE_ALL
                        || event.getAction() == InventoryAction.PLACE_ONE
                        || event.getAction() == InventoryAction.PLACE_SOME
                        || event.getAction() == InventoryAction.SWAP_WITH_CURSOR
                )
                && event.getView().getTopInventory().equals(InventoryUtil.getClickedInventory(event)))
        {
            return true;
        }

        return false;
    }

    private boolean isTopOrMoved(InventoryDragEvent event) {
        InventoryAction action;
        Inventory clicked = InventoryUtil.getClickedInventory(event);

        if (clicked == event.getView().getTopInventory()) {
            action = InventoryAction.MOVE_TO_OTHER_INVENTORY;
        } else {
            action = (event.getCursor() == null || event.getCursor().getAmount() == 0) ? InventoryAction.PLACE_ALL : InventoryAction.PLACE_SOME;
        }

        if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY && event.getView().getBottomInventory().equals(clicked)) {
            return true;
        } else if (
                (
                        action == InventoryAction.PLACE_ALL
                                || action == InventoryAction.PLACE_ONE
                                || action == InventoryAction.PLACE_SOME
                                || action == InventoryAction.SWAP_WITH_CURSOR
                )
                        && event.getView().getTopInventory().equals(clicked))
        {
            return true;
        }

        return false;
    }

    private void setWindow(InventoryEvent event) {
        if (ConfigUtil.getDebugOrFalse()) {
            logger.info("Removing enchanting table visual data from window.");
        }
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!event.getView().setProperty(InventoryView.Property.ENCHANT_ID1, -1)) {
                logger.warn("Could not set ENCHANT_ID1 to none.");
            }
            if (!event.getView().setProperty(InventoryView.Property.ENCHANT_ID2, -1)) {
                logger.warn("Could not set ENCHANT_ID2 to none.");
            }
            if (!event.getView().setProperty(InventoryView.Property.ENCHANT_ID3, -1)) {
                logger.warn("Could not set ENCHANT_ID3 to none.");
            }
        }, 1L);
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
                    } else {
                        if (ConfigUtil.getDebugOrFalse()) {
                            if (newEnchant == null) {
                                logger.info("New enchant is null. Skipping.");
                            } else if (kvp.getValue() < newEnchant.getMinLevel() || kvp.getValue() > newEnchant.getMaxLevel()) {
                                logger.info("Enchant " + newEnchant.getName() + " has a different level range than replaced enchant. Skipping.");
                            } else if (!event.getEnchanter().hasPermission("ae.enchant." + newEnchant.getName())) {
                                logger.info("Player does not have permissions to use enchant " + newEnchant.getName() + ". Skipping.");
                            }
                        }
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

        // Edge-case, if empty no exp is taken by Bukkit so we have to do it ourselves
        if (event.getEnchantsToAdd().isEmpty()) {
            int newLevel = event.getEnchanter().getLevel() - event.whichButton() - 1; // getExpLevelCost() will sometimes return 30 (max level?) so this hack is needed
            if (newLevel < 0) {
                newLevel = 0;
            }
            event.getEnchanter().setLevel(newLevel);
            event.getEnchanter().playSound(event.getEnchanter().getLocation(), enchantSound, 1.0f, 1.0f);
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
