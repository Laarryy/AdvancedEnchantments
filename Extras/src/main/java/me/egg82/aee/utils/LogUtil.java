package me.egg82.aee.utils;

import org.bukkit.ChatColor;

public class LogUtil {
    private LogUtil() {}

    public static String getHeading() { return ChatColor.YELLOW + "[" + ChatColor.AQUA + "AdvancedEnchantmentsExtras" + ChatColor.YELLOW + "] " + ChatColor.RESET; }

    public static String getSourceHeading(String source) { return ChatColor.YELLOW + "[" + ChatColor.LIGHT_PURPLE + source + ChatColor.YELLOW + "] " + ChatColor.RESET; }
}
