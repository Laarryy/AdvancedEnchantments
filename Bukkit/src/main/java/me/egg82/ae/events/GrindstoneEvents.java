package me.egg82.ae.events;

import java.util.*;
import me.egg82.ae.api.BukkitEnchantableItem;
import me.egg82.ae.api.BukkitEnchantment;
import me.egg82.ae.api.GenericEnchantment;
import me.egg82.ae.utils.InventoryUtil;
import ninja.egg82.events.BukkitEventFilters;
import ninja.egg82.events.BukkitEvents;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class GrindstoneEvents extends EventHolder {
    private final Random rand = new Random();

    public GrindstoneEvents(Plugin plugin) {
        try {
            InventoryType.valueOf("GRINDSTONE");
            events.add(
                    BukkitEvents.subscribe(plugin, InventoryClickEvent.class, EventPriority.HIGH)
                            .filter(BukkitEventFilters.ignoreCancelled())
                            .filter(e -> e.getInventory().getType() == InventoryType.valueOf("GRINDSTONE"))
                            .filter(e -> InventoryUtil.getClickedInventory(e) == e.getView().getTopInventory())
                            .filter(e -> e.getRawSlot() == 2)
                            .handler(this::rewriteItem)
            );
        } catch (IllegalArgumentException ignored) {}
    }

    private void rewriteItem(InventoryClickEvent event) {
        ItemStack resultItem = event.getCurrentItem();

        if (resultItem == null || resultItem.getType() == Material.AIR) {
            return;
        }

        BukkitEnchantableItem enchantableResultItem = BukkitEnchantableItem.fromItemStack(resultItem);
        int maxExp = removeEnchants(enchantableResultItem);
        int variation = Math.max(0, maxExp - (maxExp / 4));

        enchantableResultItem.rewriteMeta();
        event.setCurrentItem((ItemStack) enchantableResultItem.getConcrete());

        if (event.getWhoClicked() instanceof Player && maxExp + variation > 0) {
            ((Player) event.getWhoClicked()).giveExp(Math.max(1, rand.nextInt(maxExp + variation) + (maxExp - variation)));
        }
    }

    private int removeEnchants(BukkitEnchantableItem enchantableResultItem) {
        Set<GenericEnchantment> removedEnchants = new HashSet<>();
        int retVal = 0;
        for (Map.Entry<GenericEnchantment, Integer> kvp : enchantableResultItem.getEnchantments().entrySet()) {
            if (!(kvp.getKey() instanceof BukkitEnchantment) && !kvp.getKey().isCurse()) {
                removedEnchants.add(kvp.getKey());
                if (kvp.getValue() > 0) {
                    retVal += 6 * kvp.getValue();
                }
            }
        }
        enchantableResultItem.removeEnchantments(removedEnchants);
        return retVal;
    }
}
