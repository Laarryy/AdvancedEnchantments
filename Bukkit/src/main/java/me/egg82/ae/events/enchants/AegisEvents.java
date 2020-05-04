package me.egg82.ae.events.enchants;

import java.util.Optional;
import me.egg82.ae.APIException;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.BukkitEnchantableItem;
import me.egg82.ae.api.GenericEnchantableItem;
import me.egg82.ae.events.EventHolder;
import me.egg82.ae.utils.PermissionUtil;
import ninja.egg82.events.BukkitEventFilters;
import ninja.egg82.events.BukkitEvents;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.plugin.Plugin;

public class AegisEvents extends EventHolder {
    public AegisEvents(Plugin plugin) {
        events.add(
                BukkitEvents.subscribe(plugin, EntityDamageByEntityEvent.class, EventPriority.LOW)
                        .filter(BukkitEventFilters.ignoreCancelled())
                        .filter(e -> e.getEntity() instanceof LivingEntity)
                        .filter(e -> PermissionUtil.canUseEnchant(e.getEntity(), "ae.enchant.aegis"))
                        .handler(this::damageByEntity)
        );
    }

    private void damageByEntity(EntityDamageByEntityEvent event) {
        Optional<EntityEquipment> equipment = Optional.ofNullable(((LivingEntity) event.getEntity()).getEquipment());
        if (!equipment.isPresent()) {
            return;
        }

        GenericEnchantableItem enchantableHelmet = BukkitEnchantableItem.fromItemStack(equipment.get().getHelmet());
        GenericEnchantableItem enchantableChestplate = BukkitEnchantableItem.fromItemStack(equipment.get().getChestplate());
        GenericEnchantableItem enchantableLeggings = BukkitEnchantableItem.fromItemStack(equipment.get().getLeggings());
        GenericEnchantableItem enchantableBoots = BukkitEnchantableItem.fromItemStack(equipment.get().getBoots());

        boolean hasEnchantment;
        int level;
        try {
            hasEnchantment = api.anyHasEnchantment(AdvancedEnchantment.AEGIS,
                    enchantableHelmet,
                    enchantableChestplate,
                    enchantableLeggings,
                    enchantableBoots);
            level = api.getMaxLevel(AdvancedEnchantment.AEGIS,
                    enchantableHelmet,
                    enchantableChestplate,
                    enchantableLeggings,
                    enchantableBoots
            );
        } catch (APIException ex) {
            logger.error(ex.getMessage(), ex);
            return;
        }

        if (!hasEnchantment) {
            return;
        }

        double damage = Math.max(10.0d, event.getDamage()) / 1.25d;
        damage -= damage - (damage / (level + 0.3333333333333334d));
        event.setDamage(damage);
    }
}
