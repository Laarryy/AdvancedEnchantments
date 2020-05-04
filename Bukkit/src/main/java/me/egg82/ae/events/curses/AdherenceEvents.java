package me.egg82.ae.events.curses;

import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.BukkitEnchantableItem;
import me.egg82.ae.events.EventHolder;
import me.egg82.ae.utils.InventoryUtil;
import me.egg82.ae.utils.PermissionUtil;
import ninja.egg82.events.BukkitEventFilters;
import ninja.egg82.events.BukkitEvents;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;

public class AdherenceEvents extends EventHolder {
    public AdherenceEvents(Plugin plugin) {
        events.add(
                BukkitEvents.subscribe(plugin, InventoryClickEvent.class, EventPriority.LOW)
                        .filter(BukkitEventFilters.ignoreCancelled())
                        .filter(e -> PermissionUtil.canUseEnchant(e.getWhoClicked(), "ae.curse.adherence"))
                        .filter(e -> !e.getWhoClicked().hasPermission("ae.admin"))
                        .handler(this::click)
        );
        events.add(
                BukkitEvents.subscribe(plugin, InventoryDragEvent.class, EventPriority.LOW)
                        .filter(BukkitEventFilters.ignoreCancelled())
                        .filter(e -> PermissionUtil.canUseEnchant(e.getWhoClicked(), "ae.curse.adherence"))
                        .filter(e -> !e.getWhoClicked().hasPermission("ae.admin"))
                        .handler(this::drag)
        );

        events.add(
                BukkitEvents.subscribe(plugin, InventoryMoveItemEvent.class, EventPriority.LOW)
                        .filter(BukkitEventFilters.ignoreCancelled())
                        .filter(e -> {
                            if (e.getSource().getViewers().isEmpty()) {
                                return false;
                            }
                            if (!PermissionUtil.canUseEnchant(e.getSource().getViewers().get(0), "ae.curse.adherence")) {
                                return true;
                            }
                            if (e.getSource().getViewers().get(0).hasPermission("ae.admin")) {
                                return true;
                            }
                            return false;
                        })
                        .handler(this::move)
        );
    }

    private void click(InventoryClickEvent event) {
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

    private void drag(InventoryDragEvent event) {
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

    private void move(InventoryMoveItemEvent event) {
        if (BukkitEnchantableItem.fromItemStack(event.getItem()).hasEnchantment(AdvancedEnchantment.ADHERENCE_CURSE)) {
            event.setCancelled(true);
        }
    }
}
