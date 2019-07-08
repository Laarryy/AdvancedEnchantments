package me.egg82.ae.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.bukkit.enchantments.Enchantment;

public class ItemData {
    private final Map<Enchantment, Integer> enchantments;
    private final List<String> lore;

    public ItemData() {
        enchantments = new HashMap<>();
        lore = null;
    }

    public ItemData(Map<Enchantment, Integer> enchantments, List<String> lore) {
        this.enchantments = enchantments;
        this.lore = lore;
    }

    public Map<Enchantment, Integer> getEnchantments() { return enchantments; }

    public List<String> getLore() { return lore; }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemData itemData = (ItemData) o;
        return enchantments.equals(itemData.enchantments) &&
                Objects.equals(lore, itemData.lore);
    }

    public int hashCode() {
        return Objects.hash(enchantments, lore);
    }
}
