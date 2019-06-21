package me.egg82.ae.events.enchants.inventory.inventoryMoveItem;

import java.util.function.Consumer;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.BukkitEnchantableItem;
import org.bukkit.event.inventory.InventoryMoveItemEvent;

public class InventoryMoveItemAdherence implements Consumer<InventoryMoveItemEvent> {
    public InventoryMoveItemAdherence() { }

    public void accept(InventoryMoveItemEvent event) {
        if (BukkitEnchantableItem.fromItemStack(event.getItem()).hasEnchantment(AdvancedEnchantment.ADHERENCE_CURSE)) {
            event.setCancelled(true);
        }
    }
}
