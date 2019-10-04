package me.egg82.ae.events.enchants;

import java.util.Optional;
import me.egg82.ae.APIException;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.BukkitEnchantableItem;
import me.egg82.ae.api.GenericEnchantableItem;
import me.egg82.ae.events.EventHolder;
import me.egg82.ae.utils.LocationUtil;
import me.egg82.ae.utils.PermissionUtil;
import ninja.egg82.events.BukkitEventFilters;
import ninja.egg82.events.BukkitEvents;
import org.bukkit.Material;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.plugin.Plugin;

public class TrailblazerEvents extends EventHolder {
    public TrailblazerEvents(Plugin plugin) {
        events.add(
                BukkitEvents.subscribe(plugin, PlayerMoveEvent.class, EventPriority.NORMAL)
                        .filter(BukkitEventFilters.ignoreCancelled())
                        .filter(e -> !LocationUtil.isBlockEqual(e.getFrom(), e.getTo()))
                        .filter(e -> PermissionUtil.canUseEnchant(e.getPlayer(), "ae.enchant.trailblazer"))
                        .filter(e -> LocationUtil.canIgnite(e.getFrom().getBlock().getType()))
                        .handler(this::move)
        );
    }

    private void move(PlayerMoveEvent event) {
        Optional<EntityEquipment> equipment = Optional.ofNullable(event.getPlayer().getEquipment());
        if (!equipment.isPresent()) {
            return;
        }

        GenericEnchantableItem enchantableBoots = BukkitEnchantableItem.fromItemStack(equipment.get().getBoots());

        boolean hasEnchantment;
        try {
            hasEnchantment = api.anyHasEnchantment(AdvancedEnchantment.TRAILBLAZER, enchantableBoots);
        } catch (APIException ex) {
            logger.error(ex.getMessage(), ex);
            return;
        }

        if (!hasEnchantment) {
            return;
        }

        event.getFrom().getBlock().setType(Material.FIRE, true);
        if (event.getPlayer().isSprinting()) {
            event.getPlayer().setFireTicks(60);
        }
    }
}
