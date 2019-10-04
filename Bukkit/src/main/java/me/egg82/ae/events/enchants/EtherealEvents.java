package me.egg82.ae.events.enchants;

import java.util.Optional;
import me.egg82.ae.APIException;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.BukkitEnchantableItem;
import me.egg82.ae.api.GenericEnchantableItem;
import me.egg82.ae.events.EventHolder;
import me.egg82.ae.utils.PermissionUtil;
import me.egg82.ae.utils.SoulsUtil;
import ninja.egg82.events.BukkitEventFilters;
import ninja.egg82.events.BukkitEvents;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.plugin.Plugin;

public class EtherealEvents extends EventHolder {
    public EtherealEvents(Plugin plugin) {
        events.add(
                BukkitEvents.subscribe(plugin, EntityDamageByEntityEvent.class, EventPriority.LOW)
                        .filter(BukkitEventFilters.ignoreCancelled())
                        .filter(this::townyIgnoreCancelled)
                        .filter(e -> e.getEntity() instanceof LivingEntity)
                        .filter(e -> PermissionUtil.canUseEnchant(e.getEntity(), "ae.enchant.ethereal"))
                        .filter(e -> PermissionUtil.canUseEnchant(e.getEntity(), "ae.enchant.vorpal"))
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
        try {
            hasEnchantment = api.anyHasEnchantment(AdvancedEnchantment.ETHEREAL,
                    enchantableHelmet,
                    enchantableChestplate,
                    enchantableLeggings,
                    enchantableBoots);
        } catch (APIException ex) {
            logger.error(ex.getMessage(), ex);
            return;
        }

        if (!hasEnchantment) {
            return;
        }

        if (!SoulsUtil.tryRemoveSouls((LivingEntity) event.getEntity(), 1)) {
            return;
        }

        event.setCancelled(true);
    }
}
