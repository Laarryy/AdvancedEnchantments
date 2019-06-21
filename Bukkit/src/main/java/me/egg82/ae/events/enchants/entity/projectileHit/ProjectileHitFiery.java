package me.egg82.ae.events.enchants.entity.projectileHit;

import java.util.Optional;
import java.util.function.Consumer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.ProjectileHitEvent;

public class ProjectileHitFiery implements Consumer<ProjectileHitEvent> {
    public ProjectileHitFiery() { }

    public void accept(ProjectileHitEvent event) {
        Optional<Block> hitBlock = Optional.ofNullable(event.getHitBlock());
        Optional<Entity> hitEntity = Optional.ofNullable(event.getHitEntity());

        if (hitBlock.isPresent()) {
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        // Need a new loc each time so we don't screw up the math
                        Location l2 = hitBlock.get().getLocation().clone().add(x, y, z);
                        Material type = l2.getBlock().getType();
                        if (!type.isSolid() && !type.name().contains("WATER") && !type.name().contains("LAVA")) {
                            l2.getBlock().setType(Material.FIRE, true);
                        }
                    }
                }
            }
        }

        if (hitEntity.isPresent()) {
            hitEntity.get().setFireTicks(50);
        }
    }
}
