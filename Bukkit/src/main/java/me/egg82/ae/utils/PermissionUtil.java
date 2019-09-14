package me.egg82.ae.utils;

import org.bukkit.entity.Player;

public class PermissionUtil {
    private PermissionUtil() { }

    public static boolean canUseEnchant(Object obj, String node) { return !(obj instanceof Player) || ((Player) obj).hasPermission(node); }
}
