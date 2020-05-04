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
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class FreezingEvents extends EventHolder {
    public FreezingEvents(Plugin plugin) {
        events.add(
                BukkitEvents.subscribe(plugin, EntityDamageByEntityEvent.class, EventPriority.MONITOR)
                        .filter(BukkitEventFilters.ignoreCancelled())
                        .filter(e -> e.getDamager() instanceof LivingEntity)
                        .filter(e -> PermissionUtil.canUseEnchant(e.getDamager(), "ae.enchant.freezing"))
                        .handler(this::damage)
        );
        events.add(
                BukkitEvents.subscribe(plugin, EntityDamageByEntityEvent.class, EventPriority.LOW)
                        .filter(BukkitEventFilters.ignoreCancelled())
                        .filter(e -> CollectionProvider.getFreezing().containsKey(e.getDamager().getUniqueId()))
                        .handler(e -> e.setCancelled(true))
        );
        events.add(
                BukkitEvents.subscribe(plugin, PlayerInteractEvent.class, EventPriority.LOW)
                        .filter(BukkitEventFilters.ignoreCancelled())
                        .filter(e -> CollectionProvider.getFreezing().containsKey(e.getPlayer().getUniqueId()))
                        .handler(e -> e.setCancelled(true))
        );
        events.add(
                BukkitEvents.subscribe(plugin, ProjectileLaunchEvent.class, EventPriority.LOW)
                        .filter(BukkitEventFilters.ignoreCancelled())
                        .filter(e -> CollectionProvider.getFreezing().containsKey(e.getEntity().getUniqueId()))
                        .handler(e -> e.setCancelled(true))
        );
        events.add(
                BukkitEvents.subscribe(plugin, EntityShootBowEvent.class, EventPriority.LOW)
                        .filter(BukkitEventFilters.ignoreCancelled())
                        .filter(e -> CollectionProvider.getFreezing().containsKey(e.getEntity().getUniqueId()))
                        .handler(e -> e.setCancelled(true))
        );
        events.add(
                BukkitEvents.subscribe(plugin, PlayerMoveEvent.class, EventPriority.LOW)
                        .filter(BukkitEventFilters.ignoreCancelled())
                        .filter(e -> CollectionProvider.getFreezing().containsKey(e.getPlayer().getUniqueId()))
                        .handler(e -> e.setCancelled(true))
        );
        events.add(
                BukkitEvents.subscribe(plugin, PlayerTeleportEvent.class, EventPriority.LOW)
                        .filter(BukkitEventFilters.ignoreCancelled())
                        .filter(e -> CollectionProvider.getFreezing().containsKey(e.getPlayer().getUniqueId()))
                        .handler(e -> e.setCancelled(true))
        );
        events.add(
                BukkitEvents.subscribe(plugin, BlockBreakEvent.class, EventPriority.LOW)
                        .filter(BukkitEventFilters.ignoreCancelled())
                        .filter(e -> CollectionProvider.getFreezing().containsKey(e.getPlayer().getUniqueId()))
                        .handler(e -> e.setCancelled(true))
        );
        events.add(
                BukkitEvents.subscribe(plugin, BlockPlaceEvent.class, EventPriority.LOW)
                        .filter(BukkitEventFilters.ignoreCancelled())
                        .filter(e -> CollectionProvider.getFreezing().containsKey(e.getPlayer().getUniqueId()))
                        .handler(e -> e.setCancelled(true))
        );
    }

    private void damage(EntityDamageByEntityEvent event) {
        EntityItemHandler entityItemHandler;
        try {
            entityItemHandler = ServiceLocator.get(EntityItemHandler.class);
        } catch (InstantiationException | IllegalAccessException | ServiceNotFoundException ex) {
            logger.error(ex.getMessage(), ex);
            return;
        }

        LivingEntity from = (LivingEntity) event.getDamager();

        Optional<ItemStack> mainHand = entityItemHandler.getItemInMainHand(from);
        GenericEnchantableItem enchantableMainHand = mainHand.isPresent() ? BukkitEnchantableItem.fromItemStack(mainHand.get()) : null;

        boolean hasEnchantment;
        int level;
        try {
            hasEnchantment = api.anyHasEnchantment(AdvancedEnchantment.FREEZING, enchantableMainHand);
            level = api.getMaxLevel(AdvancedEnchantment.FREEZING, enchantableMainHand);
        } catch (APIException ex) {
            logger.error(ex.getMessage(), ex);
            return;
        }

        if (!hasEnchantment || level <= 0) {
            return;
        }

        if (Math.random() > 0.05 * level) {
            return;
        }

        if (event.getEntity() instanceof LivingEntity) {
            ((LivingEntity) event.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, level * 10, level), true);
        }

        if (Math.random() > 0.08 * level) {
            return;
        }

        CollectionProvider.getFreezing().put(event.getEntity().getUniqueId(), level * (event.getFinalDamage() * 0.02), 2500L, TimeUnit.MILLISECONDS);
    }
}
