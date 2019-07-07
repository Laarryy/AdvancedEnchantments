package me.egg82.ae.tasks;

import java.util.Optional;
import me.egg82.ae.APIException;
import me.egg82.ae.EnchantAPI;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.BukkitEnchantableItem;
import me.egg82.ae.api.GenericEnchantableItem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskAntigravity implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private EnchantAPI api = EnchantAPI.getInstance();

    public TaskAntigravity() { }

    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Optional<EntityEquipment> equipment = Optional.ofNullable(player.getEquipment());
            if (!equipment.isPresent()) {
                return;
            }

            GenericEnchantableItem enchantableBoots = BukkitEnchantableItem.fromItemStack(equipment.get().getBoots());

            boolean hasEnchantment;
            int level;
            try {
                hasEnchantment = api.anyHasEnchantment(AdvancedEnchantment.ANTIGRAVITY, enchantableBoots);
                level = api.getMaxLevel(AdvancedEnchantment.ANTIGRAVITY, enchantableBoots);
            } catch (APIException ex) {
                logger.error(ex.getMessage(), ex);
                return;
            }

            if (!hasEnchantment || level <= 0) {
                return;
            }

            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 60, level + 1), true);
        }
    }
}
