package me.egg82.ae.events.enchants;

import me.egg82.ae.APIException;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.BukkitEnchantableItem;
import me.egg82.ae.api.GenericEnchantableItem;
import me.egg82.ae.events.EventHolder;
import ninja.egg82.events.BukkitEventFilters;
import ninja.egg82.events.BukkitEvents;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.plugin.Plugin;

public class DecayEvents extends EventHolder {
    public DecayEvents(Plugin plugin) {
        try {
            Class.forName("org.bukkit.event.player.PlayerItemDamageEvent");
            events.add(
                    BukkitEvents.subscribe(plugin, PlayerItemDamageEvent.class, EventPriority.NORMAL)
                            .filter(BukkitEventFilters.ignoreCancelled())
                            .filter(e -> canUseEnchant(e.getPlayer(), "ae.curse.decay"))
                            .handler(this::damage)
            );
        } catch (ClassNotFoundException ignored) {}
    }

    private void damage(PlayerItemDamageEvent event) {
        GenericEnchantableItem enchantableItem = BukkitEnchantableItem.fromItemStack(event.getItem());

        boolean hasEnchantment;
        int level;
        try {
            hasEnchantment = api.anyHasEnchantment(AdvancedEnchantment.DECAY_CURSE, enchantableItem);
            level = api.getMaxLevel(AdvancedEnchantment.DECAY_CURSE, enchantableItem);
        } catch (APIException ex) {
            logger.error(ex.getMessage(), ex);
            return;
        }

        if (!hasEnchantment || level <= 0) {
            return;
        }

        event.setDamage(event.getDamage() + level * 2);
    }
}
