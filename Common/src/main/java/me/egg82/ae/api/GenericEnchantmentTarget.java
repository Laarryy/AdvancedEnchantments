package me.egg82.ae.api;

import java.util.Objects;
import java.util.UUID;

public abstract class GenericEnchantmentTarget {
    protected final UUID uuid;
    protected final String name;

    private final Object concrete;

    private final int hash;

    public GenericEnchantmentTarget(UUID uuid, String name, Object concrete) {
        if (uuid == null) {
            throw new IllegalArgumentException("uuid cannot be null.");
        }
        if (name == null) {
            throw new IllegalArgumentException("name cannot be null.");
        }

        this.uuid = uuid;
        this.name = name;

        this.concrete = concrete;
        this.hash = Objects.hash(uuid);
    }

    public final UUID getUUID() { return uuid; }

    public final String getName() { return name; }

    public final Object getConcrete() { return concrete; }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GenericEnchantmentTarget that = (GenericEnchantmentTarget) o;
        return uuid.equals(that.uuid);
    }

    public int hashCode() { return hash; }
}
