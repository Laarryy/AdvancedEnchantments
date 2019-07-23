package me.egg82.ae.events;

import java.util.ArrayList;
import java.util.List;
import me.egg82.ae.hooks.TownyHook;
import ninja.egg82.events.BukkitEventSubscriber;
import ninja.egg82.service.ServiceLocator;
import ninja.egg82.service.ServiceNotFoundException;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public abstract class EventHolder {
    protected final List<BukkitEventSubscriber<?>> events = new ArrayList<>();

    public int numEvents() { return events.size(); }

    public void cancel() {
        for (BukkitEventSubscriber<?> event : events) {
            event.cancel();
        }
    }

    protected boolean canUseEnchant(Object obj, String node) { return !(obj instanceof Player) || ((Player) obj).hasPermission(node); }

    protected boolean townyIgnoreCancelled(EntityDamageByEntityEvent event) {
        try {
            TownyHook townyHook = ServiceLocator.get(TownyHook.class);
            return townyHook.ignoreCancelled(event);
        } catch (InstantiationException | IllegalAccessException | ServiceNotFoundException ignored) { return true; }
    }
}
