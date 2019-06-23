package me.egg82.ae.utils;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationVersionUtil {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationVersionUtil.class);

    private ConfigurationVersionUtil() {}

    public static void conformVersion(ConfigurationLoader<ConfigurationNode> loader, ConfigurationNode config, File fileOnDisk) throws IOException {
        double oldVersion = config.getNode("version").getDouble(1.0d);

        if (config.getNode("version").getDouble(1.0d) == 1.0d) {
            to11(config);
        }
        /*if (config.getNode("version").getDouble() == 2.0d) {
            to21(config);
        }*/

        if (config.getNode("version").getDouble() != oldVersion) {
            File backupFile = new File(fileOnDisk.getParent(), fileOnDisk.getName() + ".bak");
            if (backupFile.exists()) {
                java.nio.file.Files.delete(backupFile.toPath());
            }

            Files.copy(fileOnDisk, backupFile);
            loader.save(config);
        }
    }

    private static void to11(ConfigurationNode config) {
        // Add ignore
        config.getNode("add-enchants").setValue(Boolean.FALSE);

        // Version
        config.getNode("version").setValue(1.1d);
    }

    /*private static void to21(ConfigurationNode config) {
        // Add consensus
        config.getNode("consensus").setValue(-1.0d);

        // Version
        config.getNode("version").setValue(2.1d);
    }*/
}
