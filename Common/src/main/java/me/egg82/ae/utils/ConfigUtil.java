package me.egg82.ae.utils;

import java.util.Optional;
import me.egg82.ae.extended.CachedConfigValues;
import me.egg82.ae.extended.Configuration;

public class ConfigUtil {
    private static Configuration config = null;
    private static CachedConfigValues cachedConfig = null;

    private ConfigUtil() {}

    public static void setConfiguration(Configuration config, CachedConfigValues cachedConfig) {
        ConfigUtil.config = config;
        ConfigUtil.cachedConfig = cachedConfig;
    }

    /**
     * Grabs the config instance from ServiceLocator
     * @return Optional, instance of the Configuration class
     */
    public static Optional<Configuration> getConfig() { return Optional.ofNullable(config); }

    /**
     * Grabs the cached config instance from ServiceLocator
     * @return Optional, instance of the CachedConfigValues class
     */
    public static Optional<CachedConfigValues> getCachedConfig() { return Optional.ofNullable(cachedConfig); }

    public static boolean getDebugOrFalse() {
        Optional<CachedConfigValues> cachedConfig = getCachedConfig();
        return cachedConfig.isPresent() && cachedConfig.get().getDebug();
    }

    public static boolean getParticlesOrFalse() {
        Optional<CachedConfigValues> cachedConfig = getCachedConfig();
        return cachedConfig.isPresent() && cachedConfig.get().getParticles();
    }
}
