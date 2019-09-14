package me.egg82.ae.events.enchants;

import me.egg82.ae.events.EventHolder;
import me.egg82.ae.utils.PermissionUtil;
import me.egg82.ae.utils.SoulsUtil;
import ninja.egg82.events.BukkitEventFilters;
import ninja.egg82.events.BukkitEvents;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.Plugin;

public class EtherealEvents extends EventHolder {
    public EtherealEvents(Plugin plugin) {
        events.add(
                BukkitEvents.subscribe(plugin, EntityDamageByEntityEvent.class, EventPriority.LOW)
                        .filter(BukkitEventFilters.ignoreCancelled())
                        .filter(this::townyIgnoreCancelled)
                        .filter(e -> e.getEntity() instanceof LivingEntity)
                        .filter(e -> PermissionUtil.canUseEnchant(e.getEntity(), "ae.enchant.ethereal"))
                        .filter(e -> PermissionUtil.canUseEnchant(e.getEntity(), "ae.enchant.vorpal"))
                        .filter(e -> SoulsUtil.tryRemoveSouls((LivingEntity) e.getEntity(), 1))
                        .handler(e -> e.setCancelled(true))
        );
    }
}
