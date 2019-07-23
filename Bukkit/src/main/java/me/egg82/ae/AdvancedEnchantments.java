package me.egg82.ae;

import co.aikar.commands.*;
import co.aikar.taskchain.BukkitTaskChainFactory;
import co.aikar.taskchain.TaskChainFactory;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.SetMultimap;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.commands.AdvancedEnchantmentsCommand;
import me.egg82.ae.enums.Message;
import me.egg82.ae.events.*;
import me.egg82.ae.events.enchants.*;
import me.egg82.ae.events.enchants.block.blockBreak.*;
import me.egg82.ae.events.enchants.entity.entityDamageByEntity.*;
import me.egg82.ae.events.enchants.entity.entityDeath.EntityDeathProficiency;
import me.egg82.ae.events.enchants.entity.entityDeath.EntityDeathRampage;
import me.egg82.ae.events.enchants.entity.entityShootBow.*;
import me.egg82.ae.events.enchants.entity.projectileHit.ProjectileHitEnder;
import me.egg82.ae.events.enchants.entity.projectileHit.ProjectileHitMarkingArrow;
import me.egg82.ae.events.enchants.inventory.inventoryClick.InventoryClickAdherence;
import me.egg82.ae.events.enchants.inventory.inventoryDrag.InventoryDragAdherence;
import me.egg82.ae.events.enchants.inventory.inventoryMoveItem.InventoryMoveItemAdherence;
import me.egg82.ae.events.enchants.player.playerAnimation.PlayerAnimationMirage;
import me.egg82.ae.events.enchants.player.playerFish.PlayerFishProficiency;
import me.egg82.ae.events.enchants.player.playerItemDamage.PlayerItemDamageDecay;
import me.egg82.ae.events.enchants.player.playerItemHeld.PlayerItemHeldStickiness;
import me.egg82.ae.events.enchants.player.playerItemHeld.PlayerItemHeldStickinessCancel;
import me.egg82.ae.extended.Configuration;
import me.egg82.ae.hooks.PlayerAnalyticsHook;
import me.egg82.ae.hooks.PluginHook;
import me.egg82.ae.hooks.ProtocolLibHook;
import me.egg82.ae.hooks.TownyHook;
import me.egg82.ae.services.CollectionProvider;
import me.egg82.ae.services.GameAnalyticsErrorHandler;
import me.egg82.ae.services.PluginMessageFormatter;
import me.egg82.ae.services.block.FakeBlockHandler;
import me.egg82.ae.services.entity.EntityItemHandler;
import me.egg82.ae.tasks.*;
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
import org.bukkit.GameMode;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdvancedEnchantments {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ExecutorService workPool = Executors.newFixedThreadPool(1, new ThreadFactoryBuilder().setNameFormat("AdvancedEnchantments-%d").build());

    private TaskChainFactory taskFactory;
    private PaperCommandManager commandManager;

    private List<EventHolder> eventHolders = new ArrayList<>();
    private List<BukkitEventSubscriber<?>> events = new ArrayList<>();
    private List<Integer> tasks = new ArrayList<>();

    private Metrics metrics = null;

    private final Plugin plugin;
    private final boolean isBukkit;

    private CommandIssuer consoleCommandIssuer = null;

    public AdvancedEnchantments(Plugin plugin) {
        isBukkit = BukkitEnvironmentUtil.getEnvironment() == BukkitEnvironmentUtil.Environment.BUKKIT;
        this.plugin = plugin;
    }

    public void onLoad() {
        if (BukkitEnvironmentUtil.getEnvironment() != BukkitEnvironmentUtil.Environment.PAPER) {
            log(Level.INFO, ChatColor.AQUA + "====================================");
            log(Level.INFO, ChatColor.YELLOW + "AdvancedEnchantments runs better on Paper!");
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

        taskFactory = BukkitTaskChainFactory.create(plugin);
        commandManager = new PaperCommandManager(plugin);
        commandManager.enableUnstableAPI("help");

        consoleCommandIssuer = commandManager.getCommandIssuer(plugin.getServer().getConsoleSender());

        loadLanguages();
        loadServices();
        loadCommands();
        loadEvents();
        loadTasks();
        loadHooks();
        loadMetrics();

        int numEvents = events.size();
        for (EventHolder eventHolder : eventHolders) {
            numEvents += eventHolder.numEvents();
        }

        consoleCommandIssuer.sendInfo(Message.GENERAL__ENABLED);
        consoleCommandIssuer.sendInfo(Message.GENERAL__LOAD,
                "{version}", plugin.getDescription().getVersion(),
                "{commands}", String.valueOf(commandManager.getRegisteredRootCommands().size()),
                "{events}", String.valueOf(numEvents),
                "{tasks}", String.valueOf(tasks.size())
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

        for (EventHolder eventHolder : eventHolders) {
            eventHolder.cancel();
        }
        eventHolders.clear();
        for (BukkitEventSubscriber<?> event : events) {
            event.cancel();
        }
        events.clear();

        unloadHooks();
        unloadServices();

        consoleCommandIssuer.sendInfo(Message.GENERAL__DISABLED);

        GameAnalyticsErrorHandler.close();
    }

    private void loadLanguages() {
        BukkitLocales locales = commandManager.getLocales();

        try {
            for (Locale locale : Locale.getAvailableLocales()) {
                Optional<File> localeFile = LanguageFileUtil.getLanguage(plugin, locale);
                if (localeFile.isPresent()) {
                    commandManager.addSupportedLanguage(locale);
                    locales.loadYamlLanguageFile(localeFile.get(), locale);
                }
            }
        } catch (IOException | InvalidConfigurationException ex) {
            logger.error(ex.getMessage(), ex);
        }

        locales.loadLanguages();
        commandManager.usePerIssuerLocale(true, true);

        commandManager.setFormat(MessageType.ERROR, new PluginMessageFormatter(commandManager, Message.GENERAL__HEADER));
        commandManager.setFormat(MessageType.INFO, new PluginMessageFormatter(commandManager, Message.GENERAL__HEADER));
        commandManager.setFormat(MessageType.ERROR, ChatColor.DARK_RED, ChatColor.YELLOW, ChatColor.AQUA, ChatColor.WHITE);
        commandManager.setFormat(MessageType.INFO, ChatColor.WHITE, ChatColor.YELLOW, ChatColor.AQUA, ChatColor.GREEN, ChatColor.RED, ChatColor.GOLD, ChatColor.BLUE, ChatColor.GRAY);
    }

    private void loadServices() {
        ConfigurationFileUtil.reloadConfig(plugin);

        try {
            ServiceLocator.register(BukkitVersionUtil.getBestMatch(EntityItemHandler.class, BukkitVersionUtil.getGameVersion(), "me.egg82.ae.services.entity", false), false);
        } catch (InstantiationException | IllegalAccessException ex) {
            logger.error(ex.getMessage(), ex);
        }

        try {
            ServiceLocator.register(BukkitVersionUtil.getBestMatch(FakeBlockHandler.class, BukkitVersionUtil.getGameVersion(), "me.egg82.ae.services.block", false), false);
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
        events.add(BukkitEvents.subscribe(plugin, PlayerLoginEvent.class, EventPriority.LOW).handler(e -> new PlayerLoginUpdateNotifyHandler(plugin, commandManager).accept(e)));

        eventHolders.add(new EnchantingTableEvents(plugin));
        eventHolders.add(new AnvilEvents(plugin));
        eventHolders.add(new GrindstoneEvents(plugin));

        eventHolders.add(new AerialEvents(plugin));
        eventHolders.add(new ArtisanEvents(plugin));
        eventHolders.add(new BeheadingEvents(plugin));
        eventHolders.add(new BleedingEvents(plugin));
        eventHolders.add(new BlindingEvents(plugin));
        eventHolders.add(new BurstEvents(plugin));
        eventHolders.add(new ChargingEvents(plugin));
        eventHolders.add(new DisarmingEvents(plugin));
        eventHolders.add(new EnsnaringEvents(plugin));
        eventHolders.add(new ExplosiveEvents(plugin));
        eventHolders.add(new FieryEvents(plugin));
        eventHolders.add(new FreezingEvents(plugin));

        events.add(BukkitEvents.subscribe(plugin, EntityDamageByEntityEvent.class, EventPriority.NORMAL).filter(BukkitEventFilters.ignoreCancelled()).filter(this::townyIgnoreCancelled).handler(e -> new EntityDamageByEntityMarking().accept(e)));
        events.add(BukkitEvents.subscribe(plugin, PlayerAnimationEvent.class, EventPriority.NORMAL).filter(BukkitEventFilters.ignoreCancelled()).filter(e -> e.getAnimationType() == PlayerAnimationType.ARM_SWING).filter(e -> canUseEnchant(e.getPlayer(), "ae.enchant.mirage")).handler(e -> new PlayerAnimationMirage(plugin).accept(e)));
        events.add(BukkitEvents.subscribe(plugin, EntityShootBowEvent.class, EventPriority.NORMAL).filter(BukkitEventFilters.ignoreCancelled()).filter(e -> canUseEnchant(e.getEntity(), "ae.enchant.multishot")).handler(e -> new EntityShootBowMultishot().accept(e)));
        events.add(BukkitEvents.subscribe(plugin, EntityDamageByEntityEvent.class, EventPriority.NORMAL).filter(BukkitEventFilters.ignoreCancelled()).filter(this::townyIgnoreCancelled).filter(e -> e.getDamager() instanceof LivingEntity && e.getEntity() instanceof LivingEntity).filter(e -> canUseEnchant(e.getDamager(), "ae.enchant.poisonous")).handler(e -> new EntityDamageByEntityPoisonous().accept(e)));
        events.add(BukkitEvents.subscribe(plugin, BlockBreakEvent.class, EventPriority.NORMAL).filter(BukkitEventFilters.ignoreCancelled()).filter(e -> canUseEnchant(e.getPlayer(), "ae.enchant.proficiency")).handler(e -> new BlockBreakProficiency().accept(e)));
        events.add(BukkitEvents.subscribe(plugin, EntityDeathEvent.class, EventPriority.NORMAL).filter(e -> e.getEntity().getKiller() != null).filter(e -> canUseEnchant(e.getEntity().getKiller(), "ae.enchant.proficiency")).handler(e -> new EntityDeathProficiency().accept(e)));
        events.add(BukkitEvents.subscribe(plugin, EntityDamageByEntityEvent.class, EventPriority.NORMAL).filter(BukkitEventFilters.ignoreCancelled()).filter(this::townyIgnoreCancelled).filter(e -> e.getDamager() instanceof LivingEntity).filter(e -> canUseEnchant(e.getDamager(), "ae.enchant.rampage")).handler(e -> new EntityDamageByEntityRampage().accept(e)));
        events.add(BukkitEvents.subscribe(plugin, EntityDeathEvent.class, EventPriority.NORMAL).filter(e -> e.getEntity().getKiller() != null).filter(e -> canUseEnchant(e.getEntity().getKiller(), "ae.enchant.rampage")).handler(e -> new EntityDeathRampage().accept(e)));
        events.add(BukkitEvents.subscribe(plugin, BlockBreakEvent.class, EventPriority.NORMAL).filter(BukkitEventFilters.ignoreCancelled()).filter(e -> e.getPlayer().getGameMode() != GameMode.CREATIVE).filter(e -> canUseEnchant(e.getPlayer(), "ae.enchant.smelting")).handler(e -> new BlockBreakSmelting().accept(e))); // This should be registered after artisan & explosive, for compatibility
        events.add(BukkitEvents.subscribe(plugin, BlockBreakEvent.class, EventPriority.NORMAL).filter(BukkitEventFilters.ignoreCancelled()).filter(e -> canUseEnchant(e.getPlayer(), "ae.enchant.stillness")).handler(e -> new BlockBreakStillness().accept(e))); // This should be registered after artisan & explosive, for compatibility
        events.add(BukkitEvents.subscribe(plugin, EntityDamageByEntityEvent.class, EventPriority.NORMAL).filter(BukkitEventFilters.ignoreCancelled()).filter(this::townyIgnoreCancelled).filter(e -> e.getDamager() instanceof LivingEntity).filter(e -> canUseEnchant(e.getDamager(), "ae.enchant.thunderous")).handler(e -> new EntityDamageByEntityThunderous().accept(e)));
        events.add(BukkitEvents.subscribe(plugin, EntityDamageByEntityEvent.class, EventPriority.NORMAL).filter(BukkitEventFilters.ignoreCancelled()).filter(this::townyIgnoreCancelled).filter(e -> e.getDamager() instanceof LivingEntity).filter(e -> canUseEnchant(e.getDamager(), "ae.enchant.tornado")).handler(e -> new EntityDamageByEntityTornado(plugin).accept(e)));
        events.add(BukkitEvents.subscribe(plugin, EntityDamageByEntityEvent.class, EventPriority.NORMAL).filter(BukkitEventFilters.ignoreCancelled()).filter(this::townyIgnoreCancelled).filter(e -> e.getDamager() instanceof LivingEntity).filter(e -> canUseEnchant(e.getDamager(), "ae.enchant.vampiric")).handler(e -> new EntityDamageByEntityVampiric().accept(e)));

        events.add(BukkitEvents.subscribe(plugin, EntityShootBowEvent.class, EventPriority.NORMAL).filter(BukkitEventFilters.ignoreCancelled()).filter(e -> canUseEnchant(e.getEntity(), "ae.enchant.marking")).handler(e -> new EntityShootBowMarking().accept(e)));
        try {
            Class.forName("org.bukkit.event.entity.ProjectileHitEvent");
            events.add(BukkitEvents.subscribe(plugin, ProjectileHitEvent.class, EventPriority.NORMAL).filter(e -> CollectionProvider.getMarkingArrows().containsKey(e.getEntity().getUniqueId())).handler(e -> new ProjectileHitMarkingArrow().accept(e)));
        } catch (ClassNotFoundException ignored) {}
        events.add(BukkitEvents.subscribe(plugin, EntityDamageByEntityEvent.class, EventPriority.NORMAL).filter(e -> CollectionProvider.getMarkingArrows().containsKey(e.getDamager().getUniqueId())).handler(e -> new EntityDamageByEntityMarkingArrow().accept(e)));

        try {
            Class.forName("org.bukkit.event.player.PlayerFishEvent");
            events.add(BukkitEvents.subscribe(plugin, PlayerFishEvent.class, EventPriority.NORMAL).filter(BukkitEventFilters.ignoreCancelled()).handler(e -> new PlayerFishProficiency().accept(e)));
        } catch (ClassNotFoundException ignored) {}

        events.add(BukkitEvents.subscribe(plugin, PlayerItemHeldEvent.class, EventPriority.NORMAL).filter(BukkitEventFilters.ignoreCancelled()).filter(e -> canUseEnchant(e.getPlayer(), "ae.curse.stickiness")).handler(e -> new PlayerItemHeldStickiness().accept(e)));
        events.add(BukkitEvents.subscribe(plugin, PlayerItemHeldEvent.class, EventPriority.LOW).filter(BukkitEventFilters.ignoreCancelled()).filter(e -> CollectionProvider.getStickiness().containsKey(e.getPlayer().getUniqueId())).handler(e -> new PlayerItemHeldStickinessCancel().accept(e)));

        events.add(BukkitEvents.subscribe(plugin, EntityDamageByEntityEvent.class, EventPriority.NORMAL).filter(BukkitEventFilters.ignoreCancelled()).filter(this::townyIgnoreCancelled).filter(e -> e.getEntity() instanceof LivingEntity).filter(e -> canUseEnchant(e.getEntity(), "ae.curse.treason")).handler(e -> new EntityDamageByEntityTreason().accept(e)));

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

        try {
            Class.forName("org.bukkit.event.player.PlayerItemDamageEvent");
            events.add(BukkitEvents.subscribe(plugin, PlayerItemDamageEvent.class, EventPriority.NORMAL).filter(BukkitEventFilters.ignoreCancelled()).filter(e -> canUseEnchant(e.getPlayer(), "ae.curse.decay")).handler(e -> new PlayerItemDamageDecay().accept(e)));
        } catch (ClassNotFoundException ignored) {}

        events.add(BukkitEvents.subscribe(plugin, EntityDamageByEntityEvent.class, EventPriority.NORMAL).filter(BukkitEventFilters.ignoreCancelled()).filter(this::townyIgnoreCancelled).filter(e -> e.getEntity() instanceof LivingEntity).filter(e -> ((LivingEntity) e.getEntity()).getEquipment() != null).filter(e -> canUseEnchant(e.getEntity(), "ae.curse.ender")).handler(e -> new EntityDamageByEntityEnder().accept(e)));
        try {
            Class.forName("org.bukkit.event.entity.ProjectileHitEvent");
            events.add(BukkitEvents.subscribe(plugin, ProjectileHitEvent.class, EventPriority.NORMAL).filter(e -> e.getHitEntity() != null).filter(e -> e.getHitEntity() instanceof LivingEntity).filter(e -> ((LivingEntity) e.getHitEntity()).getEquipment() != null).filter(e -> canUseEnchant(e.getHitEntity(), "ae.curse.ender")).handler(e -> new ProjectileHitEnder().accept(e)));
        } catch (ClassNotFoundException ignored) {}

        events.add(BukkitEvents.subscribe(plugin, EntityDamageByEntityEvent.class, EventPriority.NORMAL).filter(BukkitEventFilters.ignoreCancelled()).filter(this::townyIgnoreCancelled).filter(e -> e.getDamager() instanceof LivingEntity && e.getEntity() instanceof LivingEntity).filter(e -> canUseEnchant(e.getDamager(), "ae.curse.leeching")).handler(e -> new EntityDamageByEntityLeeching().accept(e)));
        events.add(BukkitEvents.subscribe(plugin, EntityDamageByEntityEvent.class, EventPriority.LOW).filter(BukkitEventFilters.ignoreCancelled()).filter(this::townyIgnoreCancelled).filter(e -> e.getDamager() instanceof LivingEntity).filter(e -> canUseEnchant(e.getDamager(), "ae.curse.pacifism")).handler(e -> new EntityDamageByEntityPacifismCancel().accept(e)));
    }

    private boolean canUseEnchant(Object obj, String node) { return !(obj instanceof Player) || ((Player) obj).hasPermission(node); }

    private boolean townyIgnoreCancelled(EntityDamageByEntityEvent event) {
        try {
            TownyHook townyHook = ServiceLocator.get(TownyHook.class);
            return townyHook.ignoreCancelled(event);
        } catch (InstantiationException | IllegalAccessException | ServiceNotFoundException ignored) { return true; }
    }

    private void loadTasks() {
        tasks.add(Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new TaskAntigravity(), 0L, 60L));
        tasks.add(Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new TaskBleeding(), 0L, 20L));
        tasks.add(Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new TaskFreezing(), 0L, 20L));
        tasks.add(Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new TaskMagnetic(), 0L, 3L));
        tasks.add(Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new TaskRepairing(), 0L, 100L));
        tasks.add(Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new TaskWither(), 0L, 40L));
        tasks.add(Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new TaskCalling(), 0L, 40L));
    }

    private void loadHooks() {
        PluginManager manager = plugin.getServer().getPluginManager();

        if (manager.getPlugin("Plan") != null) {
            consoleCommandIssuer.sendInfo(Message.GENERAL__HOOK_ENABLE, "{plugin}", "Plan");
            ServiceLocator.register(new PlayerAnalyticsHook());
        } else {
            consoleCommandIssuer.sendInfo(Message.GENERAL__HOOK_DISABLE, "{plugin}", "Plan");
        }

        if (manager.getPlugin("ProtocolLib") != null) {
            consoleCommandIssuer.sendInfo(Message.GENERAL__HOOK_ENABLE, "{plugin}", "ProtocolLib");
            Set<? extends FakeBlockHandler> handlers = ServiceLocator.remove(FakeBlockHandler.class);
            for (FakeBlockHandler h : handlers) {
                h.removeAll();
            }
            ServiceLocator.register(new ProtocolLibHook(plugin));
        } else {
            consoleCommandIssuer.sendInfo(Message.GENERAL__HOOK_DISABLE, "{plugin}", "ProtocolLib");
        }

        if (manager.getPlugin("Towny") != null) {
            consoleCommandIssuer.sendInfo(Message.GENERAL__HOOK_ENABLE, "{plugin}", "Towny");
            ServiceLocator.register(new TownyHook(manager.getPlugin("Towny")));
        } else {
            consoleCommandIssuer.sendInfo(Message.GENERAL__HOOK_DISABLE, "{plugin}", "Towny");
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
                consoleCommandIssuer.sendInfo(Message.GENERAL__UPDATE, "{version}", updater.getLatestVersion().get());
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
