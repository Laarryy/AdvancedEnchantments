package me.egg82.ae;

import co.aikar.commands.PaperCommandManager;
import co.aikar.commands.RegisteredCommand;
import co.aikar.taskchain.BukkitTaskChainFactory;
import co.aikar.taskchain.TaskChainFactory;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.SetMultimap;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.commands.AdvancedEnchantmentsCommand;
import me.egg82.ae.events.PlayerLoginUpdateNotifyHandler;
import me.egg82.ae.events.enchants.block.blockBreak.BlockBreakExplosive;
import me.egg82.ae.events.enchants.block.blockBreak.BlockBreakFreezingCancel;
import me.egg82.ae.events.enchants.block.blockPlace.BlockPlaceFreezingCancel;
import me.egg82.ae.events.enchants.enchantment.enchantItem.EnchantItemReplace;
import me.egg82.ae.events.enchants.entity.entityDamageByEntity.*;
import me.egg82.ae.events.enchants.entity.entityDeath.EntityDeathBeheading;
import me.egg82.ae.events.enchants.entity.entityShootBow.EntityShootBowFiery;
import me.egg82.ae.events.enchants.entity.entityShootBow.EntityShootBowFreezingCancel;
import me.egg82.ae.events.enchants.entity.projectileHit.ProjectileHitFiery;
import me.egg82.ae.events.enchants.inventory.inventoryClick.InventoryClickAdherence;
import me.egg82.ae.events.enchants.inventory.inventoryDrag.InventoryDragAdherence;
import me.egg82.ae.events.enchants.inventory.inventoryMoveItem.InventoryMoveItemAdherence;
import me.egg82.ae.events.enchants.player.playerMove.PlayerMoveFreezingCancel;
import me.egg82.ae.events.enchants.player.playerTeleport.PlayerTeleportFreezingCancel;
import me.egg82.ae.extended.Configuration;
import me.egg82.ae.hooks.PlayerAnalyticsHook;
import me.egg82.ae.hooks.PluginHook;
import me.egg82.ae.services.CollectionProvider;
import me.egg82.ae.services.GameAnalyticsErrorHandler;
import me.egg82.ae.services.entity.EntityItemHandler;
import me.egg82.ae.tasks.TaskBleeding;
import me.egg82.ae.tasks.TaskFreezing;
import me.egg82.ae.utils.*;
import ninja.egg82.events.BukkitEventFilters;
import ninja.egg82.events.BukkitEventSubscriber;
import ninja.egg82.events.BukkitEvents;
import ninja.egg82.service.ServiceLocator;
import ninja.egg82.service.ServiceNotFoundException;
import ninja.egg82.updater.SpigotUpdater;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdvancedEnchantments {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ExecutorService workPool = Executors.newFixedThreadPool(1, new ThreadFactoryBuilder().setNameFormat("AdvancedEnchantments-%d").build());

    private TaskChainFactory taskFactory;
    private PaperCommandManager commandManager;

    private List<BukkitEventSubscriber<?>> events = new ArrayList<>();
    private List<Integer> tasks = new ArrayList<>();

    private Metrics metrics = null;

    private final Plugin plugin;
    private final boolean isBukkit;

    public AdvancedEnchantments(Plugin plugin) {
        isBukkit = Bukkit.getName().equals("Bukkit") || Bukkit.getName().equals("CraftBukkit");
        this.plugin = plugin;
    }

    public void onLoad() {
        if (!Bukkit.getName().equals("Paper") && !Bukkit.getName().equals("PaperSpigot")) {
            log(Level.INFO, ChatColor.AQUA + "====================================");
            log(Level.INFO, ChatColor.YELLOW + "AdvancedEnchantments runs better on Paper!");
            log(Level.INFO, ChatColor.YELLOW + "https://whypaper.emc.gs/");
            log(Level.INFO, ChatColor.AQUA + "====================================");
        }

        if (Bukkit.getBukkitVersion().startsWith("1.8") || Bukkit.getBukkitVersion().startsWith("1.8.8")) {
            log(Level.INFO, ChatColor.AQUA + "====================================");
            log(Level.INFO, ChatColor.DARK_RED + "DEAR LORD why are you on 1.8???");
            log(Level.INFO, ChatColor.DARK_RED + "Have you tried ViaVersion or ProtocolSupport lately?");
            log(Level.INFO, ChatColor.AQUA + "====================================");
        }
    }

    public void onEnable() {
        GameAnalyticsErrorHandler.open(ServerIDUtil.getID(new File(plugin.getDataFolder(), "stats-id.txt")), plugin.getDescription().getVersion(), Bukkit.getVersion());

        taskFactory = BukkitTaskChainFactory.create(plugin);
        commandManager = new PaperCommandManager(plugin);
        commandManager.enableUnstableAPI("help");

        loadServices();
        loadCommands();
        loadEvents();
        loadTasks();
        loadHooks();
        loadMetrics();

        plugin.getServer().getConsoleSender().sendMessage(LogUtil.getHeading() + ChatColor.GREEN + "Enabled");

        plugin.getServer().getConsoleSender().sendMessage(LogUtil.getHeading()
                + ChatColor.YELLOW + "[" + ChatColor.AQUA + "Version " + ChatColor.WHITE + plugin.getDescription().getVersion() + ChatColor.YELLOW +  "] "
                + ChatColor.YELLOW + "[" + ChatColor.WHITE + commandManager.getRegisteredRootCommands().size() + ChatColor.GOLD + " Commands" + ChatColor.YELLOW +  "] "
                + ChatColor.YELLOW + "[" + ChatColor.WHITE + tasks.size() + ChatColor.GRAY + " Tasks" + ChatColor.YELLOW +  "] "
                + ChatColor.YELLOW + "[" + ChatColor.WHITE + events.size() + ChatColor.BLUE + " Events" + ChatColor.YELLOW +  "]"
        );

        workPool.submit(this::checkUpdate);
    }

    public void onDisable() {
        taskFactory.shutdown(8, TimeUnit.SECONDS);
        commandManager.unregisterCommands();

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

    private void loadCommands() {
        commandManager.getCommandCompletions().registerCompletion("enchant", c -> {
            String lower = c.getInput().toLowerCase().replace(" ", "_");
            Set<String> enchants = new LinkedHashSet<>();
            for (Enchantment e : Enchantment.values()) {
                if (e.getName().toLowerCase().startsWith(lower)) {
                    enchants.add(e.getName());
                }
            }
            for (AdvancedEnchantment e : AdvancedEnchantment.values()) {
                if (e.getName().toLowerCase().startsWith(lower)) {
                    enchants.add(e.getName());
                }
            }
            return ImmutableList.copyOf(enchants);
        });

        commandManager.getCommandCompletions().registerCompletion("subcommand", c -> {
            String lower = c.getInput().toLowerCase();
            Set<String> commands = new LinkedHashSet<>();
            SetMultimap<String, RegisteredCommand> subcommands = commandManager.getRootCommand("advancedenchantments").getSubCommands();
            for (Map.Entry<String, RegisteredCommand> kvp : subcommands.entries()) {
                if (!kvp.getValue().isPrivate() && (lower.isEmpty() || kvp.getKey().toLowerCase().startsWith(lower)) && kvp.getValue().getCommand().indexOf(' ') == -1) {
                    commands.add(kvp.getValue().getCommand());
                }
            }
            return ImmutableList.copyOf(commands);
        });

        commandManager.registerCommand(new AdvancedEnchantmentsCommand(plugin, taskFactory));
    }

    private void loadEvents() {
        events.add(BukkitEvents.subscribe(plugin, PlayerLoginEvent.class, EventPriority.LOW).handler(e -> new PlayerLoginUpdateNotifyHandler(plugin).accept(e)));
        events.add(BukkitEvents.subscribe(plugin, EnchantItemEvent.class, EventPriority.NORMAL).filter(BukkitEventFilters.ignoreCancelled()).handler(e -> new EnchantItemReplace().accept(e)));

        events.add(BukkitEvents.subscribe(plugin, EntityDamageByEntityEvent.class, EventPriority.NORMAL).filter(BukkitEventFilters.ignoreCancelled()).filter(e -> !e.getDamager().isOnGround()).filter(e -> e.getDamager() instanceof LivingEntity).handler(e -> new EntityDamageByEntityAerial().accept(e)));
        events.add(BukkitEvents.subscribe(plugin, EntityDeathEvent.class, EventPriority.NORMAL).filter(BukkitEventFilters.ignoreCancelled()).filter(e -> e.getEntity().getKiller() != null).handler(e -> new EntityDeathBeheading(plugin).accept(e)));
        events.add(BukkitEvents.subscribe(plugin, EntityDamageByEntityEvent.class, EventPriority.NORMAL).filter(BukkitEventFilters.ignoreCancelled()).filter(e -> e.getDamager() instanceof LivingEntity).handler(e -> new EntityDamageByEntityBleeding().accept(e)));
        events.add(BukkitEvents.subscribe(plugin, EntityDamageByEntityEvent.class, EventPriority.NORMAL).filter(BukkitEventFilters.ignoreCancelled()).filter(e -> e.getDamager() instanceof Player).filter(e -> ((Player) e.getDamager()).isSprinting()).handler(e -> new EntityDamageByEntityCharging().accept(e)));
        events.add(BukkitEvents.subscribe(plugin, EntityDamageByEntityEvent.class, EventPriority.NORMAL).filter(BukkitEventFilters.ignoreCancelled()).filter(e -> e.getDamager() instanceof LivingEntity && e.getEntity() instanceof LivingEntity).handler(e -> new EntityDamageByEntityDisarming().accept(e)));
        events.add(BukkitEvents.subscribe(plugin, BlockBreakEvent.class, EventPriority.NORMAL).filter(BukkitEventFilters.ignoreCancelled()).filter(e -> !CollectionProvider.getExplosive().contains(e.getBlock().getLocation())).handler(e -> new BlockBreakExplosive().accept(e)));

        events.add(BukkitEvents.subscribe(plugin, EntityShootBowEvent.class, EventPriority.NORMAL).filter(BukkitEventFilters.ignoreCancelled()).handler(e -> new EntityShootBowFiery().accept(e)));
        try {
            Class.forName("org.bukkit.event.entity.ProjectileHitEvent");
            events.add(BukkitEvents.subscribe(plugin, ProjectileHitEvent.class, EventPriority.NORMAL).filter(e -> CollectionProvider.getFiery().remove(e.getEntity().getUniqueId())).handler(e -> new ProjectileHitFiery().accept(e)));
        } catch (ClassNotFoundException ignored) {}
        events.add(BukkitEvents.subscribe(plugin, EntityDamageByEntityEvent.class, EventPriority.NORMAL).filter(e -> CollectionProvider.getFiery().remove(e.getDamager().getUniqueId())).filter(BukkitEventFilters.ignoreCancelled()).handler(e -> new EntityDamageByEntityFiery().accept(e)));

        events.add(BukkitEvents.subscribe(plugin, EntityDamageByEntityEvent.class, EventPriority.NORMAL).filter(BukkitEventFilters.ignoreCancelled()).filter(e -> e.getDamager() instanceof LivingEntity).handler(e -> new EntityDamageByEntityFreezing().accept(e)));
        events.add(BukkitEvents.subscribe(plugin, EntityDamageByEntityEvent.class, EventPriority.LOW).filter(BukkitEventFilters.ignoreCancelled()).filter(e -> CollectionProvider.getFreezing().containsKey(e.getDamager().getUniqueId())).handler(e -> new EntityDamageByEntityFreezingCancel().accept(e)));
        events.add(BukkitEvents.subscribe(plugin, EntityShootBowEvent.class, EventPriority.LOW).filter(BukkitEventFilters.ignoreCancelled()).filter(e -> CollectionProvider.getFreezing().containsKey(e.getEntity().getUniqueId())).handler(e -> new EntityShootBowFreezingCancel().accept(e)));
        events.add(BukkitEvents.subscribe(plugin, PlayerMoveEvent.class, EventPriority.LOW).filter(BukkitEventFilters.ignoreCancelled()).filter(e -> CollectionProvider.getFreezing().containsKey(e.getPlayer().getUniqueId())).handler(e -> new PlayerMoveFreezingCancel().accept(e)));
        events.add(BukkitEvents.subscribe(plugin, PlayerTeleportEvent.class, EventPriority.LOW).filter(BukkitEventFilters.ignoreCancelled()).filter(e -> CollectionProvider.getFreezing().containsKey(e.getPlayer().getUniqueId())).handler(e -> new PlayerTeleportFreezingCancel().accept(e)));
        events.add(BukkitEvents.subscribe(plugin, BlockBreakEvent.class, EventPriority.LOW).filter(BukkitEventFilters.ignoreCancelled()).filter(e -> CollectionProvider.getFreezing().containsKey(e.getPlayer().getUniqueId())).handler(e -> new BlockBreakFreezingCancel().accept(e)));
        events.add(BukkitEvents.subscribe(plugin, BlockPlaceEvent.class, EventPriority.LOW).filter(BukkitEventFilters.ignoreCancelled()).filter(e -> CollectionProvider.getFreezing().containsKey(e.getPlayer().getUniqueId())).handler(e -> new BlockPlaceFreezingCancel().accept(e)));

        events.add(BukkitEvents.subscribe(plugin, InventoryClickEvent.class, EventPriority.NORMAL).filter(BukkitEventFilters.ignoreCancelled()).filter(e -> !e.getWhoClicked().hasPermission("ae.admin")).handler(e -> new InventoryClickAdherence().accept(e)));
        events.add(BukkitEvents.subscribe(plugin, InventoryDragEvent.class, EventPriority.NORMAL).filter(BukkitEventFilters.ignoreCancelled()).filter(e -> !e.getWhoClicked().hasPermission("ae.admin")).handler(e -> new InventoryDragAdherence().accept(e)));
        events.add(BukkitEvents.subscribe(plugin, InventoryMoveItemEvent.class, EventPriority.NORMAL).filter(BukkitEventFilters.ignoreCancelled()).filter(e -> {
            if (e.getSource().getViewers().isEmpty()) {
                return false;
            }
            if (e.getSource().getViewers().get(0).hasPermission("ae.admin")) {
                return true;
            }
            return false;
        }).handler(e -> new InventoryMoveItemAdherence().accept(e)));
    }

    private void loadTasks() {
        tasks.add(Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new TaskBleeding(), 0L, 20L));
        tasks.add(Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new TaskFreezing(), 0L, 20L));
    }

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
