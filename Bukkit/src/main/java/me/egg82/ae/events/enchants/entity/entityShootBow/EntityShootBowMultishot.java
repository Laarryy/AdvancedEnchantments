package me.egg82.ae.events.enchants.entity.entityShootBow;

import java.util.Optional;
import java.util.function.Consumer;
import me.egg82.ae.APIException;
import me.egg82.ae.EnchantAPI;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.BukkitEnchantableItem;
import me.egg82.ae.api.GenericEnchantableItem;
import me.egg82.ae.services.CollectionProvider;
import me.egg82.ae.services.entity.EntityItemHandler;
import me.egg82.ae.utils.ItemDurabilityUtil;
import me.egg82.ae.utils.LocationUtil;
import ninja.egg82.service.ServiceLocator;
import ninja.egg82.service.ServiceNotFoundException;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityShootBowMultishot implements Consumer<EntityShootBowEvent> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private EnchantAPI api = EnchantAPI.getInstance();

    public EntityShootBowMultishot() { }

    public void accept(EntityShootBowEvent event) {
        EntityItemHandler entityItemHandler;
        try {
            entityItemHandler = ServiceLocator.get(EntityItemHandler.class);
        } catch (InstantiationException | IllegalAccessException | ServiceNotFoundException ex) {
            logger.error(ex.getMessage(), ex);
            return;
        }

        Optional<ItemStack> mainHand = entityItemHandler.getItemInMainHand(event.getEntity());
        GenericEnchantableItem enchantableMainHand = mainHand.isPresent() ? BukkitEnchantableItem.fromItemStack(mainHand.get()) : null;

        int level;
        boolean hasFiery;
        try {
            level = api.getMaxLevel(AdvancedEnchantment.MULTISHOT, enchantableMainHand);
            hasFiery = api.anyHasEnchantment(AdvancedEnchantment.FIERY, enchantableMainHand); // Fiery compatibility
        } catch (APIException ex) {
            logger.error(ex.getMessage(), ex);
            return;
        }

        if (level < 0) {
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
            Entity p = eyeLocation.getWorld().spawn(LocationUtil.getLocationInFront(eyeLocation, 1.0d, true), event.getProjectile().getClass());
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
            if (!ItemDurabilityUtil.removeDurability(mainHand.get(), level * 2, event.getEntity().getLocation())) {
                entityItemHandler.setItemInMainHand(event.getEntity(), null);
            }
        }
    }
}
