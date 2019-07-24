package me.egg82.ae.api;

import com.google.common.collect.ImmutableMap;
import java.util.*;

public abstract class GenericEnchantableItem {
    protected Object concrete;

    protected int souls = 0;
    protected final Set<GenericEnchantmentTarget> targets = new HashSet<>();
    protected final Map<GenericEnchantment, Integer> enchantments = new HashMap<>();

    public GenericEnchantableItem(Object concrete) {
        this.concrete = concrete;
    }

    public final Object getConcrete() { return concrete; }

    public Set<GenericEnchantmentTarget> getEnchantmentTargets() { return targets; }

    public boolean hasEnchantment(GenericEnchantment enchantment) { return enchantment != null && enchantments.containsKey(enchantment); }

    public int getEnchantmentLevel(GenericEnchantment enchantment) { return enchantment == null ? -1 : enchantments.computeIfAbsent(enchantment, k -> -1); }

    public void setEnchantmentLevel(GenericEnchantment enchantment, int level) {
        if (enchantment == null) {
            return;
        }
        enchantments.compute(enchantment, (k, v) -> level < 0 ? null : level);
    }

    public void setEnchantmentLevels(Map<GenericEnchantment, Integer> enchantments) {
        if (enchantments == null || enchantments.isEmpty()) {
            return;
        }
        for (Map.Entry<GenericEnchantment, Integer> kvp : enchantments.entrySet()) {
            this.enchantments.compute(kvp.getKey(), (k, v) -> kvp.getValue() == null || kvp.getValue() < 0 ? null : kvp.getValue());
        }
    }

    public void addEnchantment(GenericEnchantment enchantment) { setEnchantmentLevel(enchantment, enchantment.getMinLevel()); }

    public void addEnchantments(Collection<GenericEnchantment> enchantments) {
        if (enchantments == null) {
            return;
        }

        Map<GenericEnchantment, Integer> mappedEnchantments = new HashMap<>();
        for (GenericEnchantment enchantment : enchantments) {
            if (enchantment != null) {
                mappedEnchantments.put(enchantment, enchantment.getMinLevel());
            }
        }
        setEnchantmentLevels(mappedEnchantments);
    }

    public void removeEnchantment(GenericEnchantment enchantment) {
        if (enchantment == null) {
            return;
        }
        enchantments.remove(enchantment);
    }

    public void removeEnchantments(Collection<GenericEnchantment> enchantments) {
        if (enchantments == null || enchantments.isEmpty()) {
            return;
        }
        for (GenericEnchantment enchantment : enchantments) {
            if (enchantment != null) {
                this.enchantments.remove(enchantment);
            }
        }
    }

    public Map<GenericEnchantment, Integer> getEnchantments() { return ImmutableMap.copyOf(enchantments); }

    public int getSouls() { return souls; }

    public void setSouls(int souls) { this.souls = souls; }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GenericEnchantableItem that = (GenericEnchantableItem) o;
        return Objects.equals(concrete, that.concrete);
    }

    public int hashCode() { return Objects.hash(concrete); }
}
