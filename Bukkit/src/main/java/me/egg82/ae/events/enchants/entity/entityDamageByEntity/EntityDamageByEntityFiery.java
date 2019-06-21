package me.egg82.ae.events.enchants.entity.entityDamageByEntity;

import java.util.function.Consumer;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class EntityDamageByEntityFiery implements Consumer<EntityDamageByEntityEvent> {
    public EntityDamageByEntityFiery() { }

    public void accept(EntityDamageByEntityEvent event) {
        event.getEntity().setFireTicks(50);
    }
}
