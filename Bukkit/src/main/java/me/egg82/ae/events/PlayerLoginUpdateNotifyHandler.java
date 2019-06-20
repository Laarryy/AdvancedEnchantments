package me.egg82.ae.events;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import me.egg82.ae.extended.Configuration;
import me.egg82.ae.utils.ConfigUtil;
import me.egg82.ae.utils.LogUtil;
import ninja.egg82.service.ServiceLocator;
import ninja.egg82.service.ServiceNotFoundException;
import ninja.egg82.updater.SpigotUpdater;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerLoginUpdateNotifyHandler implements Consumer<PlayerLoginEvent> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Plugin plugin;

    public PlayerLoginUpdateNotifyHandler(Plugin plugin) { this.plugin = plugin; }

    public void accept(PlayerLoginEvent event) {
        if (!event.getPlayer().hasPermission("ae.admin")) {
            return;
        }

        Optional<Configuration> config = ConfigUtil.getConfig();
        if (!config.isPresent()) {
            return;
        }

        SpigotUpdater updater;

        try {
            updater = ServiceLocator.get(SpigotUpdater.class);
        } catch (InstantiationException | IllegalAccessException | ServiceNotFoundException ex) {
            logger.error(ex.getMessage(), ex);
            return;
        }

        if (!config.get().getNode("update", "check").getBoolean(true)) {
            return;
        }

        updater.isUpdateAvailable().thenAccept(v -> {
            if (!v) {
                return;
            }

            if (config.get().getNode("update", "notify").getBoolean(true)) {
                try {
                    String message = LogUtil.getHeading() + ChatColor.AQUA + " (Bukkit) has an " + ChatColor.GREEN + "update" + ChatColor.AQUA + " available! New version: " + ChatColor.YELLOW + updater.getLatestVersion().get();
                    Bukkit.getScheduler().runTask(plugin, () -> event.getPlayer().sendMessage(message));
                } catch (ExecutionException ex) {
                    logger.error(ex.getMessage(), ex);
                } catch (InterruptedException ex) {
                    logger.error(ex.getMessage(), ex);
                    Thread.currentThread().interrupt();
                }
            }
        });
    }
}
