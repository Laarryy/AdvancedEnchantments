package me.egg82.ae.utils;

import java.util.Optional;
import me.egg82.ae.services.sound.SoundLookup;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;

public class ItemDurabilityUtil {
    private ItemDurabilityUtil() { }

    private static Sound breakSound;

    static {
        Optional<Sound> tempSound = SoundLookup.get("ENTITY_ITEM_BREAK", "ITEM_BREAK");
        if (!tempSound.isPresent()) {
            throw new RuntimeException("Could not get break sound.");
        }
        breakSound = tempSound.get();
    }

    public static boolean removeDurability(ItemStack item, int durabilityToRemove, Location soundLocation) {
        if (item == null) {
            throw new IllegalArgumentException("item cannot be null.");
        }

        if (durabilityToRemove == 0) {
            return true;
        }

        short durability = item.getDurability();
        if (durability >= item.getType().getMaxDurability() - durabilityToRemove) {
            if (soundLocation != null) {
                soundLocation.getWorld().playSound(soundLocation, breakSound, 1.0f, 1.0f);
            }
            return false;
        }

        item.setDurability((short) (item.getDurability() + durabilityToRemove));
        return true;
    }

    public static void addDurability(ItemStack item, int durabilityToAdd) {
        if (item == null) {
            throw new IllegalArgumentException("item cannot be null.");
        }

        if (durabilityToAdd == 0) {
            return;
        }

        int oldDurability = item.getDurability();
        short newDurability = (short) Math.max(0, oldDurability - durabilityToAdd);

        if (newDurability == oldDurability) {
            return;
        }

        item.setDurability(newDurability);
    }
}
