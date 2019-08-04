package me.egg82.ae.utils;

import org.bukkit.enchantments.Enchantment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnchantmentUtil {
    private static Logger logger = LoggerFactory.getLogger(EnchantmentUtil.class);

    private static boolean hasKey;

    static {
        try {
            Class.forName("org.bukkit.NamespacedKey");
            hasKey = true;
        } catch (ClassNotFoundException e) { hasKey = false; }
    }

    public static String getName(Enchantment enchantment) {
        String retVal = hasKey ? enchantment.getKey().getKey() : enchantment.getName();
        if (retVal == null) {
            logger.error("Enchantment " + enchantment + " has no key or name!");
            logger.error("ANOTHER PLUGIN IS FORCEFULLY REGISTERING AN INVALID ENCHANTMENT. THIS WILL LIKELY BREAK MANY THINGS.");
        }
        return retVal;
    }
}
