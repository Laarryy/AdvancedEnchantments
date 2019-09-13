package me.egg82.ae.utils;

import java.util.Optional;
import me.egg82.ae.APIException;
import me.egg82.ae.EnchantAPI;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.BukkitEnchantableItem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SoulsUtil {
    private static final Logger logger = LoggerFactory.getLogger(SoulsUtil.class);

    private static final EnchantAPI api = EnchantAPI.getInstance();

    private SoulsUtil() { }

    public static boolean tryAddSouls(LivingEntity entity, int souls) {
        Optional<EntityEquipment> equipment = Optional.ofNullable(entity.getEquipment());
        if (!equipment.isPresent()) {
            return false;
        }

        return tryStore(BukkitEnchantableItem.fromItemStack(equipment.get().getHelmet()), souls)
                || tryStore(BukkitEnchantableItem.fromItemStack(equipment.get().getChestplate()), souls)
                || tryStore(BukkitEnchantableItem.fromItemStack(equipment.get().getLeggings()), souls)
                || tryStore(BukkitEnchantableItem.fromItemStack(equipment.get().getBoots()), souls);
    }

    public static boolean tryRemoveSouls(LivingEntity entity, int souls) { return tryAddSouls(entity, souls * -1); }

    private static boolean tryStore(BukkitEnchantableItem item, int souls) {
        int level;
        try {
            level = api.getMaxLevel(AdvancedEnchantment.VORPAL, item);
        } catch (APIException ex) {
            logger.error(ex.getMessage(), ex);
            return false;
        }

        if (level < 0) {
            return false;
        }

        int newSouls = item.getSouls() + souls;

        if (newSouls >= 0 && newSouls <= level * 2) {
            item.setSouls(newSouls);
            return true;
        }
        return false;
    }
}
