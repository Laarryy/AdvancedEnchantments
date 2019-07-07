package me.egg82.ae.api;

import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AdvancedEnchantmentTarget extends GenericEnchantmentTarget {
    private static final Set<AdvancedEnchantmentTarget> allTargets = new HashSet<>(); // Needs to be set BEFORE the targets are defined, else NPE

    public static final AdvancedEnchantmentTarget ALL = new AdvancedEnchantmentTarget("ALL");
    public static final AdvancedEnchantmentTarget ARMOR = new AdvancedEnchantmentTarget("ARMOR");
    public static final AdvancedEnchantmentTarget ARMOR_FEET = new AdvancedEnchantmentTarget("ARMOR_FEET");
    public static final AdvancedEnchantmentTarget ARMOR_LEGS = new AdvancedEnchantmentTarget("ARMOR_LEGS");
    public static final AdvancedEnchantmentTarget ARMOR_TORSO = new AdvancedEnchantmentTarget("ARMOR_TORSO");
    public static final AdvancedEnchantmentTarget ARMOR_HEAD = new AdvancedEnchantmentTarget("ARMOR_HEAD");
    public static final AdvancedEnchantmentTarget WEAPON = new AdvancedEnchantmentTarget("WEAPON");
    public static final AdvancedEnchantmentTarget TOOL = new AdvancedEnchantmentTarget("TOOL");
    public static final AdvancedEnchantmentTarget BOW = new AdvancedEnchantmentTarget("BOW");
    public static final AdvancedEnchantmentTarget FISHING_ROD = new AdvancedEnchantmentTarget("FISHING_ROD");
    public static final AdvancedEnchantmentTarget BREAKABLE = new AdvancedEnchantmentTarget("BREAKABLE");
    public static final AdvancedEnchantmentTarget WEARABLE = new AdvancedEnchantmentTarget("WEARABLE");
    public static final AdvancedEnchantmentTarget TRIDENT = new AdvancedEnchantmentTarget("TRIDENT");
    public static final AdvancedEnchantmentTarget CROSSBOW = new AdvancedEnchantmentTarget("CROSSBOW");

    public static Set<AdvancedEnchantmentTarget> values() { return ImmutableSet.copyOf(allTargets); }

    private AdvancedEnchantmentTarget(String name) {
        super(UUID.randomUUID(), name, null);
        allTargets.add(this);
    }
}
