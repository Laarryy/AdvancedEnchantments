package me.egg82.ae.events.enchants;

import java.util.Optional;
import me.egg82.ae.APIException;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.BukkitEnchantableItem;
import me.egg82.ae.api.GenericEnchantableItem;
import me.egg82.ae.events.EventHolder;
import me.egg82.ae.services.entity.EntityItemHandler;
import ninja.egg82.events.BukkitEventFilters;
import ninja.egg82.events.BukkitEvents;
import ninja.egg82.service.ServiceLocator;
import ninja.egg82.service.ServiceNotFoundException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class ProficiencyEvents extends EventHolder {
    public ProficiencyEvents(Plugin plugin) {
        events.add(
                BukkitEvents.subscribe(plugin, BlockBreakEvent.class, EventPriority.NORMAL)
                        .filter(BukkitEventFilters.ignoreCancelled())
                        .filter(e -> canUseEnchant(e.getPlayer(), "ae.enchant.proficiency"))
                        .handler(this::blockBreak)
        );
        events.add(
                BukkitEvents.subscribe(plugin, EntityDeathEvent.class, EventPriority.NORMAL)
                        .filter(e -> e.getEntity().getKiller() != null)
                        .filter(e -> canUseEnchant(e.getEntity().getKiller(), "ae.enchant.proficiency"))
                        .handler(this::death)
        );
        try {
            Class.forName("org.bukkit.event.player.PlayerFishEvent");
            events.add(
                    BukkitEvents.subscribe(plugin, PlayerFishEvent.class, EventPriority.NORMAL)
                            .filter(BukkitEventFilters.ignoreCancelled())
                            .handler(this::fish)
            );
        } catch (ClassNotFoundException ignored) {}
    }

    private void blockBreak(BlockBreakEvent event) {
        EntityItemHandler entityItemHandler;
        try {
            entityItemHandler = ServiceLocator.get(EntityItemHandler.class);
        } catch (InstantiationException | IllegalAccessException | ServiceNotFoundException ex) {
            logger.error(ex.getMessage(), ex);
            return;
        }

        Optional<ItemStack> mainHand = entityItemHandler.getItemInMainHand(event.getPlayer());
        GenericEnchantableItem enchantableMainHand = mainHand.isPresent() ? BukkitEnchantableItem.fromItemStack(mainHand.get()) : null;

        boolean hasEnchantment;
        int level;
        try {
            hasEnchantment = api.anyHasEnchantment(AdvancedEnchantment.PROFICIENCY, enchantableMainHand);
            level = api.getMaxLevel(AdvancedEnchantment.PROFICIENCY, enchantableMainHand);
        } catch (APIException ex) {
            logger.error(ex.getMessage(), ex);
            return;
        }

        if (!hasEnchantment) {
            return;
        }

        double exp = event.getExpToDrop();
        exp += exp - (exp / ((double) level + 1.0d));
        event.setExpToDrop((int) exp);
    }

    private void death(EntityDeathEvent event) {
        EntityItemHandler entityItemHandler;
        try {
            entityItemHandler = ServiceLocator.get(EntityItemHandler.class);
        } catch (InstantiationException | IllegalAccessException | ServiceNotFoundException ex) {
            logger.error(ex.getMessage(), ex);
            return;
        }

        Optional<ItemStack> mainHand = entityItemHandler.getItemInMainHand(event.getEntity().getKiller());
        GenericEnchantableItem enchantableMainHand = mainHand.isPresent() ? BukkitEnchantableItem.fromItemStack(mainHand.get()) : null;

        boolean hasEnchantment;
        int level;
        try {
            hasEnchantment = api.anyHasEnchantment(AdvancedEnchantment.PROFICIENCY, enchantableMainHand);
            level = api.getMaxLevel(AdvancedEnchantment.PROFICIENCY, enchantableMainHand);
        } catch (APIException ex) {
            logger.error(ex.getMessage(), ex);
            return;
        }

        if (!hasEnchantment) {
            return;
        }

        double exp = event.getDroppedExp();
        exp += exp - (exp / ((double) level + 1.0d));
        event.setDroppedExp((int) exp);
    }

    private void fish(PlayerFishEvent event) {
        EntityItemHandler entityItemHandler;
        try {
            entityItemHandler = ServiceLocator.get(EntityItemHandler.class);
        } catch (InstantiationException | IllegalAccessException | ServiceNotFoundException ex) {
            logger.error(ex.getMessage(), ex);
            return;
        }

        Optional<ItemStack> mainHand = entityItemHandler.getItemInMainHand(event.getPlayer());
        GenericEnchantableItem enchantableMainHand = mainHand.isPresent() ? BukkitEnchantableItem.fromItemStack(mainHand.get()) : null;

        boolean hasEnchantment;
        int level;
        try {
            hasEnchantment = api.anyHasEnchantment(AdvancedEnchantment.PROFICIENCY, enchantableMainHand);
            level = api.getMaxLevel(AdvancedEnchantment.PROFICIENCY, enchantableMainHand);
        } catch (APIException ex) {
            logger.error(ex.getMessage(), ex);
            return;
        }

        if (!hasEnchantment) {
            return;
        }

        double exp = event.getExpToDrop();
        exp += exp - (exp / ((double) level + 1.0d));
        event.setExpToDrop((int) exp);
    }
}
