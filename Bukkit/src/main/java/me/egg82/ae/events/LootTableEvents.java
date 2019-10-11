package me.egg82.ae.events;

import com.destroystokyo.paper.loottable.LootableInventoryReplenishEvent;
import java.util.Collection;
import java.util.Optional;
import java.util.Random;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.BukkitEnchantableItem;
import me.egg82.ae.extended.CachedConfigValues;
import me.egg82.ae.utils.ConfigUtil;
import me.egg82.ae.utils.EnchantmentUtil;
import ninja.egg82.events.BukkitEventFilters;
import ninja.egg82.events.BukkitEvents;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class LootTableEvents extends EventHolder {
    private final Plugin plugin;

    public LootTableEvents(Plugin plugin) {
        this.plugin = plugin;

        try {
            Class.forName("com.destroystokyo.paper.loottable.LootableInventoryReplenishEvent");
            events.add(
                    BukkitEvents.subscribe(plugin, LootableInventoryReplenishEvent.class, EventPriority.NORMAL)
                    .filter(BukkitEventFilters.ignoreCancelled())
                    .handler(this::addEnchants)
            );
        } catch (ClassNotFoundException ignored) { }
    }

    public void addEnchants(LootableInventoryReplenishEvent event) {
        Optional<CachedConfigValues> cachedConfig = ConfigUtil.getCachedConfig();
        if (!cachedConfig.isPresent() || (cachedConfig.get().getLootEnchantChance() == 0.0d && cachedConfig.get().getLootCurseChance() == 0.0d)) {
            return;
        }

        if (!event.getInventory().hasLootTable()) {
            return;
        }

        long seed = event.getInventory().getSeed();
        LootTable oldTable = event.getInventory().getLootTable();
        event.getInventory().setLootTable(new CustomLootTable(oldTable, event.getPlayer(), cachedConfig.get().getLootEnchantChance(), cachedConfig.get().getLootCurseChance()), seed);

        Bukkit.getScheduler().runTaskLater(plugin, () -> event.getInventory().setLootTable(oldTable, seed), 1L);
    }

    private class CustomLootTable implements LootTable {
        private final LootTable backingTable;
        private final Player viewingPlayer;
        private final double enchantChance;
        private final double curseChance;

        CustomLootTable(LootTable backingTable, Player viewingPlayer, double enchantChance, double curseChance) {
            this.backingTable = backingTable;
            this.viewingPlayer = viewingPlayer;
            this.enchantChance = enchantChance;
            this.curseChance = curseChance;
        }

        @NotNull
        public Collection<ItemStack> populateLoot(@NotNull Random random, @NotNull LootContext context) {
            Collection<ItemStack> retVal = backingTable.populateLoot(random, context);

            for (ItemStack i : retVal) {
                BukkitEnchantableItem item = BukkitEnchantableItem.fromItemStack(i);
                boolean rewritten = false;

                if (Math.random() <= enchantChance) {
                    if (ConfigUtil.getDebugOrFalse()) {
                        logger.info("[Loot Table] Trying to add custom enchant to " + i);
                    }

                    int tries = 0;
                    AdvancedEnchantment newEnchant;
                    do {
                        // Get a new random (custom) enchant
                        newEnchant = EnchantmentUtil.getNextEnchant();
                        tries++;

                        if (
                                newEnchant != null // We don't want nulls
                                && viewingPlayer.hasPermission("ae.enchant." + newEnchant.getName()) // We don't want enchants we don't have perms to use
                                && newEnchant.canEnchant(item) // We don't want enchants that conflict with the item, or that conflict with enchants currently on the item
                        ) {
                            break;
                        }
                        newEnchant = null; // Set enchant to null to prevent adding. The "break" above will skip this
                    } while (tries < 100);

                    if (newEnchant != null) {
                        rewritten = true;
                        item.setEnchantmentLevel(newEnchant, random.nextInt(newEnchant.getMaxLevel() - newEnchant.getMinLevel() + 1) + newEnchant.getMinLevel());

                        if (ConfigUtil.getDebugOrFalse()) {
                            logger.info("[Loot Table] Successfully added custom enchant " + newEnchant.getName());
                        }
                    } else {
                        if (ConfigUtil.getDebugOrFalse()) {
                            logger.warn("[Loot Table] Could not add custom enchant.");
                        }
                    }
                }

                if (Math.random() <= curseChance) {
                    if (ConfigUtil.getDebugOrFalse()) {
                        logger.info("[Loot Table] Trying to add custom curse to " + i);
                    }

                    int tries = 0;
                    AdvancedEnchantment newEnchant;
                    do {
                        // Get a new random (custom) curse
                        newEnchant = EnchantmentUtil.getNextCurse();
                        tries++;

                        if (
                                newEnchant != null // We don't want nulls
                                && viewingPlayer.hasPermission("ae.curse." + newEnchant.getName()) // We don't want enchants we don't have perms to use
                                && newEnchant.canEnchant(item) // We don't want enchants that conflict with the item, or that conflict with enchants currently on the item
                        ) {
                            break;
                        }
                        newEnchant = null; // Set enchant to null to prevent adding. The "break" above will skip this
                    } while (tries < 100);

                    if (newEnchant != null) {
                        rewritten = true;
                        item.setEnchantmentLevel(newEnchant, random.nextInt(newEnchant.getMaxLevel() - newEnchant.getMinLevel() + 1) + newEnchant.getMinLevel());

                        if (ConfigUtil.getDebugOrFalse()) {
                            logger.info("[Loot Table] Successfully added custom curse " + newEnchant.getName());
                        }
                    } else {
                        if (ConfigUtil.getDebugOrFalse()) {
                            logger.warn("[Loot Table] Could not add custom curse.");
                        }
                    }
                }

                if (rewritten) {
                    Bukkit.getScheduler().runTaskLater(plugin, item::rewriteEnchantMeta, 1L);
                }
            }

            return retVal;
        }

        public void fillInventory(@NotNull Inventory inventory, @NotNull Random random, @NotNull LootContext context) {
            Collection<ItemStack> loot = populateLoot(random, context);
            for (ItemStack l : loot) {
                inventory.addItem(l);
            }
        }

        @NotNull
        public NamespacedKey getKey() { return new NamespacedKey(plugin, "custom"); }
    }
}
