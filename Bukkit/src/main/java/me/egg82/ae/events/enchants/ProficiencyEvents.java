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
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class ProficiencyEvents extends EventHolder {
    public ProficiencyEvents(Plugin plugin) {
        events.add(
                BukkitEvents.subscribe(plugin, BlockBreakEvent.class, EventPriority.LOW)
                        .filter(BukkitEventFilters.ignoreCancelled())
                        .filter(e -> PermissionUtil.canUseEnchant(e.getPlayer(), "ae.enchant.proficiency"))
                        .handler(this::blockBreak)
        );
        events.add(
                BukkitEvents.subscribe(plugin, EntityDeathEvent.class, EventPriority.LOW)
                        .filter(e -> e.getEntity().getKiller() != null)
                        .filter(e -> PermissionUtil.canUseEnchant(e.getEntity().getKiller(), "ae.enchant.proficiency"))
                        .handler(this::death)
        );
        try {
            Class.forName("org.bukkit.event.player.PlayerFishEvent");
            events.add(
                    BukkitEvents.subscribe(plugin, PlayerFishEvent.class, EventPriority.LOW)
                            .filter(BukkitEventFilters.ignoreCancelled())
                            .handler(this::fish)
            );
        } catch (ClassNotFoundException ignored) { }
    }

    private void blockBreak(BlockBreakEvent event) {
        EntityItemHandler entityItemHandler = getItemHandler();
        if (entityItemHandler == null) {
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
        EntityItemHandler entityItemHandler = getItemHandler();
        if (entityItemHandler == null) {
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
        EntityItemHandler entityItemHandler = getItemHandler();
        if (entityItemHandler == null) {
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
