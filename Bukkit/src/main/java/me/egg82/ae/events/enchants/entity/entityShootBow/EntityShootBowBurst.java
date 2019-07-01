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
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityShootBowBurst implements Consumer<EntityShootBowEvent> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private EnchantAPI api = EnchantAPI.getInstance();

    private final Plugin plugin;

    public EntityShootBowBurst(Plugin plugin) {
        this.plugin = plugin;
    }

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

        boolean hasEnchantment;
        int level;
        boolean hasFiery;
        try {
            hasEnchantment = api.anyHasEnchantment(AdvancedEnchantment.BURST, enchantableMainHand);
            level = api.getMaxLevel(AdvancedEnchantment.BURST, enchantableMainHand);
            hasFiery = api.anyHasEnchantment(AdvancedEnchantment.FIERY, enchantableMainHand); // Fiery compatibility
        } catch (APIException ex) {
            logger.error(ex.getMessage(), ex);
            return;
        }

        if (!hasEnchantment || level <= 0) {
            return;
        }

        Vector velocity = event.getProjectile().getVelocity();
        Location eyeLocation = event.getEntity().getEyeLocation();

        for (int i = 0; i < level; i++) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Entity p = eyeLocation.getWorld().spawn(LocationUtil.getLocationInFront(eyeLocation, 1.0d, false), event.getProjectile().getClass());
                p.setVelocity(velocity);

                if (hasFiery) {
                    CollectionProvider.getFiery().add(p.getUniqueId()); // Fiery compatibility
                }
            }, 5L * i);
        }

        if (!(event.getEntity() instanceof Player) || ((Player) event.getEntity()).getGameMode() != GameMode.CREATIVE) {
            if (!ItemDurabilityUtil.removeDurability(mainHand.get(), level, event.getEntity().getLocation())) {
                entityItemHandler.setItemInMainHand(event.getEntity(), null);
            }
        }
    }
}
