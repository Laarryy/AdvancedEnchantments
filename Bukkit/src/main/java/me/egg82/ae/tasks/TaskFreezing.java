package me.egg82.ae.tasks;

import java.util.Map;
import java.util.UUID;
import me.egg82.ae.services.CollectionProvider;
import me.egg82.ae.services.entity.EntityDamageHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageEvent;

public class TaskFreezing implements Runnable {
    public TaskFreezing() { }

    public void run() {
        for (Map.Entry<UUID, Double> kvp : CollectionProvider.getFreezing().entrySet()) {
            Entity e = Bukkit.getEntity(kvp.getKey());
            if (e == null || e.isDead() || !(e instanceof Damageable)) {
                CollectionProvider.getFreezing().remove(kvp.getKey());
                continue;
            }
            EntityDamageHandler.damage((Damageable) e, kvp.getValue(), EntityDamageEvent.DamageCause.WITHER);
        }
    }
}
