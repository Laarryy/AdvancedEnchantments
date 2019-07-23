package me.egg82.ae.events.enchants.entity.entityDamageByEntity;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import me.egg82.ae.services.CollectionProvider;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class EntityDamageByEntityMarkingArrow implements Consumer<EntityDamageByEntityEvent> {
    public EntityDamageByEntityMarkingArrow() { }

    public void accept(EntityDamageByEntityEvent event) {
        int level = CollectionProvider.getMarkingArrows().remove(event.getDamager().getUniqueId()) + 1;
        CollectionProvider.getMarking().put(event.getEntity().getUniqueId(), ((double) level) * 0.3333333333333334d, 5000, TimeUnit.MILLISECONDS);
    }
}
