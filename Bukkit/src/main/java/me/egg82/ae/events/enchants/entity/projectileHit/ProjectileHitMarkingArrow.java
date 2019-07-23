package me.egg82.ae.events.enchants.entity.projectileHit;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import me.egg82.ae.services.CollectionProvider;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.ProjectileHitEvent;

public class ProjectileHitMarkingArrow implements Consumer<ProjectileHitEvent> {
    public ProjectileHitMarkingArrow() { }

    public void accept(ProjectileHitEvent event) {
        Optional<Entity> hitEntity = Optional.ofNullable(event.getHitEntity());

        if (hitEntity.isPresent()) {
            int level = CollectionProvider.getMarkingArrows().remove(event.getEntity().getUniqueId()) + 1;
            CollectionProvider.getMarking().put(hitEntity.get().getUniqueId(), ((double) level) * 0.3333333333333334d, 5000, TimeUnit.MILLISECONDS);
        }
    }
}
