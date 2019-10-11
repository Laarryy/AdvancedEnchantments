package me.egg82.ae.utils;

import java.io.*;
import java.nio.file.Files;
import java.text.DecimalFormat;
import me.egg82.ae.extended.CachedConfigValues;
import me.egg82.ae.extended.Configuration;
import ninja.egg82.service.ServiceLocator;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;

public class ConfigurationFileUtil {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationFileUtil.class);

    private static final DecimalFormat format = new DecimalFormat("##0.################");

    private ConfigurationFileUtil() {}

    public static void reloadConfig(Plugin plugin) {
        Configuration config;
        try {
            config = getConfig(plugin, "config.yml", new File(plugin.getDataFolder(), "config.yml"));
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
            return;
        }

        boolean debug = config.getNode("debug").getBoolean(false);

        if (!debug) {
            Reflections.log = null;
        }

        if (debug) {
            logger.info(LogUtil.getHeading() + ChatColor.YELLOW + "Debug " + ChatColor.WHITE + "enabled");
        }

        double enchantChance = config.getNode("enchant-chance").getDouble(0.0d);
        enchantChance = Math.max(0.0d, Math.min(1.0d, enchantChance)); // Clamp value

        if (debug) {
            if (enchantChance > 0.0d) {
                logger.info(LogUtil.getHeading() + ChatColor.GREEN + "Adding custom enchants to vanilla enchanting mechanics with a " + format.format(enchantChance * 100.0d) + "% chance.");
            } else {
                logger.info(LogUtil.getHeading() + ChatColor.YELLOW + "Skipping vanilla enchanting mechanics for custom enchants.");
            }
        }

        boolean bypassUnbreaking = config.getNode("bypass-unbreaking").getBoolean(true);

        if (debug) {
            if (bypassUnbreaking) {
                logger.info(LogUtil.getHeading() + ChatColor.YELLOW + "Bypassing unbreaking for enchants that consume item durability.");
            }  else {
                logger.info(LogUtil.getHeading() + ChatColor.YELLOW + "Enforcing unbreaking for enchants that consume item durability.");
            }
        }

        boolean particles = config.getNode("particles").getBoolean(true);

        if (debug) {
            if (particles) {
                logger.info(LogUtil.getHeading() + ChatColor.GREEN + "Enabling particles.");
            }  else {
                logger.info(LogUtil.getHeading() + ChatColor.YELLOW + "Disabling particles.");
            }
        }

        double lootEnchantChance = config.getNode("loot-chance", "enchant").getDouble(0.00045d);
        lootEnchantChance = Math.max(0.0d, Math.min(1.0d, lootEnchantChance)); // Clamp value
        double lootCurseChance = config.getNode("loot-chance", "curse").getDouble(0.00126d);
        lootCurseChance = Math.max(0.0d, Math.min(1.0d, lootCurseChance)); // Clamp value

        if (lootEnchantChance > 0.0d && lootCurseChance > 0.0d) {
            try {
                Class.forName("com.destroystokyo.paper.loottable.LootableInventoryReplenishEvent");
                logger.info(LogUtil.getHeading() + ChatColor.GREEN + "Enabling loot table modifications.");
                logger.info(LogUtil.getHeading() + ChatColor.GREEN + "Adding custom enchants to loot tables with a " + format.format(lootEnchantChance * 100.0d) + "% chance.");
                logger.info(LogUtil.getHeading() + ChatColor.GREEN + "Adding custom curses to loot tables with a " + format.format(lootCurseChance * 100.0d) + "% chance.");
            } catch (ClassNotFoundException ignored) {
                logger.info(LogUtil.getHeading() + ChatColor.YELLOW + "Loot table API not found. Disabling loot table modifications.");
            }
        } else {
            logger.info(LogUtil.getHeading() + ChatColor.YELLOW + "Skipping loot table modifications.");
        }

        CachedConfigValues cachedValues = CachedConfigValues.builder()
                .debug(debug)
                .enchantChance(enchantChance)
                .bypassUnbreaking(bypassUnbreaking)
                .particles(particles)
                .lootEnchantChance(lootEnchantChance)
                .lootCurseChance(lootCurseChance)
                .build();

        ConfigUtil.setConfiguration(config, cachedValues);

        ServiceLocator.register(config);
        ServiceLocator.register(cachedValues);
    }

    public static Configuration getConfig(Plugin plugin, String resourcePath, File fileOnDisk) throws IOException {
        File parentDir = fileOnDisk.getParentFile();
        if (parentDir.exists() && !parentDir.isDirectory()) {
            Files.delete(parentDir.toPath());
        }
        if (!parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                throw new IOException("Could not create parent directory structure.");
            }
        }
        if (fileOnDisk.exists() && fileOnDisk.isDirectory()) {
            Files.delete(fileOnDisk.toPath());
        }

        if (!fileOnDisk.exists()) {
            try (InputStreamReader reader = new InputStreamReader(plugin.getResource(resourcePath));
                 BufferedReader in = new BufferedReader(reader);
                 FileWriter writer = new FileWriter(fileOnDisk);
                 BufferedWriter out = new BufferedWriter(writer)) {
                String line;
                while ((line = in.readLine()) != null) {
                    out.write(line + System.lineSeparator());
                }
            }
        }

        ConfigurationLoader<ConfigurationNode> loader = YAMLConfigurationLoader.builder().setFlowStyle(DumperOptions.FlowStyle.BLOCK).setIndent(2).setFile(fileOnDisk).build();
        ConfigurationNode root = loader.load(ConfigurationOptions.defaults().setHeader("Comments are gone because update :(. Click here for new config + comments: https://www.spigotmc.org/resources/advancedenchantments-better-enchantments-curses-api.45790/"));
        Configuration config = new Configuration(root);
        ConfigurationVersionUtil.conformVersion(loader, config, fileOnDisk);

        return config;
    }
}
