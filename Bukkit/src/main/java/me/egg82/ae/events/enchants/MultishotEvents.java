package me.egg82.ae.events.enchants;

import java.util.Optional;
import me.egg82.ae.APIException;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.BukkitEnchantableItem;
import me.egg82.ae.events.EventHolder;
import me.egg82.ae.services.CollectionProvider;
import me.egg82.ae.services.entity.EntityItemHandler;
import me.egg82.ae.utils.ItemDurabilityUtil;
import me.egg82.ae.utils.LocationUtil;
import me.egg82.ae.utils.PermissionUtil;
import ninja.egg82.events.BukkitEventFilters;
import ninja.egg82.events.BukkitEvents;
import ninja.egg82.service.ServiceLocator;
import ninja.egg82.service.ServiceNotFoundException;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

public class MultishotEvents extends EventHolder {
    public MultishotEvents(Plugin plugin) {
        events.add(
                BukkitEvents.subscribe(plugin, EntityShootBowEvent.class, EventPriority.MONITOR)
                        .filter(BukkitEventFilters.ignoreCancelled())
                        .filter(e -> PermissionUtil.canUseEnchant(e.getEntity(), "ae.enchant.multishot"))
                        .handler(this::shoot)
        );

        events.add(
                BukkitEvents.subscribe(plugin, PlayerPickupItemEvent.class, EventPriority.LOW)
                        .filter(BukkitEventFilters.ignoreCancelled())
                        .filter(e -> e.getPlayer().getGameMode() != GameMode.CREATIVE)
                        .filter(e -> CollectionProvider.getMultishot().contains(e.getItem().getUniqueId()))
                        .handler(e -> e.setCancelled(true))
        );

        events.add(
                BukkitEvents.subscribe(plugin, ItemDespawnEvent.class, EventPriority.MONITOR)
                        .filter(BukkitEventFilters.ignoreCancelled())
                        .handler(e -> CollectionProvider.getMultishot().remove(e.getEntity().getUniqueId()))
        );
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
        BukkitEnchantableItem enchantableMainHand = mainHand.isPresent() ? BukkitEnchantableItem.fromItemStack(mainHand.get()) : null;

        boolean hasEnchantment;
        int level;
        boolean hasFiery;
        try {
            hasEnchantment = api.anyHasEnchantment(AdvancedEnchantment.MULTISHOT, enchantableMainHand);
            level = api.getMaxLevel(AdvancedEnchantment.MULTISHOT, enchantableMainHand);
            hasFiery = api.anyHasEnchantment(AdvancedEnchantment.FIERY, enchantableMainHand); // Fiery compatibility
        } catch (APIException ex) {
            logger.error(ex.getMessage(), ex);
            return;
        }

        if (!hasEnchantment || level <= 0) {
            return;
        }

        Vector velocity = event.getProjectile().getVelocity();
        double speed = velocity.length();
        Vector direction = new Vector(velocity.getX() / speed, velocity.getY() / speed, velocity.getZ() / speed);

        // Higher = less "spray"
        // Lower = more "spray"
        double spray = 10.5d;

        Location eyeLocation = event.getEntity().getEyeLocation();
        boolean isProjectile = event.getProjectile() instanceof Projectile;

        for (int i = 0; i < level * 2; i++) {
            Entity p = eyeLocation.getWorld().spawn(LocationUtil.getLocationInFront(eyeLocation, 1.0d, false), event.getProjectile().getClass());
            CollectionProvider.getMultishot().add(p.getUniqueId());
            p.setVelocity(
                    new Vector(
                            direction.getX() + (Math.random() - 0.5) / spray,
                            direction.getY() + (Math.random() - 0.5) / spray,
                            direction.getZ() + (Math.random() - 0.5) / spray
                    ).normalize().multiply(speed)
            );
            if (isProjectile) {
                ((Projectile) p).setShooter(event.getEntity());
            }

            if (hasFiery) {
                CollectionProvider.getFiery().add(p.getUniqueId()); // Fiery compatibility
            }
        }

        if (!(event.getEntity() instanceof Player) || ((Player) event.getEntity()).getGameMode() != GameMode.CREATIVE) {
            if (!ItemDurabilityUtil.removeDurability(event.getEntity() instanceof Player ? (Player) event.getEntity() : null, enchantableMainHand, level * 2, event.getEntity().getLocation())) {
                entityItemHandler.setItemInMainHand(event.getEntity(), null);
            }
        }
    }
}
