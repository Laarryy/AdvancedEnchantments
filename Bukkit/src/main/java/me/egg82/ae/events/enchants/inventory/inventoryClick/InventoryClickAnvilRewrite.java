package me.egg82.ae.events.enchants.inventory.inventoryClick;

import java.util.function.Consumer;
import me.egg82.ae.api.BukkitEnchantableItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class InventoryClickAnvilRewrite implements Consumer<InventoryClickEvent> {
    private final Plugin plugin;

    public InventoryClickAnvilRewrite(Plugin plugin) {
        this.plugin = plugin;
    }

    public void accept(InventoryClickEvent event) {
        ItemStack[] items = event.getInventory().getContents();

        if (items[0] == null || items[1] == null) {
            return;
        }

        if (items[0].getType() != Material.ENCHANTED_BOOK && items[1].getType() != Material.ENCHANTED_BOOK) {
            return;
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            BukkitEnchantableItem item = BukkitEnchantableItem.fromItemStack(event.getCurrentItem());
            item.rewriteMeta();
        }, 1L);
    }
}
