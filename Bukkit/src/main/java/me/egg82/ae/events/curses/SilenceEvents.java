package me.egg82.ae.events.curses;

import co.aikar.commands.CommandManager;
import java.util.Optional;
import me.egg82.ae.APIException;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.BukkitEnchantableItem;
import me.egg82.ae.api.GenericEnchantableItem;
import me.egg82.ae.enums.Message;
import me.egg82.ae.events.EventHolder;
import me.egg82.ae.utils.PermissionUtil;
import ninja.egg82.events.BukkitEventFilters;
import ninja.egg82.events.BukkitEvents;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.plugin.Plugin;

public class SilenceEvents extends EventHolder {
    private final CommandManager commandManager;

    public SilenceEvents(Plugin plugin, CommandManager commandManager) {
        this.commandManager = commandManager;

        events.add(
                BukkitEvents.subscribe(plugin, AsyncPlayerChatEvent.class, EventPriority.LOWEST)
                        .filter(BukkitEventFilters.ignoreCancelled())
                        .filter(e -> PermissionUtil.canUseEnchant(e.getPlayer(), "ae.curse.silence"))
                        .handler(this::chat)
        );

        events.add(
                BukkitEvents.subscribe(plugin, PlayerCommandPreprocessEvent.class, EventPriority.LOWEST)
                        .filter(BukkitEventFilters.ignoreCancelled())
                        .filter(e -> PermissionUtil.canUseEnchant(e.getPlayer(), "ae.curse.silence"))
                        .handler(this::command)
        );
    }

    private void chat(AsyncPlayerChatEvent event) {
        Optional<EntityEquipment> equipment = Optional.ofNullable(event.getPlayer().getEquipment());
        if (!equipment.isPresent()) {
            return;
        }

        GenericEnchantableItem enchantableHelmet = BukkitEnchantableItem.fromItemStack(equipment.get().getHelmet());

        boolean hasEnchantment;
        try {
            hasEnchantment = api.anyHasEnchantment(AdvancedEnchantment.SILENCE_CURSE, enchantableHelmet);
        } catch (APIException ex) {
            logger.error(ex.getMessage(), ex);
            return;
        }

        if (!hasEnchantment) {
            return;
        }

        commandManager.getCommandIssuer(event.getPlayer()).sendError(Message.PLAYER__SILENCED);
        event.setCancelled(true);
    }

    private void command(PlayerCommandPreprocessEvent event) {
        Optional<EntityEquipment> equipment = Optional.ofNullable(event.getPlayer().getEquipment());
        if (!equipment.isPresent()) {
            return;
        }

        GenericEnchantableItem enchantableHelmet = BukkitEnchantableItem.fromItemStack(equipment.get().getHelmet());

        boolean hasEnchantment;
        try {
            hasEnchantment = api.anyHasEnchantment(AdvancedEnchantment.SILENCE_CURSE, enchantableHelmet);
        } catch (APIException ex) {
            logger.error(ex.getMessage(), ex);
            return;
        }

        if (!hasEnchantment) {
            return;
        }

        commandManager.getCommandIssuer(event.getPlayer()).sendError(Message.PLAYER__SILENCED);
        event.setCancelled(true);
    }
}
