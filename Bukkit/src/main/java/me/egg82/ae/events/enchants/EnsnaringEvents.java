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
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class EnsnaringEvents extends EventHolder {
    public EnsnaringEvents(Plugin plugin) {
        events.add(
                BukkitEvents.subscribe(plugin, EntityDamageByEntityEvent.class, EventPriority.MONITOR)
                        .filter(BukkitEventFilters.ignoreCancelled())
                        .filter(e -> e.getDamager() instanceof LivingEntity)
                        .filter(e -> PermissionUtil.canUseEnchant(e.getDamager(), "ae.enchant.ensnaring"))
                        .handler(this::damage)
        );
        events.add(
                BukkitEvents.subscribe(plugin, ProjectileLaunchEvent.class, EventPriority.LOW)
                        .filter(BukkitEventFilters.ignoreCancelled())
                        .filter(e -> e.getEntityType() == EntityType.ENDER_PEARL)
                        .filter(e -> e.getEntity().getShooter() instanceof LivingEntity)
                        .filter(e -> CollectionProvider.getEnsnaring().containsKey(((LivingEntity) e.getEntity().getShooter()).getUniqueId()))
                        .handler(e -> e.setCancelled(true))
        );
        events.add(
                BukkitEvents.subscribe(plugin, PlayerTeleportEvent.class, EventPriority.LOW)
                        .filter(BukkitEventFilters.ignoreCancelled())
                        .filter(e -> CollectionProvider.getEnsnaring().containsKey(e.getPlayer().getUniqueId()))
                        .handler(e -> e.setCancelled(true))
        );
    }

    private void damage(EntityDamageByEntityEvent event) {
        EntityItemHandler entityItemHandler = getItemHandler();
        if (entityItemHandler == null) {
            return;
        }

        LivingEntity from = (LivingEntity) event.getDamager();

        Optional<ItemStack> mainHand = entityItemHandler.getItemInMainHand(from);
        GenericEnchantableItem enchantableMainHand = mainHand.isPresent() ? BukkitEnchantableItem.fromItemStack(mainHand.get()) : null;

        boolean hasEnchantment;
        int level;
        try {
            hasEnchantment = api.anyHasEnchantment(AdvancedEnchantment.ENSNARING, enchantableMainHand);
            level = api.getMaxLevel(AdvancedEnchantment.ENSNARING, enchantableMainHand);
        } catch (APIException ex) {
            logger.error(ex.getMessage(), ex);
            return;
        }

        if (!hasEnchantment || level <= 0) {
            return;
        }

        CollectionProvider.getEnsnaring().put(event.getEntity().getUniqueId(), level, 2500L * level, TimeUnit.MILLISECONDS);
    }
}
