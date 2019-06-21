package me.egg82.ae.api;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public class BukkitEnchantment extends GenericEnchantment {
    private static ConcurrentMap<String, BukkitEnchantment> enchants = new ConcurrentHashMap<>();

    public static BukkitEnchantment fromEnchant(Enchantment enchant) {
        if (enchant == null) {
            return null;
        }

        return enchants.computeIfAbsent(enchant.getKey().getKey(), k -> new BukkitEnchantment(enchant));
    }

    private Enchantment enchant;

    private BukkitEnchantment(Enchantment enchant) {
        super(UUID.randomUUID(), enchant.getName(), normalizeName(enchant.getName()), enchant.isCursed(), enchant.getStartLevel(), enchant.getMaxLevel(), enchant);
        this.enchant = enchant;
    }

    public boolean conflictsWith(GenericEnchantment other) {
        if (other == null) {
            return false;
        }

        if (other.getConcrete() instanceof Enchantment) {
            return enchant.conflictsWith((Enchantment) other.getConcrete());
        } else {
            return other.conflictsWith(this);
        }
    }

    public boolean canEnchant(GenericEnchantableItem item) {
        if (item == null || item.getConcrete() == null || !(item.getConcrete() instanceof ItemStack)) {
            return false;
        }

        ItemStack i = (ItemStack) item.getConcrete();
        if (!enchant.canEnchantItem(i)) {
            return false;
        }

        for (Map.Entry<GenericEnchantment, Integer> enchantment : item.getEnchantments().entrySet()) {
            if (conflictsWith(enchantment.getKey()) || enchantment.getKey().conflictsWith(this)) {
                return false;
            }
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
