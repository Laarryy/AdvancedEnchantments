package me.egg82.ae.events.enchants.entity.entityDamageByEntity;

import java.util.function.Consumer;
import me.egg82.ae.services.CollectionProvider;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class EntityDamageByEntityMarking implements Consumer<EntityDamageByEntityEvent> {
    public EntityDamageByEntityMarking() { }

    public void accept(EntityDamageByEntityEvent event) {
        Double d = CollectionProvider.getMarking().get(event.getEntity().getUniqueId());
        if (d == null) {
            return;
        }

        event.setDamage(event.getDamage() * d);
    }
}
