package me.egg82.ae.events.enchants.inventory.inventoryDrag;

import java.util.function.Consumer;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.BukkitEnchantableItem;
import me.egg82.ae.utils.InventoryUtil;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

public class InventoryDragAdherence implements Consumer<InventoryDragEvent> {
    public InventoryDragAdherence() { }

    public void accept(InventoryDragEvent event) {
        InventoryAction action;
        Inventory clicked = InventoryUtil.getClickedInventory(event);

        if (clicked == event.getView().getTopInventory()) {
            action = InventoryAction.MOVE_TO_OTHER_INVENTORY;
        } else {
            action = (event.getCursor() == null || event.getCursor().getAmount() == 0) ? InventoryAction.PLACE_ALL : InventoryAction.PLACE_SOME;
        }

        if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            BukkitEnchantableItem item = BukkitEnchantableItem.fromItemStack(event.getOldCursor());
            if (item.hasEnchantment(AdvancedEnchantment.ADHERENCE_CURSE)) {
                event.setCancelled(true);
            }
        } else if (
                action == InventoryAction.HOTBAR_MOVE_AND_READD
                || action == InventoryAction.HOTBAR_SWAP
                || action == InventoryAction.PLACE_ALL
                || action == InventoryAction.PLACE_ONE
                || action == InventoryAction.PLACE_SOME
                || action == InventoryAction.SWAP_WITH_CURSOR
        ) {
            BukkitEnchantableItem item = BukkitEnchantableItem.fromItemStack(event.getCursor());
            if (clicked == event.getView().getTopInventory() && item.hasEnchantment(AdvancedEnchantment.ADHERENCE_CURSE)) {
                event.setCancelled(true);
            }
        }
    }
}
