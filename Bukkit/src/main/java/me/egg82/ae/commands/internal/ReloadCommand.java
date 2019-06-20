package me.egg82.ae.commands.internal;

import co.aikar.taskchain.TaskChain;
import me.egg82.ae.utils.ConfigurationFileUtil;
import me.egg82.ae.utils.LogUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public class ReloadCommand implements Runnable {
    private final Plugin plugin;
    private final TaskChain<?> chain;
    private final CommandSender sender;

    public ReloadCommand(Plugin plugin, TaskChain<?> chain, CommandSender sender) {
        this.plugin = plugin;
        this.chain = chain;
        this.sender = sender;
    }

    public void run() {
        sender.sendMessage(LogUtil.getHeading() + ChatColor.YELLOW + "Reloading, please wait..");

        chain
                .async(() -> ConfigurationFileUtil.reloadConfig(plugin))
                .sync(() -> sender.sendMessage(LogUtil.getHeading() + ChatColor.GREEN + "Configuration reloaded!"))
                .execute();
    }
}
