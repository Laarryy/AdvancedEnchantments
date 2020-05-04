package me.egg82.ae.events.curses;

import me.egg82.ae.APIException;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.BukkitEnchantableItem;
import me.egg82.ae.events.EventHolder;
import me.egg82.ae.utils.PermissionUtil;
import ninja.egg82.events.BukkitEventFilters;
import ninja.egg82.events.BukkitEvents;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.plugin.Plugin;

public class FragilityEvents extends EventHolder {
    public FragilityEvents(Plugin plugin) {
        events.add(
                BukkitEvents.subscribe(plugin, EntityDamageByEntityEvent.class, EventPriority.LOW)
                        .filter(BukkitEventFilters.ignoreCancelled())
                        .filter(e -> e.getEntity() instanceof LivingEntity)
                        .filter(e -> ((LivingEntity) e.getEntity()).getEquipment() != null)
                        .filter(e -> PermissionUtil.canUseEnchant(e.getEntity(), "ae.curse.fragility"))
                        .handler(this::damage)
        );
    }

    private void damage(EntityDamageByEntityEvent event) {
        EntityEquipment equipment = ((LivingEntity) event.getEntity()).getEquipment();

        boolean hasEnchantment;
        int level;
        try {
            hasEnchantment = api.anyHasEnchantment(AdvancedEnchantment.FRAGILITY_CURSE,
                    BukkitEnchantableItem.fromItemStack(equipment.getHelmet()),
                    BukkitEnchantableItem.fromItemStack(equipment.getChestplate()),
                    BukkitEnchantableItem.fromItemStack(equipment.getLeggings()),
                    BukkitEnchantableItem.fromItemStack(equipment.getBoots())
            );
            level = api.getMaxLevel(AdvancedEnchantment.FRAGILITY_CURSE,
                    BukkitEnchantableItem.fromItemStack(equipment.getHelmet()),
                    BukkitEnchantableItem.fromItemStack(equipment.getChestplate()),
                    BukkitEnchantableItem.fromItemStack(equipment.getLeggings()),
                    BukkitEnchantableItem.fromItemStack(equipment.getBoots())
            );
        } catch (APIException ex) {
            logger.error(ex.getMessage(), ex);
            return;
        }

        if (!hasEnchantment) {
            return;
        }

        double damage = event.getDamage();
        damage += damage - (damage / (level + 0.3333333333333334d));
        event.setDamage(damage);
    }
}
