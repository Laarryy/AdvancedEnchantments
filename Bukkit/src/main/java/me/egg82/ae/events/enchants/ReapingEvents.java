package me.egg82.ae.events.enchants;

import co.aikar.commands.CommandManager;
import java.util.Optional;
import me.egg82.ae.APIException;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.BukkitEnchantableItem;
import me.egg82.ae.api.GenericEnchantableItem;
import me.egg82.ae.enums.Message;
import me.egg82.ae.events.EventHolder;
import me.egg82.ae.services.entity.EntityItemHandler;
import ninja.egg82.events.BukkitEvents;
import ninja.egg82.service.ServiceLocator;
import ninja.egg82.service.ServiceNotFoundException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class ReapingEvents extends EventHolder {
    private final CommandManager commandManager;

    public ReapingEvents(Plugin plugin, CommandManager commandManager) {
        this.commandManager = commandManager;

        events.add(
                BukkitEvents.subscribe(plugin, EntityDeathEvent.class, EventPriority.NORMAL)
                        .filter(e -> e.getEntity().getKiller() != null)
                        .filter(e -> canUseEnchant(e.getEntity().getKiller(), "ae.enchant.reaping"))
                        .filter(e -> canUseEnchant(e.getEntity().getKiller(), "ae.enchant.vorpal"))
                        .handler(this::death)
        );
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
        try {
            hasEnchantment = api.anyHasEnchantment(AdvancedEnchantment.REAPING, enchantableMainHand);
        } catch (APIException ex) {
            logger.error(ex.getMessage(), ex);
            return;
        }

        if (!hasEnchantment) {
            return;
        }

        if (!tryAddSouls(event.getEntity().getKiller(), 1)) {
            commandManager.getCommandIssuer(event.getEntity().getKiller()).sendError(Message.PLAYER__SOUL_VANISHED);
        }
    }
}
