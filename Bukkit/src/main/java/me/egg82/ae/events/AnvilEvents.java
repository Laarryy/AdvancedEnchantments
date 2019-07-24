package me.egg82.ae.events;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import me.egg82.ae.api.BukkitEnchantableItem;
import me.egg82.ae.api.BukkitEnchantment;
import me.egg82.ae.api.GenericEnchantment;
import me.egg82.ae.services.material.MaterialLookup;
import me.egg82.ae.utils.InventoryUtil;
import ninja.egg82.events.BukkitEventFilters;
import ninja.egg82.events.BukkitEvents;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

public class AnvilEvents extends EventHolder {
    private final Plugin plugin;

    private static Material enchantedBookMaterial;

    static {
        Optional<Material> m = MaterialLookup.get("ENCHANTED_BOOK");
        if (!m.isPresent()) {
            throw new RuntimeException("Could not get enchanted book material.");
        }
        enchantedBookMaterial = m.get();
    }

    public AnvilEvents(Plugin plugin) {
        this.plugin = plugin;

        try {
            Class.forName("org.bukkit.event.inventory.PrepareAnvilEvent");
            events.add(
                    BukkitEvents.subscribe(plugin, PrepareAnvilEvent.class, EventPriority.HIGH)
                            .handler(this::rewriteItem)
            );
        } catch (ClassNotFoundException ignored) {
            events.add(
                    BukkitEvents.subscribe(plugin, InventoryClickEvent.class, EventPriority.HIGH)
                            .filter(BukkitEventFilters.ignoreCancelled())
                            .filter(e -> e.getInventory().getType() == InventoryType.ANVIL)
                            .filter(e -> InventoryUtil.getClickedInventory(e) == e.getView().getTopInventory())
                            .filter(e -> e.getRawSlot() == 2)
                            .handler(this::rewriteItem)
            );
        }
    }

    private void rewriteItem(PrepareAnvilEvent event) {
        ItemStack carryoverItem = event.getInventory().getItem(0);
        ItemStack sacrificeItem = event.getInventory().getItem(1);

        if (carryoverItem == null || carryoverItem.getType() == Material.AIR || sacrificeItem == null || sacrificeItem.getType() == Material.AIR || event.getResult() == null || event.getResult().getType() == Material.AIR) {
            return;
        }

        BukkitEnchantableItem enchantableCarryoverItem = BukkitEnchantableItem.fromItemStack(carryoverItem.clone()); // Use a clone, not the real item

        if (sacrificeItem.getType() == enchantedBookMaterial) {
            if (!sacrificeItem.hasItemMeta()) {
                return;
            }
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) sacrificeItem.getItemMeta();
            if (!meta.hasStoredEnchants()) {
                return;
            }

            applyEnchants(toGenericEnchants(meta.getStoredEnchants()), enchantableCarryoverItem);
        } else {
            if (!sacrificeItem.hasItemMeta()) {
                return;
            }
            ItemMeta meta = sacrificeItem.getItemMeta();
            if (!meta.hasEnchants()) {
                return;
            }

            BukkitEnchantableItem enchantableSacrificeItem = BukkitEnchantableItem.fromItemStack(sacrificeItem);
            applyEnchants(enchantableSacrificeItem.getEnchantments(), enchantableCarryoverItem);
        }

        event.setResult((ItemStack) enchantableCarryoverItem.getConcrete());
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // Rewrite the item meta on the next tick, so the client actually gets the update
            enchantableCarryoverItem.rewriteMeta();
            // This event gets triggered (without a corresponding inventory update) when the player resizes their window
            // Bit of a workaround, but it works I guess
            for (HumanEntity e : event.getViewers()) {
                if (!(e instanceof Player)) {
                    continue;
                }
                ((Player) e).updateInventory();
            }
        }, 1L);
    }

    private void applyEnchants(Map<GenericEnchantment, Integer> enchants, BukkitEnchantableItem enchantableCarryoverItem) {
        Map<GenericEnchantment, Integer> newEnchants = new HashMap<>();

        // Add all enchants from sacrifice item
        for (Map.Entry<GenericEnchantment, Integer> kvp : enchants.entrySet()) {
            if (enchantableCarryoverItem.hasEnchantment(kvp.getKey())) {
                // carryover has enchant
                if (kvp.getValue() > enchantableCarryoverItem.getEnchantmentLevel(kvp.getKey())) {
                    // sacrifice's level is greater, so raise level to sacrifice's level
                    newEnchants.put(kvp.getKey(), kvp.getValue());
                } else if (kvp.getValue() == enchantableCarryoverItem.getEnchantmentLevel(kvp.getKey())) {
                    // sacrifice's level is equal, so raise level by one (if applicable)
                    if (enchantableCarryoverItem.getEnchantmentLevel(kvp.getKey()) < kvp.getKey().getMaxLevel()) {
                        newEnchants.put(kvp.getKey(), enchantableCarryoverItem.getEnchantmentLevel(kvp.getKey()) + 1);
                    }
                }
                // Do nothing if sacrifice's level is lower
                continue;
            }

            if (kvp.getKey().canEnchant(enchantableCarryoverItem)) {
                // Only add enchants that are compatible
                newEnchants.put(kvp.getKey(), kvp.getValue());
            }
        }

        enchantableCarryoverItem.setEnchantmentLevels(newEnchants);
    }

    private Map<GenericEnchantment, Integer> toGenericEnchants(Map<Enchantment, Integer> enchants) {
        Map<GenericEnchantment, Integer> retVal = new HashMap<>();
        for (Map.Entry<Enchantment, Integer> kvp : enchants.entrySet()) {
            retVal.put(BukkitEnchantment.fromEnchant(kvp.getKey()), kvp.getValue());
        }
        return retVal;
    }

    private void rewriteItem(InventoryClickEvent event) {
        ItemStack carryoverItem = event.getInventory().getItem(0);
        ItemStack sacrificeItem = event.getInventory().getItem(1);
        ItemStack resultItem = event.getCurrentItem();

        if (carryoverItem == null || carryoverItem.getType() == Material.AIR || sacrificeItem == null || sacrificeItem.getType() == Material.AIR || resultItem == null || resultItem.getType() == Material.AIR) {
            return;
        }

        BukkitEnchantableItem enchantableResultItem = BukkitEnchantableItem.fromItemStack(resultItem);

        if (sacrificeItem.getType() == enchantedBookMaterial) {
            if (!sacrificeItem.hasItemMeta()) {
                return;
            }
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) sacrificeItem.getItemMeta();
            if (!meta.hasStoredEnchants()) {
                return;
            }

            applyEnchants(toGenericEnchants(meta.getStoredEnchants()), enchantableResultItem);
        } else {
            if (!sacrificeItem.hasItemMeta()) {
                return;
            }
            ItemMeta meta = sacrificeItem.getItemMeta();
            if (!meta.hasEnchants()) {
                return;
            }

            BukkitEnchantableItem enchantableSacrificeItem = BukkitEnchantableItem.fromItemStack(sacrificeItem);
            applyEnchants(enchantableSacrificeItem.getEnchantments(), enchantableResultItem);
        }

        enchantableResultItem.rewriteMeta();
        event.setCurrentItem((ItemStack) enchantableResultItem.getConcrete());
    }
}
