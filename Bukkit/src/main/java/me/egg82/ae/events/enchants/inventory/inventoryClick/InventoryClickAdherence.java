package me.egg82.ae.events.enchants.inventory.inventoryClick;

import java.util.function.Consumer;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.BukkitEnchantableItem;
import me.egg82.ae.utils.InventoryUtil;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InventoryClickAdherence implements Consumer<InventoryClickEvent> {
    public InventoryClickAdherence() { }

    public void accept(InventoryClickEvent event) {
        if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            BukkitEnchantableItem item = BukkitEnchantableItem.fromItemStack(event.getCurrentItem());
            if (item.hasEnchantment(AdvancedEnchantment.ADHERENCE_CURSE)) {
                event.setCancelled(true);
            }
        } else if (
                event.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD
                || event.getAction() == InventoryAction.HOTBAR_SWAP
                || event.getAction() == InventoryAction.PLACE_ALL
                || event.getAction() == InventoryAction.PLACE_ONE
                || event.getAction() == InventoryAction.PLACE_SOME
                || event.getAction() == InventoryAction.SWAP_WITH_CURSOR
        ) {
            BukkitEnchantableItem item = BukkitEnchantableItem.fromItemStack(event.getCursor());
            if (InventoryUtil.getClickedInventory(event) == event.getView().getTopInventory() && item.hasEnchantment(AdvancedEnchantment.ADHERENCE_CURSE)) {
                event.setCancelled(true);
            }
        }
    }
}
