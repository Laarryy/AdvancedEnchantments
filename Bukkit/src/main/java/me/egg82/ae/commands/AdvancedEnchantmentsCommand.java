package me.egg82.ae.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import co.aikar.taskchain.TaskChainFactory;
import me.egg82.ae.commands.internal.ReloadCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

@CommandAlias("advancedenchantments|ae")
public class AdvancedEnchantmentsCommand extends BaseCommand {
    private final Plugin plugin;
    private final TaskChainFactory taskFactory;

    public AdvancedEnchantmentsCommand(Plugin plugin, TaskChainFactory taskFactory) {
        this.plugin = plugin;
        this.taskFactory = taskFactory;
    }

    @Subcommand("reload")
    @CommandPermission("ae.admin")
    @Description("Reloads the plugin.")
    public void onReload(CommandSender sender) {
        new ReloadCommand(plugin, taskFactory.newChain(), sender).run();
    }

    @Subcommand("add|set")
    @CommandPermission("ae.admin")
    @Description("Adds an enchantment (or sets its level) to your currently-held item.")
    @Syntax("<enchant> [level]")
    @CommandCompletion("@enchant")
    public void onSet(CommandSender sender, String enchant, int level) {
        //new SetCommand(sender, enchant, level).run();
    }

    @Subcommand("remove")
    @CommandPermission("ae.admin")
    @Description("Removed an enchantment from your currently-held item.")
    @Syntax("<enchant>")
    @CommandCompletion("@enchant")
    public void onRemove(CommandSender sender, String enchant) {
        //new RemoveCommand(sender, enchant).run();
    }

    @CatchUnknown @Default
    @CommandCompletion("@subcommand")
    public void onDefault(CommandSender sender, String[] args) {
        Bukkit.getServer().dispatchCommand(sender, "advancedenchantments help");
    }

    @HelpCommand
    @Syntax("[command]")
    public void onHelp(CommandSender sender, CommandHelp help) { help.showHelp(); }
}
