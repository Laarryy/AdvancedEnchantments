package me.egg82.ae.events.enchants;

import co.aikar.commands.CommandManager;
import java.util.Comparator;
import java.util.List;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.enums.Message;
import me.egg82.ae.events.EventHolder;
import me.egg82.ae.services.CollectionProvider;
import me.egg82.ae.utils.ConfigUtil;
import me.egg82.ae.utils.SoulsUtil;
import ninja.egg82.events.BukkitEvents;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.Plugin;

public class VacuumEvents extends EventHolder {
    private final CommandManager commandManager;

    public VacuumEvents(Plugin plugin, CommandManager commandManager) {
        this.commandManager = commandManager;

        events.add(
                BukkitEvents.subscribe(plugin, EntityDeathEvent.class, EventPriority.MONITOR)
                        .filter(e -> CollectionProvider.getSouls().add(e.getEntity().getUniqueId())) // Should be the last filter
                        .handler(this::death)
        );
    }

    private void death(EntityDeathEvent event) {
        Location deathLocation = event.getEntity().getLocation();
        List<LivingEntity> withEnchant = getEntitiesWithArmorEnchant(event.getEntity().getWorld(), AdvancedEnchantment.VACUUM, new String[] { "ae.enchant.vacuum", "ae.enchant.vorpal" });
        // Sort by distance to death event
        withEnchant.sort(Comparator.comparingDouble(o -> o.getLocation().distanceSquared(deathLocation)));

        for (LivingEntity e : withEnchant) {
            double level = getLevelForArmor(e, AdvancedEnchantment.VACUUM);
            if (e.getLocation().distanceSquared(deathLocation) > (level * 3.25d) * (level * 3.25d)) {
                continue;
            }
            if (SoulsUtil.tryAddSouls(e, 1)) {
                return;
            }
        }

        CollectionProvider.getSouls().remove(event.getEntity().getUniqueId());

        if (ConfigUtil.getDebugOrFalse()) {
            commandManager.getCommandIssuer(Bukkit.getConsoleSender()).sendError(Message.PLAYER__SOUL_VANISHED);
        }
    }
}
