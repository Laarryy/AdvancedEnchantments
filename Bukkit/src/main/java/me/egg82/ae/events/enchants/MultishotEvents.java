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
import ninja.egg82.events.BukkitEventFilters;
import ninja.egg82.events.BukkitEvents;
import ninja.egg82.service.ServiceLocator;
import ninja.egg82.service.ServiceNotFoundException;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

public class MultishotEvents extends EventHolder {
    public MultishotEvents(Plugin plugin) {
        events.add(
                BukkitEvents.subscribe(plugin, EntityShootBowEvent.class, EventPriority.NORMAL)
                        .filter(BukkitEventFilters.ignoreCancelled())
                        .filter(e -> canUseEnchant(e.getEntity(), "ae.enchant.multishot"))
                        .handler(this::shoot)
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

        for (int i = 0; i < level * 2; i++) {
            Entity p = eyeLocation.getWorld().spawn(LocationUtil.getLocationInFront(eyeLocation, 1.0d, false), event.getProjectile().getClass());
            p.setVelocity(
                    new Vector(
                            direction.getX() + (Math.random() - 0.5) / spray,
                            direction.getY() + (Math.random() - 0.5) / spray,
                            direction.getZ() + (Math.random() - 0.5) / spray
                    ).normalize().multiply(speed)
            );

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
