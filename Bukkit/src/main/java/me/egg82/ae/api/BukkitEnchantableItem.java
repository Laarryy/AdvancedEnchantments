package me.egg82.ae.api;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class BukkitEnchantableItem extends GenericEnchantableItem {
    private static Cache<ItemStack, BukkitEnchantableItem> cache = Caffeine.newBuilder().expireAfterAccess(5L, TimeUnit.MINUTES).build();

    public static GenericEnchantableItem fromItemStack(ItemStack item) {
        if (item == null) {
            return null;
        }

        return cache.get(item, k -> new BukkitEnchantableItem(item));
    }

    private ItemStack item;

    private BukkitEnchantableItem(ItemStack item) {
        super(item);
        this.item = item;
        targets.addAll(getTargets(item));
        enchantments.putAll(getVanillaEnchantments(item));
        enchantments.putAll(getAdvancedEnchantments(item));
    }

    private Set<GenericEnchantmentTarget> getTargets(ItemStack item) {
        Set<GenericEnchantmentTarget> retVal = new HashSet<>();
        for (EnchantmentTarget target : EnchantmentTarget.values()) {
            if (target.includes(item)) {
                retVal.add(BukkitEnchantmentTarget.fromEnchantmentTarget(target));
            }
        }
        return retVal;
    }

    private Map<GenericEnchantment, Integer> getVanillaEnchantments(ItemStack item) {
        Map<GenericEnchantment, Integer> retVal = new HashMap<>();
        for (Map.Entry<Enchantment, Integer> kvp : item.getEnchantments().entrySet()) {
            retVal.put(BukkitEnchantment.fromEnchant(kvp.getKey()), kvp.getValue());
        }
        return retVal;
    }

    private Map<GenericEnchantment, Integer> getAdvancedEnchantments(ItemStack item) {
        Map<GenericEnchantment, Integer> retVal = new HashMap<>();

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasLore()) {
            return retVal;
        }

        for (String line : meta.getLore()) {
            line = ChatColor.stripColor(line);
            String[] split = line.split("\\s+");
            if (split.length <= 1) {
                continue;
            }

            String[] enchantName = Arrays.copyOf(split, split.length - 1, String[].class);
            Optional<AdvancedEnchantment> enchant = AdvancedEnchantment.getByName(String.join(" ", enchantName));
            if (!enchant.isPresent()) {
                continue;
            }

            Optional<Integer> level = getLevel(split[split.length - 1]);
            if (!level.isPresent()) {
                continue;
            }

            retVal.put(enchant.get(), level.get());
        }

        return retVal;
    }

    private Optional<Integer> getLevel(String numerals) {
        numerals = numerals.toUpperCase().replaceAll("[^MDCLXVI]", "");
        if (numerals.isEmpty()) {
            return Optional.empty();
        }

        int retVal = 0;

        for (int i = 0; i < numerals.length(); i++) {
            int one = getValue(numerals.charAt(i));
            int two = i < numerals.length() - 1 ? getValue(numerals.charAt(i + 1)) : -1;

            if (one < two) {
                retVal += two - one;
                i++;
            } else {
                retVal += one;
            }
        }

        return Optional.of(retVal);
    }

    private int getValue(char c) {
        switch (c) {
            case 'I':
                return 1;
            case 'V':
                return 5;
            case 'X':
                return 10;
            case 'L':
                return  50;
            case 'C':
                return 100;
            case 'D':
                return 500;
            case 'M':
                return 1000;
            default:
                return -1;
        }
    }

    public void setEnchantmentLevel(GenericEnchantment enchantment, int level) {
        super.setEnchantmentLevel(enchantment, level);
        rewriteMeta(item);
    }

    public void addEnchantment(GenericEnchantment enchantment) {
        super.addEnchantment(enchantment);
        rewriteMeta(item);
    }

    public void removeEnchantment(GenericEnchantment enchantment) {
        super.removeEnchantment(enchantment);
        rewriteMeta(item);
    }

    public void rewriteMeta(ItemStack item) {
        ItemMeta meta = getMeta(item);

        List<String> lore = !meta.hasLore() ? new ArrayList<>() : stripEnchants(meta.getLore());
        for (Map.Entry<Enchantment, Integer> kvp : item.getEnchantments().entrySet()) {
            if (!enchantments.containsKey(BukkitEnchantment.fromEnchant(kvp.getKey()))) {
                item.removeEnchantment(kvp.getKey());
            }
        }

        Set<BukkitEnchantment> bukkitEnchants = new HashSet<>();
        Set<GenericEnchantment> otherEnchants = new HashSet<>();

        for (Map.Entry<GenericEnchantment, Integer> kvp : enchantments.entrySet()) {
            if (kvp.getKey() instanceof BukkitEnchantment) {
                bukkitEnchants.add((BukkitEnchantment) kvp.getKey());
                item.addEnchantment((Enchantment) kvp.getKey().getConcrete(), kvp.getValue());
            } else {
                otherEnchants.add(kvp.getKey());
                // Skip Bukkit enchants for lore
                lore.add((kvp.getKey().isCurse() ? ChatColor.RED : ChatColor.GRAY) + kvp.getKey().getFriendlyName() + " " + getNumerals(kvp.getValue()));
            }
        }

        boolean hasBukkitEnchants = false;
        boolean hasHackyEnchant = false;
        for (BukkitEnchantment bukkitEnchant : bukkitEnchants) {
            if (bukkitEnchant.getConcrete().equals(Enchantment.DURABILITY) && enchantments.get(bukkitEnchant) == 0) {
                hasHackyEnchant = true;
            } else {
                hasBukkitEnchants = true;
            }
        }

        if (hasBukkitEnchants) {
            if (hasHackyEnchant) {
                enchantments.remove(BukkitEnchantment.fromEnchant(Enchantment.DURABILITY));
                item.removeEnchantment(Enchantment.DURABILITY);
                hasHackyEnchant = false;
            }
            meta.removeItemFlags(ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_ENCHANTS);
        } else {
            if (!otherEnchants.isEmpty() && !hasHackyEnchant) {
                enchantments.put(BukkitEnchantment.fromEnchant(Enchantment.DURABILITY), 0);
                item.addEnchantment(Enchantment.DURABILITY, 1);
                hasHackyEnchant = true;
            }

            if (hasHackyEnchant) {
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                meta.removeItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
            }
        }

        if (!hasHackyEnchant) {
            meta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    private List<String> stripEnchants(List<String> lore) {
        List<String> retVal = new ArrayList<>();

        for (String line : lore) {
            String newLine = ChatColor.stripColor(line);
            String[] split = newLine.split("\\s+");
            if (split.length <= 1) {
                retVal.add(line);
                continue;
            }

            String[] enchantName = Arrays.copyOf(split, split.length - 1, String[].class);
            Optional<AdvancedEnchantment> enchant = AdvancedEnchantment.getByName(String.join(" ", enchantName));
            Optional<Integer> level = getLevel(split[split.length - 1]);
            if (enchant.isPresent() && level.isPresent()) {
                continue;
            }

            retVal.add(line);
        }

        return retVal;
    }

    private ItemMeta getMeta(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            meta = Bukkit.getItemFactory().getItemMeta(item.getType());
            item.setItemMeta(meta);
        }
        return meta;
    }

    private String getNumerals(int level) {
        StringBuilder retVal = new StringBuilder();

        while (level >= 1000) {
            retVal.append('M');
            level -= 1000;
        }
        while (level >= 900) {
            retVal.append("CM");
            level -= 900;
        }
        while (level >= 500) {
            retVal.append('D');
            level -= 500;
        }
        while (level >= 400) {
            retVal.append("CD");
            level -= 400;
        }
        while (level >= 100) {
            retVal.append('C');
            level -= 100;
        }
        while (level >= 90) {
            retVal.append("XC");
            level -= 90;
        }
        while (level >= 50) {
            retVal.append('L');
            level -= 50;
        }
        while (level >= 40) {
            retVal.append("XL");
            level -= 40;
        }
        while (level >= 10) {
            retVal.append('X');
            level -= 10;
        }
        while (level >= 9) {
            retVal.append("IX");
            level -= 9;
        }
        while (level >= 5) {
            retVal.append('V');
            level -= 5;
        }
        while (level >= 4) {
            retVal.append("IV");
            level -= 4;
        }
        while (level >= 1) {
            retVal.append('I');
            level -= 1;
        }

        return retVal.toString();
    }
}
