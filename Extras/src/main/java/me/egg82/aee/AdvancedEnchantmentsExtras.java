package me.egg82.aee;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import me.egg82.ae.extended.Configuration;
import me.egg82.ae.services.GameAnalyticsErrorHandler;
import me.egg82.ae.utils.*;
import me.egg82.aee.hooks.PlayerAnalyticsHook;
import me.egg82.aee.hooks.PluginHook;
import me.egg82.aee.services.entity.EntityItemHandler;
import me.egg82.aee.utils.BukkitEnvironmentUtil;
import me.egg82.aee.utils.BukkitVersionUtil;
import me.egg82.aee.utils.ConfigurationFileUtil;
import me.egg82.aee.utils.LogUtil;
import ninja.egg82.events.BukkitEventSubscriber;
import ninja.egg82.service.ServiceLocator;
import ninja.egg82.service.ServiceNotFoundException;
import ninja.egg82.updater.SpigotUpdater;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdvancedEnchantmentsExtras {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ExecutorService workPool = Executors.newFixedThreadPool(1, new ThreadFactoryBuilder().setNameFormat("AdvancedEnchantmentsExtras-%d").build());

    private List<BukkitEventSubscriber<?>> events = new ArrayList<>();
    private List<Integer> tasks = new ArrayList<>();

    private Metrics metrics = null;

    private final Plugin plugin;
    private final boolean isBukkit;

    public AdvancedEnchantmentsExtras(Plugin plugin) {
        isBukkit = BukkitEnvironmentUtil.getEnvironment() == BukkitEnvironmentUtil.Environment.BUKKIT;
        this.plugin = plugin;
    }

    public void onLoad() {
        if (BukkitEnvironmentUtil.getEnvironment() != BukkitEnvironmentUtil.Environment.PAPER) {
            log(Level.INFO, ChatColor.AQUA + "====================================");
            log(Level.INFO, ChatColor.YELLOW + "AdvancedEnchantmentsExtras runs better on Paper!");
            log(Level.INFO, ChatColor.YELLOW + "https://whypaper.emc.gs/");
            log(Level.INFO, ChatColor.AQUA + "====================================");
        }

        if (BukkitVersionUtil.getGameVersion().startsWith("1.8")) {
            log(Level.INFO, ChatColor.AQUA + "====================================");
            log(Level.INFO, ChatColor.DARK_RED + "DEAR LORD why are you on 1.8???");
            log(Level.INFO, ChatColor.DARK_RED + "Have you tried ViaVersion or ProtocolSupport lately?");
            log(Level.INFO, ChatColor.AQUA + "====================================");
        }
    }

    public void onEnable() {
        GameAnalyticsErrorHandler.open(ServerIDUtil.getID(new File(plugin.getDataFolder(), "stats-id.txt")), plugin.getDescription().getVersion(), Bukkit.getVersion());

        loadServices();
        loadEvents();
        loadTasks();
        loadHooks();
        loadMetrics();

        plugin.getServer().getConsoleSender().sendMessage(LogUtil.getHeading() + ChatColor.GREEN + "Enabled");

        plugin.getServer().getConsoleSender().sendMessage(LogUtil.getHeading()
                + ChatColor.YELLOW + "[" + ChatColor.AQUA + "Version " + ChatColor.WHITE + plugin.getDescription().getVersion() + ChatColor.YELLOW +  "] "
                + ChatColor.YELLOW + "[" + ChatColor.WHITE + tasks.size() + ChatColor.GRAY + " Tasks" + ChatColor.YELLOW +  "] "
                + ChatColor.YELLOW + "[" + ChatColor.WHITE + events.size() + ChatColor.BLUE + " Events" + ChatColor.YELLOW +  "]"
        );

        workPool.submit(this::checkUpdate);
    }

    public void onDisable() {
        for (int task : tasks) {
            Bukkit.getScheduler().cancelTask(task);
        }
        tasks.clear();

        for (BukkitEventSubscriber<?> event : events) {
            event.cancel();
        }
        events.clear();

        unloadHooks();
        unloadServices();

        plugin.getServer().getConsoleSender().sendMessage(LogUtil.getHeading() + ChatColor.DARK_RED + "Disabled");

        GameAnalyticsErrorHandler.close();
    }

    private void loadServices() {
        ConfigurationFileUtil.reloadConfig(plugin);

        try {
            ServiceLocator.register(BukkitVersionUtil.getBestMatch(EntityItemHandler.class, BukkitVersionUtil.getGameVersion(), "me.egg82.ae.services.entity", false), false);
        } catch (InstantiationException | IllegalAccessException ex) {
            logger.error(ex.getMessage(), ex);
        }

        ServiceLocator.register(new SpigotUpdater(plugin, 45790));
    }

    private void loadEvents() { }

    private boolean canUseEnchant(Object obj, String node) { return !(obj instanceof Player) || ((Player) obj).hasPermission(node); }

    private void loadTasks() { }

    private void loadHooks() {
        PluginManager manager = plugin.getServer().getPluginManager();

        if (manager.getPlugin("Plan") != null) {
            plugin.getServer().getConsoleSender().sendMessage(LogUtil.getHeading() + ChatColor.GREEN + "Enabling support for Plan.");
            ServiceLocator.register(new PlayerAnalyticsHook());
        } else {
            plugin.getServer().getConsoleSender().sendMessage(LogUtil.getHeading() + ChatColor.YELLOW + "Plan was not found. Personal analytics support has been disabled.");
        }
    }

    private void loadMetrics() {
        metrics = new Metrics(plugin);
    }

    private void checkUpdate() {
        Optional<Configuration> config = ConfigUtil.getConfig();
        if (!config.isPresent()) {
            return;
        }

        SpigotUpdater updater;
        try {
            updater = ServiceLocator.get(SpigotUpdater.class);
        } catch (InstantiationException | IllegalAccessException | ServiceNotFoundException ex) {
            logger.error(ex.getMessage(), ex);
            return;
        }

        if (!config.get().getNode("update", "check").getBoolean(true)) {
            return;
        }

        updater.isUpdateAvailable().thenAccept(v -> {
            if (!v) {
                return;
            }

            try {
                plugin.getServer().getConsoleSender().sendMessage(LogUtil.getHeading() + ChatColor.AQUA + " has an " + ChatColor.GREEN + "update" + ChatColor.AQUA + " available! New version: " + ChatColor.YELLOW + updater.getLatestVersion().get());
            } catch (ExecutionException ex) {
                logger.error(ex.getMessage(), ex);
            } catch (InterruptedException ex) {
                logger.error(ex.getMessage(), ex);
                Thread.currentThread().interrupt();
            }
        });

        try {
            Thread.sleep(60L * 60L * 1000L);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }

        workPool.submit(this::checkUpdate);
    }

    private void unloadHooks() {
        Set<? extends PluginHook> hooks = ServiceLocator.remove(PluginHook.class);
        for (PluginHook hook : hooks) {
            hook.cancel();
        }
    }

    public void unloadServices() { }

    private void log(Level level, String message) {
        plugin.getServer().getLogger().log(level, (isBukkit) ? ChatColor.stripColor(message) : message);
    }
}
