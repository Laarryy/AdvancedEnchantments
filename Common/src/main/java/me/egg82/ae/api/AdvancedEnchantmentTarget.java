package me.egg82.ae.api;

import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AdvancedEnchantmentTarget extends GenericEnchantmentTarget {
    public static final AdvancedEnchantmentTarget ALL = new AdvancedEnchantmentTarget("ALL");
    public static final AdvancedEnchantmentTarget ARMOR = new AdvancedEnchantmentTarget("ARMOR");
    public static final AdvancedEnchantmentTarget WEAPON = new AdvancedEnchantmentTarget("WEAPON");
    public static final AdvancedEnchantmentTarget TOOL = new AdvancedEnchantmentTarget("TOOL");
    public static final AdvancedEnchantmentTarget BOW = new AdvancedEnchantmentTarget("BOW");
    public static final AdvancedEnchantmentTarget CROSSBOW = new AdvancedEnchantmentTarget("CROSSBOW");
    public static final AdvancedEnchantmentTarget FISHING_ROD = new AdvancedEnchantmentTarget("FISHING_ROD");
    public static final AdvancedEnchantmentTarget BREAKABLE = new AdvancedEnchantmentTarget("BREAKABLE");
    public static final AdvancedEnchantmentTarget WEARABLE = new AdvancedEnchantmentTarget("WEARABLE");
    public static final AdvancedEnchantmentTarget TRIDENT = new AdvancedEnchantmentTarget("TRIDENT");

    private static final Set<AdvancedEnchantmentTarget> allTargets = new HashSet<>();

    public static Set<AdvancedEnchantmentTarget> values() { return ImmutableSet.copyOf(allTargets); }

    private AdvancedEnchantmentTarget(String name) {
        super(UUID.randomUUID(), name, null);
        allTargets.add(this);
    }
}
