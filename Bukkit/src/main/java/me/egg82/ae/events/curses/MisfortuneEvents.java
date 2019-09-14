package me.egg82.ae.events.curses;

import java.util.Optional;
import me.egg82.ae.APIException;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.BukkitEnchantableItem;
import me.egg82.ae.events.EventHolder;
import me.egg82.ae.services.entity.EntityItemHandler;
import me.egg82.ae.utils.ItemDurabilityUtil;
import me.egg82.ae.utils.PermissionUtil;
import ninja.egg82.events.BukkitEventFilters;
import ninja.egg82.events.BukkitEvents;
import ninja.egg82.service.ServiceLocator;
import ninja.egg82.service.ServiceNotFoundException;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class MisfortuneEvents extends EventHolder {
    public MisfortuneEvents(Plugin plugin) {
        events.add(
                BukkitEvents.subscribe(plugin, BlockBreakEvent.class, EventPriority.NORMAL)
                        .filter(BukkitEventFilters.ignoreCancelled())
                        .filter(e -> e.getPlayer().getGameMode() != GameMode.CREATIVE)
                        .filter(e -> PermissionUtil.canUseEnchant(e.getPlayer(), "ae.curse.misfortune"))
                        .handler(this::blockBreak)
        );
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
        if (!mainHand.isPresent() || event.getBlock().getDrops(mainHand.get()).isEmpty()) {
            return;
        }

        BukkitEnchantableItem enchantableMainHand = BukkitEnchantableItem.fromItemStack(mainHand.get());

        boolean hasEnchantment;
        int level;
        try {
            hasEnchantment = api.anyHasEnchantment(AdvancedEnchantment.MISFORTUNE_CURSE, enchantableMainHand);
            level = api.getMaxLevel(AdvancedEnchantment.MISFORTUNE_CURSE, enchantableMainHand);
        } catch (APIException ex) {
            logger.error(ex.getMessage(), ex);
            return;
        }

        if (!hasEnchantment) {
            return;
        }

        if (Math.random() > 0.2 * level) {
            return;
        }

        // Don't drop exp
        event.setCancelled(true);
        event.getBlock().setType(Material.AIR, true);

        if (!ItemDurabilityUtil.removeDurability(event.getPlayer(), enchantableMainHand, 1, event.getPlayer().getLocation())) {
            entityItemHandler.setItemInMainHand(event.getPlayer(), null);
        }
    }
}
