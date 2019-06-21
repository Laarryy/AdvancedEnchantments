package me.egg82.ae.events.enchants.entity.entityShootBow;

import java.util.function.Consumer;
import org.bukkit.event.entity.EntityShootBowEvent;

public class EntityShootBowFreezingCancel implements Consumer<EntityShootBowEvent> {
    public EntityShootBowFreezingCancel() { }

    public void accept(EntityShootBowEvent event) {
        event.setCancelled(true);
    }
}
