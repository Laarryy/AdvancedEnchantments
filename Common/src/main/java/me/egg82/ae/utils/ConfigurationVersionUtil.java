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

        /*if (config.getNode("version").getDouble(1.0d) == 1.0d) {
            to20(config);
        }
        if (config.getNode("version").getDouble() == 2.0d) {
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

    /*private static void to20(ConfigurationNode config) {
        // Rabbit -> Messaging
        boolean rabbitEnabled = config.getNode("rabbit", "enabled").getBoolean();
        String rabbitAddress = config.getNode("rabbit", "address").getString("");
        int rabbitPort = config.getNode("rabbit", "port").getInt(5672);
        String rabbitUser = config.getNode("rabbit", "user").getString("guest");
        String rabbitPass = config.getNode("rabbit", "pass").getString("guest");
        config.removeChild("rabbit");
        config.getNode("messaging", "type").setValue((rabbitEnabled) ? "rabbit" : "bungee");
        config.getNode("messaging", "rabbit", "address").setValue(rabbitAddress);
        config.getNode("messaging", "rabbit", "port").setValue(rabbitPort);
        config.getNode("messaging", "rabbit", "user").setValue(rabbitUser);
        config.getNode("messaging", "rabbit", "pass").setValue(rabbitPass);

        // sources.order String -> List
        String[] order = config.getNode("sources", "order").getString("").split(",\\s?");
        config.getNode("sources", "order").setValue(Arrays.asList(order));

        // Add ignore
        config.getNode("ignore").setValue(Arrays.asList("127.0.0.1", "localhost", "::1"));

        // Remove async
        config.removeChild("async");

        // Version
        config.getNode("version").setValue(2.0d);
    }

    private static void to21(ConfigurationNode config) {
        // Add consensus
        config.getNode("consensus").setValue(-1.0d);

        // Version
        config.getNode("version").setValue(2.1d);
    }*/
}
