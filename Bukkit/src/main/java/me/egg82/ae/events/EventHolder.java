package me.egg82.ae.events;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import me.egg82.ae.EnchantAPI;
import me.egg82.ae.hooks.TownyHook;
import me.egg82.ae.hooks.WorldGuardHook;
import ninja.egg82.events.BukkitEventSubscriber;
import ninja.egg82.service.ServiceLocator;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class EventHolder {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected final List<BukkitEventSubscriber<?>> events = new ArrayList<>();

    protected final EnchantAPI api = EnchantAPI.getInstance();

    public final int numEvents() { return events.size(); }

    public final void cancel() {
        for (BukkitEventSubscriber<?> event : events) {
            event.cancel();
        }
    }

    protected final boolean compatIgnoreCancelled(EntityDamageByEntityEvent event) {
        try {
            Optional<TownyHook> townyHook = ServiceLocator.getOptional(TownyHook.class);
            Optional<WorldGuardHook> worldGuardHook = ServiceLocator.getOptional(WorldGuardHook.class);
            return
                    (!townyHook.isPresent() || townyHook.get().ignoreCancelled(event))
                    && (!worldGuardHook.isPresent() || worldGuardHook.get().ignoreCancelled(event))
            ;
        } catch (InstantiationException | IllegalAccessException ignored) { return true; }
    }
}
