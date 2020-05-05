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
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

public class BurstEvents extends EventHolder {
    private final Plugin plugin;

    public BurstEvents(Plugin plugin) {
        this.plugin = plugin;

        events.add(
                BukkitEvents.subscribe(plugin, EntityShootBowEvent.class, EventPriority.MONITOR)
                        .filter(BukkitEventFilters.ignoreCancelled())
                        .filter(e -> PermissionUtil.canUseEnchant(e.getEntity(), "ae.enchant.burst"))
                        .handler(this::shoot)
        );
    }

    private void shoot(EntityShootBowEvent event) {
        EntityItemHandler entityItemHandler = getItemHandler();
        if (entityItemHandler == null) {
            return;
        }

        Optional<ItemStack> mainHand = entityItemHandler.getItemInMainHand(event.getEntity());
        BukkitEnchantableItem enchantableMainHand = mainHand.isPresent() ? BukkitEnchantableItem.fromItemStack(mainHand.get()) : null;

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
        boolean isProjectile = event.getProjectile() instanceof Projectile;

        for (int i = 0; i < level; i++) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Entity p = eyeLocation.getWorld().spawn(LocationUtil.getLocationInFront(eyeLocation, 1.0d, false), event.getProjectile().getClass());
                CollectionProvider.getMultishot().add(p.getUniqueId());
                p.setVelocity(velocity);
                if (isProjectile) {
                    ((Projectile) p).setShooter(event.getEntity());
                }

                if (hasFiery) {
                    CollectionProvider.getFiery().add(p.getUniqueId()); // Fiery compatibility
                }
            }, 5L * i);
        }

        if (!(event.getEntity() instanceof Player) || ((Player) event.getEntity()).getGameMode() != GameMode.CREATIVE) {
            if (!ItemDurabilityUtil.removeDurability(event.getEntity() instanceof Player ? (Player) event.getEntity() : null, enchantableMainHand, level, event.getEntity().getLocation())) {
                entityItemHandler.setItemInMainHand(event.getEntity(), null);
            }
        }
    }
}
