package me.egg82.ae.events.enchants.inventory.prepareAnvil;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import me.egg82.ae.api.BukkitEnchantableItem;
import me.egg82.ae.api.BukkitEnchantment;
import me.egg82.ae.api.GenericEnchantment;
import me.egg82.ae.services.material.MaterialLookup;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

public class PrepareAnvilRewrite implements Consumer<PrepareAnvilEvent> {
    private static Material enchantedBookMaterial;

    static {
        Optional<Material> m = MaterialLookup.get("ENCHANTED_BOOK");
        if (!m.isPresent()) {
            throw new RuntimeException("Could not get enchanted book material.");
        }
        enchantedBookMaterial = m.get();
    }

    private final Plugin plugin;

    public PrepareAnvilRewrite(Plugin plugin) {
        this.plugin = plugin;
    }

    public void accept(PrepareAnvilEvent event) {
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
        Bukkit.getScheduler().runTaskLater(plugin, () -> enchantableCarryoverItem.rewriteMeta(), 1L);
    }

    private void applyEnchants(Map<GenericEnchantment, Integer> enchants, BukkitEnchantableItem enchantableCarryoverItem) {
        // Add all enchants from sacrifice item
        for (Map.Entry<GenericEnchantment, Integer> kvp : enchants.entrySet()) {
            if (enchantableCarryoverItem.hasEnchantment(kvp.getKey())) {
                // carryover has enchant
                if (kvp.getValue() > enchantableCarryoverItem.getEnchantmentLevel(kvp.getKey())) {
                    // sacrifice's level is greater, so raise level to sacrifice's level
                    enchantableCarryoverItem.setEnchantmentLevel(kvp.getKey(), kvp.getValue());
                } else if (kvp.getValue() == enchantableCarryoverItem.getEnchantmentLevel(kvp.getKey())) {
                    // sacrifice's level is equal, so raise level by one (if applicable)
                    if (enchantableCarryoverItem.getEnchantmentLevel(kvp.getKey()) < kvp.getKey().getMaxLevel()) {
                        enchantableCarryoverItem.setEnchantmentLevel(kvp.getKey(), enchantableCarryoverItem.getEnchantmentLevel(kvp.getKey()) + 1);
                    }
                }
                // Do nothing if sacrifice's level is lower
                continue;
            }

            if (kvp.getKey().canEnchant(enchantableCarryoverItem)) {
                // Only add enchants that are compatible
                enchantableCarryoverItem.setEnchantmentLevel(kvp.getKey(), kvp.getValue());
            }
        }
    }

    private Map<GenericEnchantment, Integer> toGenericEnchants(Map<Enchantment, Integer> enchants) {
        Map<GenericEnchantment, Integer> retVal = new HashMap<>();
        for (Map.Entry<Enchantment, Integer> kvp : enchants.entrySet()) {
            retVal.put(BukkitEnchantment.fromEnchant(kvp.getKey()), kvp.getValue());
        }
        return retVal;
    }
}
