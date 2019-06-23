package me.egg82.ae.tasks;

import java.util.List;
import java.util.Optional;
import me.egg82.ae.APIException;
import me.egg82.ae.EnchantAPI;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.BukkitEnchantableItem;
import me.egg82.ae.api.GenericEnchantableItem;
import me.egg82.ae.services.entity.EntityDamageHandler;
import me.egg82.ae.services.entity.EntityItemHandler;
import me.egg82.ae.utils.LocationUtil;
import ninja.egg82.service.ServiceLocator;
import ninja.egg82.service.ServiceNotFoundException;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskCalling implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private EnchantAPI api = EnchantAPI.getInstance();

    private EntityItemHandler entityItemHandler;

    public TaskCalling() {
        try {
            entityItemHandler = ServiceLocator.get(EntityItemHandler.class);
        } catch (InstantiationException | IllegalAccessException | ServiceNotFoundException ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Optional<EntityEquipment> equipment = Optional.ofNullable(player.getEquipment());
            if (!equipment.isPresent()) {
                return;
            }

            Optional<ItemStack> mainHand = entityItemHandler.getItemInMainHand(player);
            Optional<ItemStack> offHand = entityItemHandler.getItemInOffHand(player);
            GenericEnchantableItem enchantableMainHand = mainHand.isPresent() ? BukkitEnchantableItem.fromItemStack(mainHand.get()) : null;
            GenericEnchantableItem enchantableOffHand = offHand.isPresent() ? BukkitEnchantableItem.fromItemStack(offHand.get()) : null;

            GenericEnchantableItem enchantableHelmet = BukkitEnchantableItem.fromItemStack(equipment.get().getHelmet());
            GenericEnchantableItem enchantableChestplate = BukkitEnchantableItem.fromItemStack(equipment.get().getChestplate());
            GenericEnchantableItem enchantableLeggings = BukkitEnchantableItem.fromItemStack(equipment.get().getLeggings());
            GenericEnchantableItem enchantableBoots = BukkitEnchantableItem.fromItemStack(equipment.get().getBoots());

            boolean hasEnchantment;
            int level;
            try {
                hasEnchantment = api.anyHasEnchantment(AdvancedEnchantment.CALLING_CURSE,
                        enchantableMainHand,
                        enchantableOffHand,
                        enchantableHelmet,
                        enchantableChestplate,
                        enchantableLeggings,
                        enchantableBoots);
                level = api.getMaxLevel(AdvancedEnchantment.CALLING_CURSE,
                        enchantableMainHand,
                        enchantableOffHand,
                        enchantableHelmet,
                        enchantableChestplate,
                        enchantableLeggings,
                        enchantableBoots);
            } catch (APIException ex) {
                logger.error(ex.getMessage(), ex);
                return;
            }

            if (!hasEnchantment || level <= 0) {
                return;
            }

            World playerWorld = player.getWorld();
            Vector playerVector = player.getLocation().toVector();

            for (Entity e : player.getNearbyEntities(10.0d * level, 10.0d * level, 10.0d * level)) {
                if (e instanceof Monster && e.getWorld().equals(playerWorld)) {
                    if (e.getType() == EntityType.PIG_ZOMBIE) {
                        PigZombie pig = (PigZombie) e;
                        pig.setAngry(true);
                    }

                    ((Monster) e).setTarget(player);
                    Vector v = playerVector.clone().subtract(e.getLocation().toVector()).normalize().multiply(0.23d);
                    if (LocationUtil.isFinite(v)) {
                        e.setVelocity(v);
                    }
                }
            }
        }
    }
}
