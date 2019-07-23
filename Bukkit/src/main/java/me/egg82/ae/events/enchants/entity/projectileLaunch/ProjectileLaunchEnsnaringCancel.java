package me.egg82.ae.events.enchants.entity.projectileLaunch;

import java.util.function.Consumer;
import org.bukkit.event.entity.ProjectileLaunchEvent;

public class ProjectileLaunchEnsnaringCancel implements Consumer<ProjectileLaunchEvent> {
    public ProjectileLaunchEnsnaringCancel() { }

    public void accept(ProjectileLaunchEvent event) {
        event.setCancelled(true);
    }
}
