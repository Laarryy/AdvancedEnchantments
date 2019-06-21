package me.egg82.ae.events.enchants.entity.entityDamageByEntity;

import java.util.function.Consumer;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class EntityDamageByEntityFreezingCancel implements Consumer<EntityDamageByEntityEvent> {
    public EntityDamageByEntityFreezingCancel() { }

    public void accept(EntityDamageByEntityEvent event) {
        event.setCancelled(true);
    }
}
