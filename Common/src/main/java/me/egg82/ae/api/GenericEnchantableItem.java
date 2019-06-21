package me.egg82.ae.api;

import com.google.common.collect.ImmutableMap;
import java.util.*;

public abstract class GenericEnchantableItem {
    private final Object concrete;

    protected final Set<GenericEnchantmentTarget> targets = new HashSet<>();
    protected final Map<GenericEnchantment, Integer> enchantments = new HashMap<>();

    public GenericEnchantableItem(Object concrete) {
        this.concrete = concrete;
    }

    public final Object getConcrete() { return concrete; }

    public Set<GenericEnchantmentTarget> getEnchantmentTargets() { return targets; }

    public boolean hasEnchantment(GenericEnchantment enchantment) { return enchantments.containsKey(enchantment); }

    public int getEnchantmentLevel(GenericEnchantment enchantment) { return enchantments.computeIfAbsent(enchantment, k -> -1); }

    public void setEnchantmentLevel(GenericEnchantment enchantment, int level) { enchantments.compute(enchantment, (k, v) -> level < 0 ? null : level); }

    public void addEnchantment(GenericEnchantment enchantment) { setEnchantmentLevel(enchantment, enchantment.minLevel); }

    public void removeEnchantment(GenericEnchantment enchantment) { enchantments.remove(enchantment); }

    public Map<GenericEnchantment, Integer> getEnchantments() { return ImmutableMap.copyOf(enchantments); }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GenericEnchantableItem that = (GenericEnchantableItem) o;
        return Objects.equals(concrete, that.concrete);
    }

    public int hashCode() { return Objects.hash(concrete); }
}
