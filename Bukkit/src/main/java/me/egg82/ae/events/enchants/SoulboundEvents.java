package me.egg82.ae.events.enchants;

import java.util.ArrayList;
import java.util.List;
import me.egg82.ae.APIException;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.BukkitEnchantableItem;
import me.egg82.ae.api.GenericEnchantableItem;
import me.egg82.ae.events.EventHolder;
import me.egg82.ae.services.CollectionProvider;
import me.egg82.ae.utils.PermissionUtil;
import ninja.egg82.events.BukkitEvents;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class SoulboundEvents extends EventHolder {
    public SoulboundEvents(Plugin plugin) {
        events.add(
                BukkitEvents.subscribe(plugin, EntityDeathEvent.class, EventPriority.LOW)
                        .filter(e -> PermissionUtil.canUseEnchant(e.getEntity(), "ae.enchant.soulbound"))
                        .handler(this::death)
        );

        events.add(
                BukkitEvents.subscribe(plugin, PlayerRespawnEvent.class, EventPriority.MONITOR)
                        .handler(this::respawn)
        );
    }

    private void death(EntityDeathEvent event) {
        boolean player = false;
        if (event.getEntity() instanceof Player) {
            player = true;
        }

        List<ItemStack> removedItems = new ArrayList<>();

        for (ItemStack item : event.getDrops()) {
            GenericEnchantableItem enchantableItem = BukkitEnchantableItem.fromItemStack(item);

            boolean hasEnchantment;
            try {
                hasEnchantment = api.anyHasEnchantment(AdvancedEnchantment.SOULBOUND, enchantableItem);
            } catch (APIException ex) {
                logger.error(ex.getMessage(), ex);
                continue;
            }

            if (hasEnchantment) {
                removedItems.add(item);
            }
        }

        if (!removedItems.isEmpty()) {
            if (player) {
                CollectionProvider.getSoulboundItems(event.getEntity().getUniqueId()).addAll(removedItems);
            }
            event.getDrops().removeAll(removedItems);
        }
    }

    private void respawn(PlayerRespawnEvent event) {
        List<ItemStack> items = CollectionProvider.getAndClearSoulboundItems(event.getPlayer().getUniqueId());
        if (items == null || items.isEmpty()) {
            return;
        }

        event.getPlayer().getInventory().addItem(items.toArray(new ItemStack[0]));
    }
}
