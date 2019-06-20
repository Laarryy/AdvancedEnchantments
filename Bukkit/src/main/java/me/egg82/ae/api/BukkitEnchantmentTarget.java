package me.egg82.ae.api;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.bukkit.enchantments.EnchantmentTarget;

public class BukkitEnchantmentTarget extends GenericEnchantmentTarget {
    private static ConcurrentMap<String, GenericEnchantmentTarget> targets = new ConcurrentHashMap<>();

    public static GenericEnchantmentTarget fromEnchantmentTarget(EnchantmentTarget target) {
        if (target == null) {
            return null;
        }

        return targets.computeIfAbsent(target.name(), k -> {
            for (AdvancedEnchantmentTarget t : AdvancedEnchantmentTarget.values()) {
                if (k.equals(t.getName())) {
                    return t;
                }
            }
            return new BukkitEnchantmentTarget(target);
        });
    }

    private BukkitEnchantmentTarget(EnchantmentTarget target) {
        super(UUID.randomUUID(), target.name(), target);
    }
}
