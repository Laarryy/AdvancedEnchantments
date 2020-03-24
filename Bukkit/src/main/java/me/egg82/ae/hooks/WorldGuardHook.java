package me.egg82.ae.hooks;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import ninja.egg82.events.BukkitEvents;
import ninja.egg82.service.ServiceLocator;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

public class WorldGuardHook implements PluginHook {
    public static void create(Plugin plugin, Plugin worldGuard) {
        if (!worldGuard.isEnabled()) {
            BukkitEvents.subscribe(plugin, PluginEnableEvent.class, EventPriority.MONITOR)
                    .expireIf(e -> e.getPlugin().getName().equals("WorldGuard"))
                    .filter(e -> e.getPlugin().getName().equals("WorldGuard"))
                    .handler(e -> ServiceLocator.register(new WorldGuardHook()));
            return;
        }
        ServiceLocator.register(new WorldGuardHook());
    }

    public WorldGuardHook() { }

    public void cancel() { }

    public boolean ignoreCancelled(EntityDamageByEntityEvent event) { return WorldGuardPlugin.inst().createProtectionQuery().testEntityDamage(event.getCause(), event.getEntity()); }
}
