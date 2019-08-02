package me.egg82.ae.tasks;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import me.egg82.ae.services.CollectionProvider;
import me.egg82.ae.services.entity.EntityDamageHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageEvent;

public class TaskBleeding implements Runnable {
    public TaskBleeding() { }

    public void run() {
        for (Iterator<Map.Entry<UUID, Double>> i = CollectionProvider.getBleeding().entrySet().iterator(); i.hasNext();) {
            Map.Entry<UUID, Double> kvp = i.next();
            Entity e = Bukkit.getEntity(kvp.getKey());
            if (e == null || e.isDead() || !(e instanceof Damageable)) {
                i.remove();
                continue;
            }
            EntityDamageHandler.damage((Damageable) e, kvp.getValue(), EntityDamageEvent.DamageCause.WITHER);
        }
    }
}
