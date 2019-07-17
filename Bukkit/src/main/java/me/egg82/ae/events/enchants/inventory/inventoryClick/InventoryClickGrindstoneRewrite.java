package me.egg82.ae.events.enchants.inventory.inventoryClick;

import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import me.egg82.ae.api.BukkitEnchantableItem;
import me.egg82.ae.api.BukkitEnchantment;
import me.egg82.ae.api.GenericEnchantment;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class InventoryClickGrindstoneRewrite implements Consumer<InventoryClickEvent> {
    private Random rand = new Random();

    public InventoryClickGrindstoneRewrite() { }

    public void accept(InventoryClickEvent event) {
        ItemStack resultItem = event.getCurrentItem();

        if (resultItem == null || resultItem.getType() == Material.AIR) {
            return;
        }

        BukkitEnchantableItem enchantableResultItem = BukkitEnchantableItem.fromItemStack(resultItem);
        int maxExp = removeEnchants(enchantableResultItem);
        int variation = Math.max(0, maxExp - (maxExp / 4));

        enchantableResultItem.rewriteMeta();
        event.setCurrentItem((ItemStack) enchantableResultItem.getConcrete());

        if (event.getWhoClicked() instanceof Player) {
            ((Player) event.getWhoClicked()).giveExp(rand.nextInt(maxExp + variation) + (maxExp - variation));
        }
    }

    private int removeEnchants(BukkitEnchantableItem enchantableResultItem) {
        Set<GenericEnchantment> removedEnchants = new HashSet<>();
        int retVal = 0;
        for (Map.Entry<GenericEnchantment, Integer> kvp : enchantableResultItem.getEnchantments().entrySet()) {
            if (!(kvp.getKey() instanceof BukkitEnchantment) && !kvp.getKey().isCurse()) {
                removedEnchants.add(kvp.getKey());
                if (kvp.getValue() > 0) {
                    retVal += 6 * kvp.getValue();
                }
            }
        }
        enchantableResultItem.removeEnchantments(removedEnchants);
        return retVal;
    }
}
