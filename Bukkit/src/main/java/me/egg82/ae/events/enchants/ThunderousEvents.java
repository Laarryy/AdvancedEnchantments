package me.egg82.ae.events.enchants;

import java.util.Optional;
import me.egg82.ae.APIException;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.BukkitEnchantableItem;
import me.egg82.ae.api.GenericEnchantableItem;
import me.egg82.ae.events.EventHolder;
import me.egg82.ae.services.entity.EntityItemHandler;
import me.egg82.ae.utils.PermissionUtil;
import ninja.egg82.events.BukkitEventFilters;
import ninja.egg82.events.BukkitEvents;
import ninja.egg82.service.ServiceLocator;
import ninja.egg82.service.ServiceNotFoundException;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class ThunderousEvents extends EventHolder {
    private final Plugin plugin;

    public ThunderousEvents(Plugin plugin) {
        this.plugin = plugin;

        events.add(
                BukkitEvents.subscribe(plugin, EntityDamageByEntityEvent.class, EventPriority.MONITOR)
                        .filter(BukkitEventFilters.ignoreCancelled())
                        .filter(e -> e.getDamager() instanceof LivingEntity)
                        .filter(e -> PermissionUtil.canUseEnchant(e.getDamager(), "ae.enchant.thunderous"))
                        .handler(this::damage)
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
            hasEnchantment = api.anyHasEnchantment(AdvancedEnchantment.THUNDEROUS, enchantableMainHand);
            level = api.getMaxLevel(AdvancedEnchantment.THUNDEROUS, enchantableMainHand);
        } catch (APIException ex) {
            logger.error(ex.getMessage(), ex);
            return;
        }

        if (!hasEnchantment) {
            return;
        }

        if (Math.random() > 0.04 * level) {
            return;
        }

        event.getEntity().getWorld().strikeLightning(event.getEntity().getLocation());
        for (int i = 0; i < level - 1; i++) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!event.getEntity().isDead()) {
                    event.getEntity().getWorld().strikeLightning(event.getEntity().getLocation());
                }
            }, (i + 1L) * 5L);
        }
    }
}
