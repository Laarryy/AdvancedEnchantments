package me.egg82.ae.core;

import java.util.*;

import me.egg82.ae.api.GenericEnchantmentTarget;
import org.bukkit.enchantments.Enchantment;

public class ItemData {
    private final Map<Enchantment, Integer> enchantments;
    private final List<String> lore;
    private final Set<GenericEnchantmentTarget> targets;

    public ItemData() {
        enchantments = new HashMap<>();
        lore = null;
        targets = new HashSet<>();
    }

    public ItemData(Map<Enchantment, Integer> enchantments, List<String> lore, Set<GenericEnchantmentTarget> targets) {
        this.enchantments = enchantments;
        this.lore = lore;
        this.targets = targets;
    }

    public Map<Enchantment, Integer> getEnchantments() { return enchantments; }

    public List<String> getLore() { return lore; }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemData itemData = (ItemData) o;
        return enchantments.equals(itemData.enchantments) &&
                Objects.equals(lore, itemData.lore) &&
                targets.equals(itemData.targets);
    }

    public int hashCode() {
        return Objects.hash(enchantments, lore, targets);
    }
}
