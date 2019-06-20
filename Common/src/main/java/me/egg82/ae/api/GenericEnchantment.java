package me.egg82.ae.api;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public abstract class GenericEnchantment {
    protected final UUID uuid;
    protected final String name;
    protected final String friendlyName;
    protected final boolean curse;
    protected final int minLevel;
    protected final int maxLevel;

    protected final Set<GenericEnchantment> conflicts = new HashSet<>();
    protected final Set<GenericEnchantmentTarget> targets = new HashSet<>();

    private final Object concrete;

    private final int hash;

    public GenericEnchantment(UUID uuid, String name, String friendlyName, boolean isCurse, int minLevel, int maxLevel, Object concrete) {
        if (uuid == null) {
            throw new IllegalArgumentException("uuid cannot be null.");
        }
        if (name == null) {
            throw new IllegalArgumentException("name cannot be null.");
        }
        if (friendlyName == null) {
            throw new IllegalArgumentException("friendlyName cannot be null.");
        }

        this.uuid = uuid;
        this.name = name;
        this.friendlyName = friendlyName;
        this.curse = isCurse;
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;

        this.concrete = concrete;
        this.hash = Objects.hash(uuid);
    }

    public final UUID getUUID() { return uuid; }

    public final String getName() { return name; }

    public final String getFriendlyName() { return friendlyName; }

    public final boolean isCurse() { return curse; }

    public final int getMinLevel() { return minLevel; }

    public final int getMaxLevel() { return maxLevel; }

    public final Object getConcrete() { return concrete; }

    public boolean conflictsWith(GenericEnchantment other) { return other != null && conflicts.contains(other); }

    public boolean canEnchant(GenericEnchantableItem item) {
        if (item == null) {
            return false;
        }

        for (GenericEnchantmentTarget target : item.getEnchantmentTargets()) {
            if (targets.contains(target)) {
                return true;
            }
        }

        return false;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GenericEnchantment that = (GenericEnchantment) o;
        return uuid.equals(that.uuid);
    }

    public int hashCode() { return hash; }
}
