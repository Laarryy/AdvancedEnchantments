package me.egg82.ae.commands.internal;

import org.bukkit.command.CommandSender;

public class SetCommand implements Runnable {
    private final CommandSender sender;
    private final String enchant;
    private final int level;

    public SetCommand(CommandSender sender, String enchant, int level) {
        this.sender = sender;
        this.enchant = enchant;
        this.level = level;
    }

    public void run() {

    }
}
