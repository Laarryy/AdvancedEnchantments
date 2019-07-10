package me.egg82.ae.events.enchants.inventory.inventoryClick;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import me.egg82.ae.api.BukkitEnchantableItem;
import me.egg82.ae.api.GenericEnchantment;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class InventoryClickGrindstoneRewrite implements Consumer<InventoryClickEvent> {
    public InventoryClickGrindstoneRewrite() { }

    public void accept(InventoryClickEvent event) {
        ItemStack resultItem = event.getCurrentItem();

        if (resultItem == null || resultItem.getType() == Material.AIR) {
            return;
        }

        BukkitEnchantableItem enchantableResultItem = BukkitEnchantableItem.fromItemStack(resultItem);
        removeEnchants(enchantableResultItem);

        enchantableResultItem.rewriteMeta();
        event.setCurrentItem((ItemStack) enchantableResultItem.getConcrete());
    }

    private void removeEnchants(BukkitEnchantableItem enchantableResultItem) {
        Set<GenericEnchantment> removedEnchants = new HashSet<>();
        for (Map.Entry<GenericEnchantment, Integer> kvp : enchantableResultItem.getEnchantments().entrySet()) {
            if (!kvp.getKey().isCurse()) {
                removedEnchants.add(kvp.getKey());
            }
        }
        enchantableResultItem.removeEnchantments(removedEnchants);
    }
}
