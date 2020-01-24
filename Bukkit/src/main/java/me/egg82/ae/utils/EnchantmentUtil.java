package me.egg82.ae.utils;

import java.util.Map;
import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;
import me.egg82.ae.api.AdvancedEnchantment;
import org.bukkit.enchantments.Enchantment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnchantmentUtil {
    private static Logger logger = LoggerFactory.getLogger(EnchantmentUtil.class);

    private static boolean hasKey;

    private static double highestEnchantWeight = 0.0d;
    private static final NavigableMap<Double, AdvancedEnchantment> customEnchants = new TreeMap<>();

    private static double highestCurseWeight = 0.0d;
    private static final NavigableMap<Double, AdvancedEnchantment> customCurses = new TreeMap<>();

    private static final Random rand = new Random();

    static {
        try {
            Class.forName("org.bukkit.NamespacedKey");
            hasKey = true;
        } catch (ClassNotFoundException e) { hasKey = false; }
        if (hasKey && !BukkitVersionUtil.isAtLeast("1.13")) {
            hasKey = false;
        }

        for (AdvancedEnchantment enchant : AdvancedEnchantment.values()) {
            if (enchant.isCurse()) {
                highestCurseWeight += 1.0d;
                customCurses.put(highestCurseWeight, enchant);
            } else {
                highestEnchantWeight += 1.0d;
                customEnchants.put(highestEnchantWeight, enchant);
            }
        }
        highestEnchantWeight += 1.0d;
        highestCurseWeight += 1.0d;
    }

    public static String getName(Enchantment enchantment) {
        String retVal = hasKey ? enchantment.getKey().getKey() : enchantment.getName();
        if (retVal == null) {
            logger.error("Enchantment " + enchantment + " has no key or name!");
            logger.error("ANOTHER PLUGIN IS FORCEFULLY REGISTERING AN INVALID ENCHANTMENT. THIS WILL LIKELY BREAK MANY THINGS.");
        }
        return retVal;
    }

    public static AdvancedEnchantment getNextEnchant() {
        double lowestWeight = customEnchants.firstKey();

        if (ConfigUtil.getDebugOrFalse()) {
            logger.info("Getting next enchant between " + lowestWeight + " and " + highestEnchantWeight);
        }

        // Select least-recently used enchant with random (weighted random)
        Map.Entry<Double, AdvancedEnchantment> entry = customEnchants.lowerEntry(rand.nextDouble() * (highestEnchantWeight - lowestWeight) + lowestWeight);
        if (entry == null) {
            if (ConfigUtil.getDebugOrFalse()) {
                logger.warn("No enchant found between the specified weights.");
            }
            return null;
        }

        if (ConfigUtil.getDebugOrFalse()) {
            logger.info("Got enchant " + entry.getValue().getName() + " at weight " + entry.getKey());
        }

        // Increase weight (decrease chance) of selected item
        highestEnchantWeight = Math.max(highestEnchantWeight, entry.getKey() + 2.0d); // +2 to keep the highest +1 above the max, for a floored random
        customEnchants.remove(entry.getKey(), entry.getValue());
        customEnchants.put(entry.getKey() + 1.0d, entry.getValue());
        return entry.getValue();
    }

    public static AdvancedEnchantment getNextCurse() {
        double lowestWeight = customCurses.firstKey();

        if (ConfigUtil.getDebugOrFalse()) {
            logger.info("Getting next curse between " + lowestWeight + " and " + highestEnchantWeight);
        }

        // Select least-recently used enchant with random (weighted random)
        Map.Entry<Double, AdvancedEnchantment> entry = customCurses.lowerEntry(rand.nextDouble() * (highestCurseWeight - lowestWeight) + lowestWeight);
        if (entry == null) {
            if (ConfigUtil.getDebugOrFalse()) {
                logger.warn("No curse found between the specified weights.");
            }
            return null;
        }

        if (ConfigUtil.getDebugOrFalse()) {
            logger.info("Got curse " + entry.getValue().getName() + " at weight " + entry.getKey());
        }

        // Increase weight (decrease chance) of selected item
        highestCurseWeight = Math.max(highestCurseWeight, entry.getKey() + 2.0d); // +2 to keep the highest +1 above the max, for a floored random
        customCurses.remove(entry.getKey(), entry.getValue());
        customCurses.put(entry.getKey() + 1.0d, entry.getValue());
        return entry.getValue();
    }
}
