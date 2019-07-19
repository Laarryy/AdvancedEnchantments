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

    private static boolean isPaper = true;

    static {
        Optional<Material> m = MaterialLookup.get("PLAYER_HEAD", "SKULL_ITEM");
        if (!m.isPresent()) {
            throw new RuntimeException("Could not get skull material.");
        }
        skullMaterial = m.get();

        try {
            Class.forName("com.destroystokyo.paper.profile.PlayerProfile");
        } catch (ClassNotFoundException ignored) {
            isPaper = false;
        }
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
        GenericEnchantableItem enchantableMainHand = mainHand.isPresent() ? BukkitEnchantableItem.fromItemStack(mainHand.get()) : null;

        boolean hasEnchantment;
        int level;
        try {
            hasEnchantment = api.anyHasEnchantment(AdvancedEnchantment.BEHEADING, enchantableMainHand);
            level = api.getMaxLevel(AdvancedEnchantment.BEHEADING, enchantableMainHand);
        } catch (APIException ex) {
            logger.error(ex.getMessage(), ex);
            return;
        }

        if (!hasEnchantment) {
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
            // There's no "reasonable" player heads for this one
            if (isPaper) {
                skull = getPlayerSkull(
                        "Bat Head",
                        "MHF_Question",
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzgyMGExMGRiMjIyZjY5YWMyMjE1ZDdkMTBkY2E0N2VlYWZhMjE1NTUzNzY0YTJiODFiYWZkNDc5ZTc5MzNkMSJ9fX0=",
                        "https://textures.minecraft.net/texture/3820a10db222f69ac2215d7d10dca47eeafa215553764a2b81bafd479e7933d1"
                );
            } else {
                skull = getPlayerSkull("Bat Head", "Bat");
            }
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
            // There's no "reasonable" player heads for this one
            if (isPaper) {
                skull = getPlayerSkull(
                        "Dolphin Head",
                        "MHF_Question",
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGU5Njg4Yjk1MGQ4ODBiNTViN2FhMmNmY2Q3NmU1YTBmYTk0YWFjNmQxNmY3OGU4MzNmNzQ0M2VhMjlmZWQzIn19fQ==",
                        "https://textures.minecraft.net/texture/8e9688b950d880b55b7aa2cfcd76e5a0fa94aac6d16f78e833f7443ea29fed3"
                );
            } else {
                skull = getPlayerSkull("Dolphin Head", "MHF_Dolphin");
            }
        } else if (type.name().equals("DONKEY")) {
            // There's no "reasonable" player heads for this one
            if (isPaper) {
                skull = getPlayerSkull(
                        "Donkey Head",
                        "MHF_Question",
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjNhOTc2YzA0N2Y0MTJlYmM1Y2IxOTcxMzFlYmVmMzBjMDA0YzBmYWY0OWQ4ZGQ0MTA1ZmNhMTIwN2VkYWZmMyJ9fX0=",
                        "https://textures.minecraft.net/texture/63a976c047f412ebc5cb197131ebef30c004c0faf49d8dd4105fca1207edaff3"
                );
            } else {
                skull = getPlayerSkull("Donkey Head", "Donkey");
            }
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
            // There's no "reasonable" player heads for this one
            if (isPaper) {
                skull = getPlayerSkull(
                        "Evoker Head",
                        "MHF_Question",
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDk1NDEzNWRjODIyMTM5NzhkYjQ3ODc3OGFlMTIxMzU5MWI5M2QyMjhkMzZkZDU0ZjFlYTFkYTQ4ZTdjYmE2In19fQ==",
                        "https://textures.minecraft.net/texture/d954135dc82213978db478778ae1213591b93d228d36dd54f1ea1da48e7cba6"
                );
            } else {
                skull = getPlayerSkull("Evoker Head", "MHF_Question");
            }
        } else if (type == EntityType.GHAST) {
            skull = getPlayerSkull("Ghast Head", "MHF_Ghast");
        } else if (type == EntityType.GIANT) {
            skull = createSkull((short) 2);
        } else if (type == EntityType.GUARDIAN) {
            skull = (((Guardian) event.getEntity()).isElder()) ? getPlayerSkull("Elder Guardian Head", "ElderGuardian") : getPlayerSkull("Guardian Head", "MHF_Guardian");
        } else if (type == EntityType.HORSE) {
            skull = getPlayerSkull("Horse Head", "Door"); // This account is probably safe
        } else if (type.name().equals("HUSK")) {
            // There's no "reasonable" player heads for this one
            if (isPaper) {
                skull = getPlayerSkull(
                        "Husk Head",
                        "MHF_Question",
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDY3NGM2M2M4ZGI1ZjRjYTYyOGQ2OWEzYjFmOGEzNmUyOWQ4ZmQ3NzVlMWE2YmRiNmNhYmI0YmU0ZGIxMjEifX19",
                        "https://textures.minecraft.net/texture/d674c63c8db5f4ca628d69a3b1f8a36e29d8fd775e1a6bdb6cabb4be4db121"
                );
            } else {
                skull = getPlayerSkull("Husk Head", "MHF_Question");
            }
        } else if (type.name().equals("ILLUSIONER")) {
            // There's no "reasonable" player heads for this one
            if (isPaper) {
                skull = getPlayerSkull(
                        "Illusioner Head",
                        "MHF_Question",
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmYyODgyZGQwOTcyM2U0N2MwYWI5NjYzZWFiMDgzZDZhNTk2OTI3MzcwNjExMGM4MjkxMGU2MWJmOGE4ZjA3ZSJ9fX0=",
                        "https://textures.minecraft.net/texture/2f2882dd09723e47c0ab9663eab083d6a5969273706110c82910e61bf8a8f07e"
                );
            } else {
                skull = getPlayerSkull("Illusioner Head", "MHF_Question");
            }
        } else if (type == EntityType.IRON_GOLEM) {
            skull = getPlayerSkull("Iron Golem Head", "MHF_Golem");
        } else if (type.name().equals("LLAMA")) {
            // There's no "reasonable" player heads for this one
            if (isPaper) {
                skull = getPlayerSkull(
                        "Llama Head",
                        "MHF_Question",
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmE1ZjEwZTZlNjIzMmYxODJmZTk2NmY1MDFmMWMzNzk5ZDQ1YWUxOTAzMWExZTQ5NDFiNWRlZTBmZWZmMDU5YiJ9fX0=",
                        "https://textures.minecraft.net/texture/2a5f10e6e6232f182fe966f501f1c3799d45ae19031a1e4941b5dee0feff059b"
                );
            } else {
                skull = getPlayerSkull("Llama Head", "MHF_Question");
            }
        } else if (type == EntityType.MAGMA_CUBE) {
            skull = getPlayerSkull("Magma Cube Head", "MHF_LavaSlime");
        } else if (type.name().equals("MULE")) {
            // There's no "reasonable" player heads for this one
            if (isPaper) {
                skull = getPlayerSkull(
                        "Mule Head",
                        "MHF_Question",
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjNhOTc2YzA0N2Y0MTJlYmM1Y2IxOTcxMzFlYmVmMzBjMDA0YzBmYWY0OWQ4ZGQ0MTA1ZmNhMTIwN2VkYWZmMyJ9fX0=",
                        "https://textures.minecraft.net/texture/63a976c047f412ebc5cb197131ebef30c004c0faf49d8dd4105fca1207edaff3"
                );
            } else {
                skull = getPlayerSkull("Mule Head", "Donkey");
            }
        } else if (type == EntityType.MUSHROOM_COW) {
            skull = getPlayerSkull("Mooshroom Head", "MHF_MushroomCow");
        } else if (type == EntityType.OCELOT) {
            skull = getPlayerSkull("Ocelot Head", "MHF_Ocelot");
        } else if (type.name().equals("PARROT")) {
            // There's no "reasonable" player heads for this one
            if (isPaper) {
                skull = getPlayerSkull(
                        "Parrot Head",
                        "MHF_Question",
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTRiYThkNjZmZWNiMTk5MmU5NGI4Njg3ZDZhYjRhNTMyMGFiNzU5NGFjMTk0YTI2MTVlZDRkZjgxOGVkYmMzIn19fQ==",
                        "https://textures.minecraft.net/texture/a4ba8d66fecb1992e94b8687d6ab4a5320ab7594ac194a2615ed4df818edbc3"
                );
            } else {
                skull = getPlayerSkull("Parrot Head", "MHF_Parrot");
            }
        } else if (type.name().equals("PHANTOM")) {
            // There's no "reasonable" player heads for this one
            if (isPaper) {
                skull = getPlayerSkull(
                        "Phantom Head",
                        "MHF_Question",
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2U5NTE1M2VjMjMyODRiMjgzZjAwZDE5ZDI5NzU2ZjI0NDMxM2EwNjFiNzBhYzAzYjk3ZDIzNmVlNTdiZDk4MiJ9fX0=",
                        "https://textures.minecraft.net/texture/7e95153ec23284b283f00d19d29756f244313a061b70ac03b97d236ee57bd982"
                );
            } else {
                skull = getPlayerSkull("Phantom Head", "MHF_Question");
            }
        } else if (type == EntityType.PIG) {
            skull = getPlayerSkull("Pig Head", "MHF_Pig");
        } else if (type == EntityType.PIG_ZOMBIE) {
            skull = getPlayerSkull("Pig Zombie Head", "MHF_PigZombie");
        } else if (type.name().equals("POLAR_BEAR")) {
            skull = getPlayerSkull("Polar Bear Head", "cdragoneer");
        } else if (type.name().equals("PUFFERFISH")) {
            skull = getPlayerSkull("Pufferfish Head", "MHF_Pufferfish");
        } else if (type == EntityType.RABBIT) {
            skull = getPlayerSkull("Rabbit Head", "MHF_Rabbit");
        } else if (type.name().equals("SALMON")) {
            // There's no "reasonable" player heads for this one
            if (isPaper) {
                skull = getPlayerSkull(
                        "Salmon Head",
                        "MHF_Question",
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGFlYjIxYTI1ZTQ2ODA2Y2U4NTM3ZmJkNjY2ODI4MWNmMTc2Y2VhZmU5NWFmOTBlOTRhNWZkODQ5MjQ4NzgifX19",
                        "https://textures.minecraft.net/texture/8aeb21a25e46806ce8537fbd6668281cf176ceafe95af90e94a5fd84924878"
                );
            } else {
                skull = getPlayerSkull("Salmon Head", "MHF_Question");
            }
        } else if (type == EntityType.SHEEP) {
            skull = getPlayerSkull("Sheep Head", "MHF_Sheep");
        } else if (type.name().equals("SHULKER")) {
            skull = getPlayerSkull("Shulker Head", "MHF_Shulker");
        } else if (type == EntityType.SILVERFISH) {
            // There's no "reasonable" player heads for this one
            if (isPaper) {
                skull = getPlayerSkull(
                        "Silverfish Head",
                        "MHF_Question",
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGE5MWRhYjgzOTFhZjVmZGE1NGFjZDJjMGIxOGZiZDgxOWI4NjVlMWE4ZjFkNjIzODEzZmE3NjFlOTI0NTQwIn19fQ==",
                        "https://textures.minecraft.net/texture/da91dab8391af5fda54acd2c0b18fbd819b865e1a8f1d623813fa761e924540"
                );
            } else {
                skull = getPlayerSkull("Silverfish Head", "MHF_Question");
            }
        } else if (type == EntityType.SKELETON) {
            skull = createSkull((short) 0);
        } else if (type.name().equals("SKELETON_HORSE")) {
            // There's no "reasonable" player heads for this one
            if (isPaper) {
                skull = getPlayerSkull(
                        "Skeleton Horse Head",
                        "MHF_Question",
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDdlZmZjZTM1MTMyYzg2ZmY3MmJjYWU3N2RmYmIxZDIyNTg3ZTk0ZGYzY2JjMjU3MGVkMTdjZjg5NzNhIn19fQ==",
                        "https://textures.minecraft.net/texture/47effce35132c86ff72bcae77dfbb1d22587e94df3cbc2570ed17cf8973a"
                );
            } else {
                skull = getPlayerSkull("Skeleton Horse Head", "MHF_Question");
            }
        } else if (type == EntityType.SLIME) {
            skull = getPlayerSkull("Slime Head", "MHF_Slime");
        } else if (type.name().equals("SNOWMAN")) {
            if (isPaper) {
                skull = (Math.random() >= 0.5d) ? getPlayerSkull("Snow Golem Head", "Koebasti") : getPlayerSkull(
                        "Snow Golem Head",
                        "MHF_Question",
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWZkZmQxZjc1MzhjMDQwMjU4YmU3YTkxNDQ2ZGE4OWVkODQ1Y2M1ZWY3MjhlYjVlNjkwNTQzMzc4ZmNmNCJ9fX0=",
                        "https://textures.minecraft.net/texture/1fdfd1f7538c040258be7a91446da89ed845cc5ef728eb5e690543378fcf4"
                );
            } else {
                skull = getPlayerSkull("Snowman Head", "Koebasti");
            }
        } else if (type == EntityType.SPIDER) {
            skull = getPlayerSkull("Spider Head", "MHF_Spider");
        } else if (type.name().equals("STRAY")) {
            skull = getPlayerSkull("Wolf Head", "MHF_Wolf");
        } else if (type == EntityType.SQUID) {
            skull = getPlayerSkull("Squid Head", "MHF_Squid");
        } else if (type.name().equals("TROPICAL_FISH")) {
            skull = getPlayerSkull("Tropical Fish Head", "_TheBuilder_");
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
            // There's no "reasonable" player heads for this one
            if (isPaper) {
                skull = getPlayerSkull(
                        "Zombie Horse Head",
                        "MHF_Question",
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDIyOTUwZjJkM2VmZGRiMThkZTg2ZjhmNTVhYzUxOGRjZTczZjEyYTZlMGY4NjM2ZDU1MWQ4ZWI0ODBjZWVjIn19fQ==",
                        "https://textures.minecraft.net/texture/d22950f2d3efddb18de86f8f55ac518dce73f12a6e0f8636d551d8eb480ceec"
                );
            } else {
                skull = getPlayerSkull("Zombie Horse Head", "MHF_Question");
            }
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
            return getPlayerSkull(displayName, info.getName(), info.getUUID(), null, null);
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
            return null;
        }
    }

    private ItemStack getPlayerSkull(String displayName, UUID playerUuid) {
        try {
            PlayerInfo info = PlayerLookup.get(playerUuid);
            return getPlayerSkull(displayName, info.getName(), info.getUUID(), null, null);
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
            return null;
        }
    }

    private ItemStack getPlayerSkull(String displayName, UUID playerUuid, String texture, String textureUrl) {
        try {
            PlayerInfo info = PlayerLookup.get(playerUuid);
            return getPlayerSkull(displayName, info.getName(), info.getUUID(), texture, textureUrl);
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
            return null;
        }
    }

    private ItemStack getPlayerSkull(String displayName, String playerName, String texture, String textureUrl) {
        try {
            PlayerInfo info = PlayerLookup.get(playerName);
            return getPlayerSkull(displayName, info.getName(), info.getUUID(), texture, textureUrl);
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
            return null;
        }
    }

    private ItemStack getPlayerSkull(String displayName, String playerName, UUID playerUuid, String texture, String textureUrl) {
        ItemStack skull;
        try {
            if (texture == null || textureUrl == null) {
                skull = SkinLookup.get(playerUuid, new File(plugin.getDataFolder(), "cache")).getSkull();
            } else {
                skull = SkinLookup.get(playerUuid, playerName, texture, textureUrl, null).getSkull();
            }
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
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        if (skullMeta == null) {
            skullMeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(skullMaterial);
        }
        skull.setItemMeta(skullMeta);
        skull.setDurability(data);

        return skull;
    }
}
