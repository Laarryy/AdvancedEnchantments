package me.egg82.ae.api;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import me.egg82.ae.utils.ConfigUtil;
import me.egg82.ae.utils.EnchantmentUtil;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BukkitEnchantment extends GenericEnchantment {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static ConcurrentMap<String, BukkitEnchantment> enchants = new ConcurrentHashMap<>();

    public static BukkitEnchantment fromEnchant(Enchantment enchant) {
        if (enchant == null) {
            return null;
        }

        return enchants.computeIfAbsent(EnchantmentUtil.getName(enchant), k -> new BukkitEnchantment(enchant));
    }

    private static final BukkitEnchantment MENDING = fromEnchant(Enchantment.getByName("MENDING"));

    static {
        if (MENDING != null) {
            MENDING.conflicts.add(AdvancedEnchantment.PROFICIENCY);
        }
    }

    private Enchantment enchant;

    private BukkitEnchantment(Enchantment enchant) {
        super(UUID.randomUUID(), EnchantmentUtil.getName(enchant), normalizeName(EnchantmentUtil.getName(enchant)), EnchantmentUtil.getName(enchant).toLowerCase().endsWith("_curse"), enchant.getStartLevel(), enchant.getMaxLevel(), enchant);
        this.enchant = enchant;
    }

    public boolean conflictsWith(GenericEnchantment other) {
        if (ConfigUtil.getDebugOrFalse()) {
            logger.info("Checking if enchant " + name + " conflicts with " + (other == null ? "null" : other.name));
        }

        if (other == null) {
            if (ConfigUtil.getDebugOrFalse()) {
                logger.info("Conflicts: false");
            }
            return false;
        }

        if (other.getConcrete() instanceof Enchantment) {
            if (ConfigUtil.getDebugOrFalse()) {
                logger.info("Conflicts: " + enchant.conflictsWith((Enchantment) other.getConcrete()));
            }
            return enchant.conflictsWith((Enchantment) other.getConcrete());
        } else {
            if (ConfigUtil.getDebugOrFalse()) {
                logger.info("Conflicts: " + other.conflictsWith(this));
            }
            return other.conflictsWith(this);
        }
    }

    public boolean canEnchant(GenericEnchantableItem item) {
        if (ConfigUtil.getDebugOrFalse()) {
            logger.info("Checking if enchant " + name + " is compatible with " + (item == null ? "null" : item.getConcrete()));
        }

        if (item == null || item.getConcrete() == null || !(item.getConcrete() instanceof ItemStack)) {
            if (ConfigUtil.getDebugOrFalse()) {
                logger.info("Compatible: false");
            }
            return false;
        }

        ItemStack i = (ItemStack) item.getConcrete();
        if (!enchant.canEnchantItem(i)) {
            if (ConfigUtil.getDebugOrFalse()) {
                logger.info("Enchant " + name + " can't enchant item (vanilla check)");
            }
            return false;
        }

        for (Map.Entry<GenericEnchantment, Integer> enchantment : item.getEnchantments().entrySet()) {
            if (conflictsWith(enchantment.getKey()) || enchantment.getKey().conflictsWith(this)) {
                if (ConfigUtil.getDebugOrFalse()) {
                    logger.info("Enchant " + name + " conflicts with existing enchant " + enchantment.getKey().name + " on item");
                }
                return false;
            }
        }

        if (ConfigUtil.getDebugOrFalse()) {
            logger.info("Compatible: true");
        }
        return true;
    }

    private static String normalizeName(String name) {
        String[] split = name.split("_");

        if (split[split.length - 1].equalsIgnoreCase("curse")) {
            String[] newSplit = new String[split.length + 1];
            newSplit[0] = "curse";
            newSplit[1] = "of";
            for (int i = 0; i < split.length - 1; i++) {
                newSplit[i + 2] = split[i];
            }
        }

        for (int i = 0; i < split.length; i++) {
            if (
                    split[i].equalsIgnoreCase("of")
                            || split[i].equalsIgnoreCase("and")
                            || split[i].equalsIgnoreCase("or")
            ) {
                continue;
            }

            split[i] = split[i].substring(0, 1).toUpperCase() + split[i].substring(1).toLowerCase();
        }

        return String.join(" ", split);
    }
}
