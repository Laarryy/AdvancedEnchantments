package me.egg82.ae.api;

import java.util.*;
import me.egg82.ae.utils.ConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class GenericEnchantment {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    protected final UUID uuid;
    protected final String name;
    protected final String friendlyName;
    protected final boolean curse;
    protected final int minLevel;
    protected final int maxLevel;

    protected final Set<GenericEnchantment> conflicts = new HashSet<>();
    protected final Set<GenericEnchantmentTarget> targets = new HashSet<>();

    protected Object concrete;

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

    public boolean conflictsWith(GenericEnchantment other) {
        if (ConfigUtil.getDebugOrFalse()) {
            logger.info("Checking if enchant " + name + " conflicts with " + (other == null ? "null" : other.name));
            logger.info("Conflicts: " + (other != null && conflicts.contains(other)));
            return other != null && conflicts.contains(other);
        }

        return other != null && conflicts.contains(other);
    }

    public boolean canEnchant(GenericEnchantableItem item) {
        if (ConfigUtil.getDebugOrFalse()) {
            logger.info("Checking if enchant " + name + " is compatible with " + (item == null ? "null" : item.getConcrete()));
        }

        if (item == null) {
            if (ConfigUtil.getDebugOrFalse()) {
                logger.info("Compatible: false");
            }
            return false;
        }

        boolean good = false;
        for (GenericEnchantmentTarget target : item.getEnchantmentTargets()) {
            if (targets.contains(target)) {
                if (ConfigUtil.getDebugOrFalse()) {
                    logger.info("Found valid target " + target.getName() + " for enchant " + name);
                }

                good = true;
                break;
            }
        }

        if (good) {
            for (Map.Entry<GenericEnchantment, Integer> enchantment : item.getEnchantments().entrySet()) {
                if (conflictsWith(enchantment.getKey()) || enchantment.getKey().conflictsWith(this)) {
                    if (ConfigUtil.getDebugOrFalse()) {
                        logger.info("Enchant " + name + " conflicts with existing enchant " + enchantment.getKey().name + " on item.");
                    }
                    return false;
                }
            }
        } else {
            if (ConfigUtil.getDebugOrFalse()) {
                logger.info("Enchant " + name + " does not have a valid target");
            }
        }

        if (ConfigUtil.getDebugOrFalse()) {
            logger.info("Compatible: " + good);
        }
        return good;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GenericEnchantment)) return false;
        GenericEnchantment that = (GenericEnchantment) o;
        return uuid.equals(that.uuid);
    }

    public int hashCode() { return hash; }
}
