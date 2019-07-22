package me.egg82.ae.services;

import co.aikar.commands.BukkitMessageFormatter;
import co.aikar.commands.CommandManager;
import co.aikar.locales.MessageKeyProvider;

public class PluginMessageFormatter extends BukkitMessageFormatter {
    private String header;

    public PluginMessageFormatter(CommandManager manager, MessageKeyProvider header) { this(manager.getLocales().getMessage(null, header)); }

    public PluginMessageFormatter(String header) {
        super();
        this.header = header;
    }

    public String format(String message) {
        message = header + message;
        return super.format(message);
    }
}
