package me.egg82.ae.events.curses;

import java.util.Optional;
import me.egg82.ae.APIException;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.BukkitEnchantableItem;
import me.egg82.ae.api.GenericEnchantableItem;
import me.egg82.ae.events.EventHolder;
import me.egg82.ae.services.entity.EntityDamageHandler;
import me.egg82.ae.utils.PermissionUtil;
import ninja.egg82.events.BukkitEventFilters;
import ninja.egg82.events.BukkitEvents;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.plugin.Plugin;

public class EmpathyEvents extends EventHolder {
    public EmpathyEvents(Plugin plugin) {
        events.add(
                BukkitEvents.subscribe(plugin, EntityDamageByEntityEvent.class, EventPriority.MONITOR)
                        .filter(BukkitEventFilters.ignoreCancelled())
                        .handler(this::damageNearby)
        );
    }

    private void damageNearby(EntityDamageByEntityEvent event) {
        for (Entity e : event.getEntity().getNearbyEntities(15.0d, 15.0d, 15.0d)) {
            if (e.getUniqueId().equals(event.getEntity().getUniqueId()) || !(e instanceof LivingEntity)) {
                continue;
            }

            if (!PermissionUtil.canUseEnchant(e, "ae.curse.empathy")) {
                continue;
            }

            Optional<EntityEquipment> equipment = Optional.ofNullable(((LivingEntity) e).getEquipment());
            if (!equipment.isPresent()) {
                continue;
            }

            GenericEnchantableItem enchantableChestplate = BukkitEnchantableItem.fromItemStack(equipment.get().getChestplate());

            boolean hasEnchantment;
            int level;
            try {
                hasEnchantment = api.anyHasEnchantment(AdvancedEnchantment.EMPATHY_CURSE, enchantableChestplate);
                level = api.getMaxLevel(AdvancedEnchantment.EMPATHY_CURSE, enchantableChestplate);
            } catch (APIException ex) {
                logger.error(ex.getMessage(), ex);
                continue;
            }

            if (!hasEnchantment) {
                continue;
            }

            double damage = event.getDamage();
            damage = damage - (damage / (level + 0.3333333333333334d));

            EntityDamageHandler.damage(event.getDamager(), (Damageable) e, damage, event.getCause());
        }
    }
}
