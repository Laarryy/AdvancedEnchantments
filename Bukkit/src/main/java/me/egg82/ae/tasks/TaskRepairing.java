package me.egg82.ae.tasks;

import java.util.Optional;
import me.egg82.ae.APIException;
import me.egg82.ae.EnchantAPI;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.BukkitEnchantableItem;
import me.egg82.ae.services.entity.EntityItemHandler;
import me.egg82.ae.utils.ItemDurabilityUtil;
import ninja.egg82.service.ServiceLocator;
import ninja.egg82.service.ServiceNotFoundException;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskRepairing implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private EnchantAPI api = EnchantAPI.getInstance();

    private EntityItemHandler entityItemHandler;

    public TaskRepairing() {
        try {
            entityItemHandler = ServiceLocator.get(EntityItemHandler.class);
        } catch (InstantiationException | IllegalAccessException | ServiceNotFoundException ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Optional<EntityEquipment> equipment = Optional.ofNullable(player.getEquipment());
            if (!equipment.isPresent()) {
                return;
            }

            tryRepair(BukkitEnchantableItem.fromItemStack(equipment.get().getHelmet()));
            tryRepair(BukkitEnchantableItem.fromItemStack(equipment.get().getChestplate()));
            tryRepair(BukkitEnchantableItem.fromItemStack(equipment.get().getLeggings()));
            tryRepair(BukkitEnchantableItem.fromItemStack(equipment.get().getBoots()));

            Optional<ItemStack> mainHand = entityItemHandler.getItemInMainHand(player);
            if (mainHand.isPresent()) {
                tryRepair(BukkitEnchantableItem.fromItemStack(mainHand.get()));
            }

            Optional<ItemStack> offHand = entityItemHandler.getItemInOffHand(player);
            if (offHand.isPresent()) {
                tryRepair(BukkitEnchantableItem.fromItemStack(offHand.get()));
            }
        }
    }

    private void tryRepair(BukkitEnchantableItem item) {
        int level;
        try {
            level = api.getMaxLevel(AdvancedEnchantment.REPAIRING, item);
        } catch (APIException ex) {
            logger.error(ex.getMessage(), ex);
            return;
        }

        if (level < 0) {
            return;
        }

        ItemDurabilityUtil.addDurability((ItemStack) item.getConcrete(), level);
    }
}
