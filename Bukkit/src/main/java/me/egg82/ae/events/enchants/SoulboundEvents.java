package me.egg82.ae.events.enchants;

import java.util.ArrayList;
import java.util.List;
import me.egg82.ae.APIException;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.BukkitEnchantableItem;
import me.egg82.ae.api.GenericEnchantableItem;
import me.egg82.ae.events.EventHolder;
import me.egg82.ae.utils.PermissionUtil;
import me.egg82.ae.utils.SoulsUtil;
import ninja.egg82.events.BukkitEvents;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class SoulboundEvents extends EventHolder {
    public SoulboundEvents(Plugin plugin) {
        events.add(
                BukkitEvents.subscribe(plugin, EntityDeathEvent.class, EventPriority.NORMAL)
                        .filter(e -> PermissionUtil.canUseEnchant(e.getEntity(), "ae.enchant.soulbound"))
                        .filter(e -> PermissionUtil.canUseEnchant(e.getEntity(), "ae.enchant.vorpal"))
                        .filter(e -> SoulsUtil.tryRemoveSouls(e.getEntity(), 1))
                        .handler(this::death)
        );
    }

    private void death(EntityDeathEvent event) {
        List<ItemStack> removedItems = new ArrayList<>();

        for (ItemStack item : event.getDrops()) {
            GenericEnchantableItem enchantableItem = BukkitEnchantableItem.fromItemStack(item);

            boolean hasEnchantment;
            try {
                hasEnchantment = api.anyHasEnchantment(AdvancedEnchantment.SOULBOUND, enchantableItem);
            } catch (APIException ex) {
                logger.error(ex.getMessage(), ex);
                continue;
            }

            if (hasEnchantment) {
                removedItems.add(item);
            }
        }

        if (!removedItems.isEmpty()) {
            event.getDrops().removeAll(removedItems);
        }
    }
}
