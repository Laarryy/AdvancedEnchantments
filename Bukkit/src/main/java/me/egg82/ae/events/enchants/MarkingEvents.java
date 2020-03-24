package me.egg82.ae.events.enchants;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import me.egg82.ae.APIException;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.BukkitEnchantableItem;
import me.egg82.ae.api.GenericEnchantableItem;
import me.egg82.ae.events.EventHolder;
import me.egg82.ae.services.CollectionProvider;
import me.egg82.ae.services.entity.EntityItemHandler;
import me.egg82.ae.utils.PermissionUtil;
import ninja.egg82.events.BukkitEventFilters;
import ninja.egg82.events.BukkitEvents;
import ninja.egg82.service.ServiceLocator;
import ninja.egg82.service.ServiceNotFoundException;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class MarkingEvents extends EventHolder {
    public MarkingEvents(Plugin plugin) {
        events.add(
                BukkitEvents.subscribe(plugin, EntityDamageByEntityEvent.class, EventPriority.NORMAL)
                        .filter(BukkitEventFilters.ignoreCancelled())
                        .filter(this::compatIgnoreCancelled)
                        .handler(this::damageIncrease)
        );
        events.add(
                BukkitEvents.subscribe(plugin, EntityShootBowEvent.class, EventPriority.NORMAL)
                        .filter(BukkitEventFilters.ignoreCancelled())
                        .filter(e -> PermissionUtil.canUseEnchant(e.getEntity(), "ae.enchant.marking"))
                        .handler(this::shoot)
        );
        try {
            Class.forName("org.bukkit.event.entity.ProjectileHitEvent");
            events.add(
                    BukkitEvents.subscribe(plugin, ProjectileHitEvent.class, EventPriority.NORMAL)
                            .filter(e -> CollectionProvider.getMarkingArrows().containsKey(e.getEntity().getUniqueId()))
                            .handler(this::hit)
            );
        } catch (ClassNotFoundException ignored) {}
        events.add(
                BukkitEvents.subscribe(plugin, EntityDamageByEntityEvent.class, EventPriority.NORMAL)
                        .filter(e -> CollectionProvider.getMarkingArrows().containsKey(e.getDamager().getUniqueId()))
                        .handler(this::damageMark)
        );
    }

    private void damageIncrease(EntityDamageByEntityEvent event) {
        Double d = CollectionProvider.getMarking().get(event.getEntity().getUniqueId());
        if (d == null) {
            return;
        }

        event.setDamage(event.getDamage() * d);
    }

    private void shoot(EntityShootBowEvent event) {
        EntityItemHandler entityItemHandler;
        try {
            entityItemHandler = ServiceLocator.get(EntityItemHandler.class);
        } catch (InstantiationException | IllegalAccessException | ServiceNotFoundException ex) {
            logger.error(ex.getMessage(), ex);
            return;
        }

        Optional<ItemStack> mainHand = entityItemHandler.getItemInMainHand(event.getEntity());
        GenericEnchantableItem enchantableMainHand = mainHand.isPresent() ? BukkitEnchantableItem.fromItemStack(mainHand.get()) : null;

        boolean hasEnchantment;
        int level;
        try {
            hasEnchantment = api.anyHasEnchantment(AdvancedEnchantment.MARKING, enchantableMainHand);
            level = api.getMaxLevel(AdvancedEnchantment.MARKING, enchantableMainHand);
        } catch (APIException ex) {
            logger.error(ex.getMessage(), ex);
            return;
        }

        if (!hasEnchantment) {
            return;
        }

        CollectionProvider.getMarkingArrows().put(event.getProjectile().getUniqueId(), level);
    }

    private void hit(ProjectileHitEvent event) {
        Optional<Entity> hitEntity = Optional.ofNullable(event.getHitEntity());

        if (hitEntity.isPresent()) {
            int level = CollectionProvider.getMarkingArrows().remove(event.getEntity().getUniqueId()) + 1;
            CollectionProvider.getMarking().put(hitEntity.get().getUniqueId(), ((double) level) * 0.3333333333333334d, 5000L, TimeUnit.MILLISECONDS);
        }
    }

    private void damageMark(EntityDamageByEntityEvent event) {
        int level = CollectionProvider.getMarkingArrows().remove(event.getDamager().getUniqueId()) + 1;
        CollectionProvider.getMarking().put(event.getEntity().getUniqueId(), ((double) level) * 0.3333333333333334d, 5000L, TimeUnit.MILLISECONDS);
    }
}
