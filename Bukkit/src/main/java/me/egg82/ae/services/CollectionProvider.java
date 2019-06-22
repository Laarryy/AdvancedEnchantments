package me.egg82.ae.services;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import me.egg82.ae.core.FakeBlockData;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.bukkit.Location;

public class CollectionProvider {
    private CollectionProvider() {}

    private static ExpiringMap<UUID, Double> bleeding = ExpiringMap.builder().variableExpiration().expirationPolicy(ExpirationPolicy.CREATED).build();
    public static ExpiringMap<UUID, Double> getBleeding() { return bleeding; }

    private static ExpiringMap<UUID, Double> freezing = ExpiringMap.builder().variableExpiration().expirationPolicy(ExpirationPolicy.CREATED).build();
    public static ExpiringMap<UUID, Double> getFreezing() { return freezing; }

    private static Set<Location> explosive = new HashSet<>();
    public static Set<Location> getExplosive() { return explosive; }

    private static Set<Location> artisan = new HashSet<>();
    public static Set<Location> getArtisan() { return artisan; }

    private static Set<UUID> fiery = new HashSet<>();
    public static Set<UUID> getFiery() { return fiery; }

    private static ConcurrentMap<Location, FakeBlockData> fakeBlocks = new ConcurrentHashMap<>();
    public static ConcurrentMap<Location, FakeBlockData> getFakeBlocks() { return fakeBlocks; }
}
