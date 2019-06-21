package me.egg82.ae.events.enchants.entity.entityDeath;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import me.egg82.ae.APIException;
import me.egg82.ae.EnchantAPI;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.BukkitEnchantableItem;
import me.egg82.ae.api.GenericEnchantableItem;
import me.egg82.ae.services.entity.EntityItemHandler;
import me.egg82.ae.services.lookup.PlayerInfo;
import me.egg82.ae.services.lookup.PlayerLookup;
import me.egg82.ae.services.material.MaterialLookup;
import me.egg82.ae.services.skin.SkinLookup;
import ninja.egg82.service.ServiceLocator;
import ninja.egg82.service.ServiceNotFoundException;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Guardian;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityDeathBeheading implements Consumer<EntityDeathEvent> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Plugin plugin;
    private EnchantAPI api = EnchantAPI.getInstance();

    private static Material skullMaterial;

    static {
        Optional<Material> m = MaterialLookup.get("PLAYER_HEAD", "SKULL_ITEM");
        if (!m.isPresent()) {
            throw new RuntimeException("Could not get skull material.");
        }
        skullMaterial = m.get();
    }

    public EntityDeathBeheading(Plugin plugin) {
        this.plugin = plugin;
    }

    public void accept(EntityDeathEvent event) {
        EntityItemHandler entityItemHandler;
        try {
            entityItemHandler = ServiceLocator.get(EntityItemHandler.class);
        } catch (InstantiationException | IllegalAccessException | ServiceNotFoundException ex) {
            logger.error(ex.getMessage(), ex);
            return;
        }

        Optional<ItemStack> mainHand = entityItemHandler.getItemInMainHand(event.getEntity().getKiller());
        Optional<ItemStack> offHand = entityItemHandler.getItemInOffHand(event.getEntity().getKiller());

        GenericEnchantableItem enchantableMainHand = mainHand.isPresent() ? BukkitEnchantableItem.fromItemStack(mainHand.get()) : null;
        GenericEnchantableItem enchantableOffHand = offHand.isPresent() ? BukkitEnchantableItem.fromItemStack(offHand.get()) : null;

        int level;
        try {
            level = api.getMaxLevel(AdvancedEnchantment.BEHEADING, enchantableMainHand, enchantableOffHand);
        } catch (APIException ex) {
            logger.error(ex.getMessage(), ex);
            return;
        }

        if (level < 0) {
            return;
        }

        if (Math.random() > 0.33 * level) {
            return;
        }

        ItemStack skull;

        EntityType type = event.getEntityType();
        if (type == EntityType.PLAYER) {
            skull = getPlayerSkull(((Player) event.getEntity()).getDisplayName(), event.getEntity().getUniqueId());
        } else if (type == EntityType.BAT) {
            skull = getPlayerSkull("Bat Head", "Bat");
        } else if (type == EntityType.BLAZE) {
            skull = getPlayerSkull("Blaze Head", "MHF_Blaze");
        } else if (type == EntityType.CAVE_SPIDER) {
            skull = getPlayerSkull("Cave Spider Head", "MHF_CaveSpider");
        } else if (type == EntityType.CHICKEN) {
            skull = getPlayerSkull("Chicken Head", "MHF_Chicken");
        } else if (type == EntityType.COW) {
            skull = getPlayerSkull("Cow Head", "MHF_Cow");
        } else if (type == EntityType.CREEPER) {
            skull = createSkull((short) 4);
        } else if (type.name().equals("DOLPHIN")) {
            skull = getPlayerSkull("Dolphin Head", "MHF_Dolphin");
        } else if (type.name().equals("DONKEY")) {
            skull = getPlayerSkull("Donkey Head", "Donkey");
        } else if (type.name().equals("DROWNED")) {
            skull = getPlayerSkull("Drowned Head", "MHF_Drowned");
        } else if (type.name().equals("ELDER_GUARDIAN")) {
            skull = getPlayerSkull("Elder Guardian Head", "ElderGuardian");
        } else if (type == EntityType.ENDER_DRAGON) {
            skull = createSkull((short) 5);
        } else if (type == EntityType.ENDERMAN) {
            skull = getPlayerSkull("Enderman Head", "MHF_Enderman");
        } else if (type == EntityType.ENDERMITE) {
            skull = getPlayerSkull("Endermite Head", "MHF_EnderMite");
        } else if (type.name().equals("EVOKER")) {
            skull = getPlayerSkull("Evoker Head", "MHF_Evoker");
        } else if (type == EntityType.GHAST) {
            skull = getPlayerSkull("Ghast Head", "MHF_Ghast");
        } else if (type == EntityType.GIANT) {
            skull = createSkull((short) 2);
        } else if (type == EntityType.GUARDIAN) {
            skull = (((Guardian) event.getEntity()).isElder()) ? getPlayerSkull("Elder Guardian Head", "ElderGuardian") : getPlayerSkull("Guardian Head", "MHF_Guardian");
        } else if (type == EntityType.HORSE) {
            skull = getPlayerSkull("Horse Head", "Door");
        } else if (type.name().equals("HUSK")) {
            skull = getPlayerSkull("Husk Head", "Chuzume");
        } else if (type.name().equals("ILLUSIONER")) {
            skull = getPlayerSkull("Illusioner Head", "MHF_Question");
        } else if (type == EntityType.IRON_GOLEM) {
            skull = getPlayerSkull("Iron Golem Head", "MHF_Golem");
        } else if (type.name().equals("LLAMA")) {
            skull = getPlayerSkull("Llama Head", "MHF_Question");
        } else if (type == EntityType.MAGMA_CUBE) {
            skull = getPlayerSkull("Magma Cube Head", "MHF_LavaSlime");
        } else if (type.name().equals("MULE")) {
            skull = getPlayerSkull("Mule Head", "Donkey");
        } else if (type == EntityType.MUSHROOM_COW) {
            skull = getPlayerSkull("Mooshroom Head", "MHF_MushroomCow");
        } else if (type == EntityType.OCELOT) {
            skull = getPlayerSkull("Ocelot Head", "MHF_Ocelot");
        } else if (type.name().equals("PARROT")) {
            skull = getPlayerSkull("Parrot Head", "MHF_Parrot");
        } else if (type.name().equals("PHANTOM")) {
            skull = getPlayerSkull("Phantom Head", "MHF_Question");
        } else if (type == EntityType.PIG) {
            skull = getPlayerSkull("Pig Head", "MHF_Pig");
        } else if (type == EntityType.PIG_ZOMBIE) {
            skull = getPlayerSkull("Pig Zombie Head", "MHF_PigZombie");
        } else if (type.name().equals("POLAR_BEAR")) {
            skull = getPlayerSkull("Polar Bear Head", "Unarchiver");
        } else if (type.name().equals("PUFFERFISH")) {
            skull = getPlayerSkull("Pufferfish Head", "MHF_Pufferfish");
        } else if (type == EntityType.RABBIT) {
            skull = getPlayerSkull("Rabbit Head", "MHF_Rabbit");
        } else if (type.name().equals("SALMON")) {
            skull = getPlayerSkull("Salmon Head", "MHF_Question");
        } else if (type == EntityType.SHEEP) {
            skull = getPlayerSkull("Sheep Head", "MHF_Sheep");
        } else if (type.name().equals("SHULKER")) {
            skull = getPlayerSkull("Shulker Head", "MHF_Shulker");
        } else if (type == EntityType.SILVERFISH) {
            skull = getPlayerSkull("Silverfish Head", "MHF_Question");
        } else if (type == EntityType.SKELETON) {
            skull = createSkull((short) 0);
        } else if (type.name().equals("SKELETON_HORSE")) {
            skull = getPlayerSkull("Skeleton Horse Head", "MHF_Question");
        } else if (type == EntityType.SLIME) {
            skull = getPlayerSkull("Slime Head", "MHF_Slime");
        } else if (type.name().equals("SNOWMAN")) {
            skull = (Math.random() >= 0.5d) ? getPlayerSkull("Snowman Head", "MHF_Pumpkin") : getPlayerSkull("Snowman Head", "SnowMan690");
        } else if (type == EntityType.SPIDER) {
            skull = getPlayerSkull("Spider Head", "MHF_Spider");
        } else if (type.name().equals("STRAY")) {
            skull = getPlayerSkull("Wolf Head", "MHF_Wolf");
        } else if (type == EntityType.SQUID) {
            skull = getPlayerSkull("Squid Head", "MHF_Squid");
        } else if (type.name().equals("TROPICAL_FISH")) {
            skull = getPlayerSkull("Tropical Fish Head", "MHF_Question");
        } else if (type.name().equals("TURTLE")) {
            skull = getPlayerSkull("Turtle Head", "MHF_Turtle");
        } else if (type.name().equals("VEX")) {
            skull = getPlayerSkull("Vex Head", "MHF_Vex");
        } else if (type == EntityType.VILLAGER) {
            skull = getPlayerSkull("Villager Head", "MHF_Villager");
        } else if (type.name().equals("VINDICATOR")) {
            skull = getPlayerSkull("Vindicator Head", "Vindicator");
        } else if (type == EntityType.WITCH) {
            skull = getPlayerSkull("Witch Head", "MHF_Witch");
        } else if (type.name().equals("WITHER_SKELETON")) {
            skull = createSkull((short) 1);
        } else if (type.name().equals("WOLF")) {
            skull = getPlayerSkull("Wolf Head", "MHF_Wolf");
        } else if (type == EntityType.ZOMBIE) {
            skull = createSkull((short) 2);
        } else if (type.name().equals("ZOMBIE_HORSE")) {
            skull = getPlayerSkull("Zombie Horse Head", "MHF_Question");
        } else if (type.name().equals("ZOMBIE_VILLAGER")) {
            skull = getPlayerSkull("Zombie Villager Head", "Dweg");
        } else {
            skull = getPlayerSkull("??? Head", "MHF_Question");
        }

        if (skull == null) {
            return;
        }

        event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(), skull);
    }

    private ItemStack getPlayerSkull(String displayName, String playerName) {
        try {
            PlayerInfo info = PlayerLookup.get(playerName);
            return getPlayerSkull(displayName, info.getUUID());
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
            return null;
        }
    }

    private ItemStack getPlayerSkull(String displayName, UUID playerUuid) {
        ItemStack skull;
        try {
            skull = SkinLookup.get(playerUuid, new File(plugin.getDataFolder(), "cache")).getSkull();
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
            return null;
        }

        if (displayName != null && skull != null) {
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            if (meta == null) {
                meta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(skull.getType());
            }
            meta.setDisplayName(displayName);
            skull.setItemMeta(meta);
        }

        return skull;
    }

    private ItemStack createSkull(short data) {
        ItemStack skull = new ItemStack(skullMaterial, 1);
        skull.setDurability(data);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        if (skullMeta == null) {
            skullMeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(skullMaterial);
        }
        skull.setItemMeta(skullMeta);

        return skull;
    }
}
