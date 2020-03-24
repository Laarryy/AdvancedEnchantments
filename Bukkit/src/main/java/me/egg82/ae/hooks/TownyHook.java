package me.egg82.ae.hooks;

import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.utils.CombatUtil;
import ninja.egg82.events.BukkitEvents;
import ninja.egg82.service.ServiceLocator;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

public class TownyHook implements PluginHook {
    private Towny plugin;

    public static void create(Plugin plugin, Plugin towny) {
        if (!towny.isEnabled()) {
            BukkitEvents.subscribe(plugin, PluginEnableEvent.class, EventPriority.MONITOR)
                    .expireIf(e -> e.getPlugin().getName().equals("Towny"))
                    .filter(e -> e.getPlugin().getName().equals("Towny"))
                    .handler(e -> ServiceLocator.register(new TownyHook(towny)));
            return;
        }
        ServiceLocator.register(new TownyHook(towny));
    }

    public TownyHook(Plugin plugin) { this.plugin = (Towny) plugin; }

    public void cancel() { }

    public boolean ignoreCancelled(EntityDamageByEntityEvent event) { return !CombatUtil.preventDamageCall(plugin, event.getDamager(), event.getEntity()); }
}
