package me.egg82.ae.commands.internal;

import org.bukkit.command.CommandSender;

public class RemoveCommand implements Runnable {
    private final CommandSender sender;
    private final String enchant;

    public RemoveCommand(CommandSender sender, String enchant) {
        this.sender = sender;
        this.enchant = enchant;
    }

    public void run() {

    }
}
