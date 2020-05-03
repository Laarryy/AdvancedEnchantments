package me.egg82.ae;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import javax.xml.xpath.XPathExpressionException;
import me.egg82.ae.utils.BukkitEnvironmentUtil;
import me.egg82.ae.utils.LogUtil;
import me.lucko.jarrelocator.JarRelocator;
import me.lucko.jarrelocator.Relocation;
import ninja.egg82.maven.Artifact;
import ninja.egg82.maven.Repository;
import ninja.egg82.maven.Scope;
import ninja.egg82.services.ProxiedURLClassLoader;
import ninja.egg82.utils.DownloadUtil;
import ninja.egg82.utils.InjectUtil;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public class BukkitBootstrap extends JavaPlugin {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Object concrete;
    private Class<?> concreteClass;

    private final boolean isBukkit;

    private URLClassLoader proxiedClassLoader;
    private final ExecutorService downloadPool = Executors.newWorkStealingPool(Math.max(4, Runtime.getRuntime().availableProcessors()));

    public BukkitBootstrap() {
        super();
        isBukkit = BukkitEnvironmentUtil.getEnvironment() == BukkitEnvironmentUtil.Environment.BUKKIT;
    }

    @Override
    public void onLoad() {
        proxiedClassLoader = new ProxiedURLClassLoader(getClass().getClassLoader(), new String[] { "org\\.slf4j\\..*" });

        try {
            loadJars(new File(getDataFolder(), "external"), proxiedClassLoader, (URLClassLoader) getClass().getClassLoader());
        } catch (ClassCastException | IOException | IllegalAccessException | InvocationTargetException ex) {
            logger.error(ex.getMessage(), ex);
            throw new RuntimeException("Could not load required deps.");
        }

        downloadPool.shutdown();
        try {
            if (!downloadPool.awaitTermination(1L, TimeUnit.HOURS)) {
                logger.error("Could not download all dependencies. Please try again later.");
                return;
            }
        } catch (InterruptedException ex) {
            logger.error(ex.getMessage(), ex);
            Thread.currentThread().interrupt();
        }

        try {
            concreteClass = proxiedClassLoader.loadClass("me.egg82.ae.AdvancedEnchantments");
            concrete = concreteClass.getDeclaredConstructor(Plugin.class).newInstance(this);
            concreteClass.getMethod("onLoad").invoke(concrete);
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            logger.error(ex.getMessage(), ex);
            throw new RuntimeException("Could not create main class.");
        }
    }

    @Override
    public void onEnable() {
        try {
            concreteClass.getMethod("onEnable").invoke(concrete);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            logger.error(ex.getMessage(), ex);
            throw new RuntimeException("Could not invoke onEnable.");
        }
    }

    @Override
    public void onDisable() {
        try {
            concreteClass.getMethod("onDisable").invoke(concrete);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            logger.error(ex.getMessage(), ex);
            throw new RuntimeException("Could not invoke onDisable.");
        }
    }

    private void loadJars(File jarsDir, URLClassLoader classLoader, URLClassLoader parentLoader) throws IOException, IllegalAccessException, InvocationTargetException {
        if (jarsDir.exists() && !jarsDir.isDirectory()) {
            Files.delete(jarsDir.toPath());
        }
        if (!jarsDir.exists()) {
            if (!jarsDir.mkdirs()) {
                throw new IOException("Could not create parent directory structure.");
            }
        }

        File cacheDir = new File(jarsDir, "cache");

        // First

        Artifact.Builder guava = Artifact.builder("com.google.guava", "guava", "${guava.version}", cacheDir)
                .addRepository(Repository.builder("https://repo1.maven.org/maven2/").addProxy("https://nexus.egg82.me/repository/maven-central/").build());
        buildInjectWait(guava, jarsDir, classLoader, "Google Guava", 1);

        // Same file

        InjectUtil.injectFile(getFile(), classLoader);

        // Local

        Artifact.Builder taskchainBukkit = Artifact.builder("co.aikar", "taskchain-bukkit", "${taskchain.version}", cacheDir)
                .addRepository(Repository.builder("https://repo.aikar.co/nexus/content/groups/aikar/").addProxy("https://nexus.egg82.me/repository/aikar/").build())
                .addRepository(Repository.builder("https://repo1.maven.org/maven2/").addProxy("https://nexus.egg82.me/repository/maven-central/").build());
        buildInject(taskchainBukkit, jarsDir, classLoader, "Taskchain", 1);

        printLatest("ACF");
        Artifact.Builder acfPaper = Artifact.builder("co.aikar", "acf-paper", "${acf.version}", cacheDir)
                .addDirectJarURL("https://nexus.egg82.me/repository/aikar/{GROUP}/{ARTIFACT}/{VERSION}/{ARTIFACT}-{SNAPSHOT}-shaded.jar")
                .addDirectJarURL("https://repo.aikar.co/nexus/content/groups/aikar/{GROUP}/{ARTIFACT}/{VERSION}/{ARTIFACT}-{SNAPSHOT}-shaded.jar")
                .addRepository(Repository.builder("https://repo.aikar.co/nexus/content/groups/aikar/").addProxy("https://nexus.egg82.me/repository/aikar/").build())
                .addRepository(Repository.builder("https://repo1.maven.org/maven2/").addProxy("https://nexus.egg82.me/repository/maven-central/").build());
        buildInject(acfPaper, jarsDir, classLoader, "ACF");

        Artifact.Builder eventChainBukkit = Artifact.builder("ninja.egg82", "event-chain-bukkit", "${eventchain.version}", cacheDir)
                .addRepository(Repository.builder("https://nexus.egg82.me/repository/maven-releases/").build())
                .addRepository(Repository.builder("https://repo1.maven.org/maven2/").addProxy("https://nexus.egg82.me/repository/maven-central/").build());
        buildInject(eventChainBukkit, jarsDir, classLoader, "Event Chain");

        Artifact.Builder configurateYaml = Artifact.builder("org.spongepowered", "configurate-yaml", "${configurate.version}", cacheDir)
                .addRepository(Repository.builder("https://repo.spongepowered.org/maven/").addProxy("https://nexus.egg82.me/repository/sponge/").build())
                .addRepository(Repository.builder("https://repo1.maven.org/maven2/").addProxy("https://nexus.egg82.me/repository/maven-central/").build());
        buildInject(configurateYaml, jarsDir, classLoader, "Configurate", 2);

        Artifact.Builder spigotUpdater = Artifact.builder("ninja.egg82", "spigot-updater", "${updater.version}", cacheDir)
                .addRepository(Repository.builder("https://nexus.egg82.me/repository/maven-releases/").build())
                .addRepository(Repository.builder("https://repo1.maven.org/maven2/").addProxy("https://nexus.egg82.me/repository/maven-central/").build());
        buildInject(spigotUpdater, jarsDir, classLoader, "Spigot Updater");

        Artifact.Builder serviceLocator = Artifact.builder("ninja.egg82", "service-locator", "${servicelocator.version}", cacheDir)
                .addRepository(Repository.builder("https://nexus.egg82.me/repository/maven-releases/").build())
                .addRepository(Repository.builder("https://repo1.maven.org/maven2/").addProxy("https://nexus.egg82.me/repository/maven-central/").build());
        buildInject(serviceLocator, jarsDir, classLoader, "Service Locator");

        Artifact.Builder javassist = Artifact.builder("org.javassist", "javassist", "3.26.0-GA", cacheDir)
                .addRepository(Repository.builder("https://repo1.maven.org/maven2/").addProxy("https://nexus.egg82.me/repository/maven-central/").build());
        buildInject(javassist, jarsDir, classLoader, "Javassist");

        Artifact.Builder javaxAnnotationApi = Artifact.builder("javax.annotation", "javax.annotation-api", "1.3.2", cacheDir)
                .addRepository(Repository.builder("https://repo1.maven.org/maven2/").addProxy("https://nexus.egg82.me/repository/maven-central/").build());
        buildInject(javaxAnnotationApi, jarsDir, classLoader, "Javax Annotations");

        Artifact.Builder reflectionUtils = Artifact.builder("ninja.egg82", "reflection-utils", "${reflectionutils.version}", cacheDir)
                .addRepository(Repository.builder("https://nexus.egg82.me/repository/maven-releases/").build())
                .addRepository(Repository.builder("https://repo1.maven.org/maven2/").addProxy("https://nexus.egg82.me/repository/maven-central/").build());
        buildInject(reflectionUtils, jarsDir, classLoader, "Reflection Utils");

        Artifact.Builder mineskinJavaClient = Artifact.builder("org.mineskin", "java-client", "${mineskin.version}", cacheDir)
                .addRepository(Repository.builder("https://repo.inventivetalent.org/content/groups/public/").addProxy("https://nexus.egg82.me/repository/inventivetalent/").build())
                .addRepository(Repository.builder("https://repo1.maven.org/maven2/").addProxy("https://nexus.egg82.me/repository/maven-central/").build());
        buildInject(mineskinJavaClient, jarsDir, classLoader, "Mineskin Client");

        printLatest("PacketWrapper");
        Artifact.Builder packetWrapper = Artifact.builder("com.comphenix.packetwrapper", "PacketWrapper", "${packetwrapper.version}", cacheDir)
                .addRepository(Repository.builder("http://repo.dmulloy2.net/nexus/repository/snapshots/").addProxy("https://nexus.egg82.me/repository/dmulloy2-snapshots/").build())
                .addRepository(Repository.builder("https://repo1.maven.org/maven2/").addProxy("https://nexus.egg82.me/repository/maven-central/").build());
        buildInject(packetWrapper, jarsDir, classLoader, "PacketWrapper");

        Artifact.Builder effectLib = Artifact.builder("de.slikey", "EffectLib", "${effectlib.version}", cacheDir)
                .addRepository(Repository.builder("http://maven.elmakers.com/repository/").addProxy("https://nexus.egg82.me/repository/elmakers/").build())
                .addRepository(Repository.builder("https://repo1.maven.org/maven2/").addProxy("https://nexus.egg82.me/repository/maven-central/").build());
        buildInject(effectLib, jarsDir, classLoader, "EffectLib");

        Artifact.Builder expiringMap = Artifact.builder("net.jodah", "expiringmap", "${expiringmap.version}", cacheDir)
                .addRepository(Repository.builder("https://repo1.maven.org/maven2/").addProxy("https://nexus.egg82.me/repository/maven-central/").build());
        buildInject(expiringMap, jarsDir, classLoader, "ExpiringMap");

        // Global

        Artifact.Builder caffeine = Artifact.builder("com.github.ben-manes.caffeine", "caffeine", "${caffeine.version}", cacheDir)
                .addRepository(Repository.builder("https://repo1.maven.org/maven2/").addProxy("https://nexus.egg82.me/repository/maven-central/").build());
        buildRelocateInject(caffeine, jarsDir, Collections.singletonList(new Relocation("com.github.benmanes.caffeine", "me.egg82.ae.external.com.github.benmanes.caffeine")), classLoader, "Caffeine");
        buildRelocateInject(caffeine, jarsDir, Collections.singletonList(new Relocation("com.github.benmanes.caffeine", "me.egg82.ae.external.com.github.benmanes.caffeine")), parentLoader, "Caffeine");

        Artifact.Builder gameanalyticsApi = Artifact.builder("ninja.egg82", "gameanalytics-api", "${gameanalytics.version}", cacheDir)
                .addRepository(Repository.builder("https://nexus.egg82.me/repository/maven-releases/").build())
                .addRepository(Repository.builder("https://repo1.maven.org/maven2/").addProxy("https://nexus.egg82.me/repository/maven-central/").build());
        buildInject(gameanalyticsApi, jarsDir, classLoader, "GameAnalytics API", 1);

        Artifact.Builder abstractConfiguration = Artifact.builder("ninja.egg82", "abstract-configuration", "${abstractconfiguration.version}", cacheDir)
                .addRepository(Repository.builder("https://nexus.egg82.me/repository/maven-releases/").build())
                .addRepository(Repository.builder("https://repo1.maven.org/maven2/").addProxy("https://nexus.egg82.me/repository/maven-central/").build());
        buildInject(abstractConfiguration, jarsDir, classLoader, "Abstract Configuration");
    }

    private void printLatest(String friendlyName) {
        log(Level.INFO, LogUtil.getHeading() + ChatColor.YELLOW + "Checking version of " + ChatColor.WHITE + friendlyName);
    }

    private void buildInject(Artifact.Builder builder, File jarsDir, URLClassLoader classLoader, String friendlyName) {
        buildInject(builder, jarsDir, classLoader, friendlyName, 0);
    }

    private void buildInject(Artifact.Builder builder, File jarsDir, URLClassLoader classLoader, String friendlyName, int depth) {
        downloadPool.submit(() -> buildInjectWait(builder, jarsDir, classLoader, friendlyName, depth));
    }

    private void buildInjectWait(Artifact.Builder builder, File jarsDir, URLClassLoader classLoader, String friendlyName, int depth) {
        Exception lastEx = null;
        try {
            injectArtifact(builder.build(), jarsDir, classLoader, friendlyName, depth, null);
            return;
        } catch (IOException ex) {
            lastEx = ex;
        } catch (IllegalAccessException | InvocationTargetException | URISyntaxException | XPathExpressionException | SAXException ex) {
            logger.error(ex.getMessage(), ex);
            return;
        }

        if (depth > 0) {
            logger.error(lastEx.getMessage(), lastEx);
            return;
        }

        try {
            injectArtifact(builder, jarsDir, classLoader, null);
        } catch (IOException | IllegalAccessException | InvocationTargetException ex) {
            logger.error(lastEx.getMessage(), lastEx);
        }
    }

    private void buildRelocateInject(Artifact.Builder builder, File jarsDir, List<Relocation> rules, URLClassLoader classLoader, String friendlyName) {
        buildRelocateInject(builder, jarsDir, rules, classLoader, friendlyName, 0);
    }

    private void buildRelocateInject(Artifact.Builder builder, File jarsDir, List<Relocation> rules, URLClassLoader classLoader, String friendlyName, int depth) {
        downloadPool.submit(() -> buildRelocateInjectWait(builder, jarsDir, rules, classLoader, friendlyName, depth));
    }

    private void buildRelocateInjectWait(Artifact.Builder builder, File jarsDir, List<Relocation> rules, URLClassLoader classLoader, String friendlyName, int depth) {
        Exception lastEx = null;
        try {
            injectArtifact(builder.build(), jarsDir, classLoader, friendlyName, depth, rules);
            return;
        } catch (IOException ex) {
            lastEx = ex;
        } catch (IllegalAccessException | InvocationTargetException | URISyntaxException | XPathExpressionException | SAXException ex) {
            logger.error(ex.getMessage(), ex);
            return;
        }

        if (depth > 0) {
            logger.error(lastEx.getMessage(), lastEx);
            return;
        }

        try {
            injectArtifact(builder, jarsDir, classLoader, rules);
        } catch (IOException | IllegalAccessException | InvocationTargetException ex) {
            logger.error(lastEx.getMessage(), lastEx);
        }
    }

    private void injectArtifact(Artifact artifact, File jarsDir, URLClassLoader classLoader, String friendlyName, int depth, List<Relocation> rules) throws IOException, IllegalAccessException, InvocationTargetException, URISyntaxException, XPathExpressionException, SAXException {
        File output = new File(jarsDir, artifact.getGroupId()
                + "-" + artifact.getArtifactId()
                + "-" + artifact.getRealVersion() + ".jar"
        );

        if (friendlyName != null && !artifact.fileExists(output)) {
            log(Level.INFO, LogUtil.getHeading() + ChatColor.YELLOW + "Downloading " + ChatColor.WHITE + friendlyName);
        }

        if (rules == null) {
            artifact.injectJar(output, classLoader);
        } else {
            if (!DownloadUtil.hasFile(output)) {
                artifact.downloadJar(output);
            }
            File relocatedOutput = new File(jarsDir, artifact.getGroupId()
                    + "-" + artifact.getArtifactId()
                    + "-" + artifact.getRealVersion() + "-relocated.jar"
            );
            if (!DownloadUtil.hasFile(relocatedOutput)) {
                JarRelocator relocator = new JarRelocator(output, relocatedOutput, rules);
                relocator.run();
            }
            InjectUtil.injectFile(relocatedOutput, classLoader);
        }

        if (depth > 0) {
            for (Artifact dependency : artifact.getDependencies()) {
                if (dependency.getScope() == Scope.COMPILE || dependency.getScope() == Scope.RUNTIME) {
                    injectArtifact(dependency, jarsDir, classLoader, null, depth - 1, rules);
                }
            }
        }
    }

    private void injectArtifact(Artifact.Builder builder, File jarsDir, URLClassLoader classLoader, List<Relocation> rules) throws IOException, IllegalAccessException, InvocationTargetException {
        File[] files = jarsDir.listFiles();
        if (files == null) {
            throw new IOException();
        }

        long latest = Long.MIN_VALUE;
        File retVal = null;
        for (File file : files) {
            if (file.getName().startsWith(builder.getGroupId() + "-" + builder.getArtifactId()) && file.lastModified() >= latest) {
                latest = file.lastModified();
                retVal = file;
            }
        }

        if (retVal == null) {
            throw new IOException();
        }

        if (rules == null) {
            InjectUtil.injectFile(retVal, classLoader);
        } else {
            File output = new File(jarsDir, retVal.getName().substring(0, retVal.getName().length() - 4) + "-relocated.jar");
            if (!DownloadUtil.hasFile(output)) {
                JarRelocator relocator = new JarRelocator(retVal, output, rules);
                relocator.run();
            }
            InjectUtil.injectFile(output, classLoader);
        }
    }

    private void log(Level level, String message) {
        getServer().getLogger().log(level, (isBukkit) ? ChatColor.stripColor(message) : message);
    }
}
