package me.egg82.ae.events.enchants;

import co.aikar.commands.CommandManager;
import java.util.Optional;
import me.egg82.ae.APIException;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.BukkitEnchantableItem;
import me.egg82.ae.api.GenericEnchantableItem;
import me.egg82.ae.enums.Message;
import me.egg82.ae.events.EventHolder;
import me.egg82.ae.services.CollectionProvider;
import me.egg82.ae.services.entity.EntityItemHandler;
import me.egg82.ae.utils.PermissionUtil;
import me.egg82.ae.utils.SoulsUtil;
import ninja.egg82.events.BukkitEvents;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class ReapingEvents extends EventHolder {
    private final CommandManager commandManager;

    public ReapingEvents(Plugin plugin, CommandManager commandManager) {
        this.commandManager = commandManager;

        events.add(
                BukkitEvents.subscribe(plugin, EntityDeathEvent.class, EventPriority.MONITOR)
                        .filter(e -> e.getEntity().getKiller() != null)
                        .filter(e -> PermissionUtil.canUseEnchant(e.getEntity().getKiller(), "ae.enchant.reaping"))
                        .filter(e -> PermissionUtil.canUseEnchant(e.getEntity().getKiller(), "ae.enchant.vorpal"))
                        .filter(e -> CollectionProvider.getSouls().add(e.getEntity().getUniqueId())) // Should be the last filter
                        .handler(this::death)
        );
    }

    private void death(EntityDeathEvent event) {
        EntityItemHandler entityItemHandler = getItemHandler();
        if (entityItemHandler == null) {
            CollectionProvider.getSouls().remove(event.getEntity().getUniqueId());
            return;
        }

        Optional<ItemStack> mainHand = entityItemHandler.getItemInMainHand(event.getEntity().getKiller());
        GenericEnchantableItem enchantableMainHand = mainHand.isPresent() ? BukkitEnchantableItem.fromItemStack(mainHand.get()) : null;

        boolean hasEnchantment;
        try {
            hasEnchantment = api.anyHasEnchantment(AdvancedEnchantment.REAPING, enchantableMainHand);
        } catch (APIException ex) {
            CollectionProvider.getSouls().remove(event.getEntity().getUniqueId());
            logger.error(ex.getMessage(), ex);
            return;
        }

        if (!hasEnchantment) {
            CollectionProvider.getSouls().remove(event.getEntity().getUniqueId());
            return;
        }

        if (!SoulsUtil.tryAddSouls(event.getEntity().getKiller(), 1)) {
            CollectionProvider.getSouls().remove(event.getEntity().getUniqueId());
            commandManager.getCommandIssuer(event.getEntity().getKiller()).sendError(Message.PLAYER__SOUL_VANISHED);
        }
    }
}
