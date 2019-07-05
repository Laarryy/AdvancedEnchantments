package me.egg82.ae.utils;

import java.util.Optional;

import me.egg82.ae.api.BukkitEnchantableItem;
import me.egg82.ae.services.sound.SoundLookup;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class ItemDurabilityUtil {
    private ItemDurabilityUtil() { }

    private static Sound breakSound;
    private static boolean hasItemBreakEvent;

    static {
        Optional<Sound> tempSound = SoundLookup.get("ENTITY_ITEM_BREAK", "ITEM_BREAK");
        if (!tempSound.isPresent()) {
            throw new RuntimeException("Could not get break sound.");
        }
        breakSound = tempSound.get();

        try {
            Class.forName("org.bukkit.event.player.PlayerItemDamageEvent");
            hasItemBreakEvent = true;
        } catch (ClassNotFoundException ignored) {
            hasItemBreakEvent = false;
        }
    }

    public static boolean removeDurability(BukkitEnchantableItem item, int durabilityToRemove, Location soundLocation, Plugin plugin) {
        return removeDurability(null, item, durabilityToRemove, soundLocation, plugin);
    }

    public static boolean removeDurability(Player player, BukkitEnchantableItem item, int durabilityToRemove, Location soundLocation, Plugin plugin) {
        if (item == null) {
            throw new IllegalArgumentException("item cannot be null.");
        }
        if (plugin == null) {
            throw new IllegalArgumentException("plugin cannot be null.");
        }

        if (durabilityToRemove == 0) {
            return true;
        }

        ItemStack i = (ItemStack) item.getConcrete();

        if (player != null && hasItemBreakEvent) {
            org.bukkit.event.player.PlayerItemDamageEvent event = new org.bukkit.event.player.PlayerItemDamageEvent(player, i, durabilityToRemove);
            Bukkit.getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                return true;
            }

            durabilityToRemove = event.getDamage();
        }

        short durability = i.getDurability();
        if (durability >= i.getType().getMaxDurability() - durabilityToRemove) {
            if (soundLocation != null) {
                soundLocation.getWorld().playSound(soundLocation, breakSound, 1.0f, 1.0f);
            }
            return false;
        }

        i.setDurability((short) (i.getDurability() + durabilityToRemove));
        Bukkit.getScheduler().runTaskLater(plugin, () -> BukkitEnchantableItem.forceCache(i, item), 1L);

        return true;
    }

    public static void addDurability(BukkitEnchantableItem item, int durabilityToAdd, Plugin plugin) {
        if (item == null) {
            throw new IllegalArgumentException("item cannot be null.");
        }
        if (plugin == null) {
            throw new IllegalArgumentException("plugin cannot be null.");
        }

        if (durabilityToAdd == 0) {
            return;
        }

        ItemStack i = (ItemStack) item.getConcrete();

        int oldDurability = i.getDurability();
        short newDurability = (short) Math.max(0, oldDurability - durabilityToAdd);

        if (newDurability == oldDurability) {
            return;
        }

        i.setDurability(newDurability);
        Bukkit.getScheduler().runTaskLater(plugin, () -> BukkitEnchantableItem.forceCache(i, item), 1L);
    }
}
