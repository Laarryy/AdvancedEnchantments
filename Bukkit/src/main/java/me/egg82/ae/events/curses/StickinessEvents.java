package me.egg82.ae.events.curses;

import java.util.concurrent.TimeUnit;
import me.egg82.ae.APIException;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.BukkitEnchantableItem;
import me.egg82.ae.api.GenericEnchantableItem;
import me.egg82.ae.events.EventHolder;
import me.egg82.ae.services.CollectionProvider;
import me.egg82.ae.utils.PermissionUtil;
import ninja.egg82.events.BukkitEventFilters;
import ninja.egg82.events.BukkitEvents;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.plugin.Plugin;

public class StickinessEvents extends EventHolder {
    public StickinessEvents(Plugin plugin) {
        events.add(
                BukkitEvents.subscribe(plugin, PlayerItemHeldEvent.class, EventPriority.NORMAL)
                        .filter(BukkitEventFilters.ignoreCancelled())
                        .filter(e -> PermissionUtil.canUseEnchant(e.getPlayer(), "ae.curse.stickiness"))
                        .handler(this::held)
        );
        events.add(
                BukkitEvents.subscribe(plugin, PlayerItemHeldEvent.class, EventPriority.LOW)
                        .filter(BukkitEventFilters.ignoreCancelled())
                        .filter(e -> CollectionProvider.getStickiness().containsKey(e.getPlayer().getUniqueId()))
                        .handler(e -> e.setCancelled(true))
        );
    }

    private void held(PlayerItemHeldEvent event) {
        GenericEnchantableItem enchantableItem = BukkitEnchantableItem.fromItemStack(event.getPlayer().getInventory().getItem(event.getNewSlot()));

        boolean hasEnchantment;
        int level;
        try {
            hasEnchantment = api.anyHasEnchantment(AdvancedEnchantment.STICKINESS_CURSE, enchantableItem);
            level = api.getMaxLevel(AdvancedEnchantment.STICKINESS_CURSE, enchantableItem);
        } catch (APIException ex) {
            logger.error(ex.getMessage(), ex);
            return;
        }

        if (!hasEnchantment || level <= 0) {
            return;
        }

        CollectionProvider.getStickiness().put(event.getPlayer().getUniqueId(), level, level * 750L, TimeUnit.MILLISECONDS);
    }
}
