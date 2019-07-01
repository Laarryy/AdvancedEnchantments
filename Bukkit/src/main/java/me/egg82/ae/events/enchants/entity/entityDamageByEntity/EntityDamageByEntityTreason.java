package me.egg82.ae.events.enchants.entity.entityDamageByEntity;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import me.egg82.ae.APIException;
import me.egg82.ae.EnchantAPI;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.BukkitEnchantableItem;
import me.egg82.ae.services.entity.EntityItemHandler;
import ninja.egg82.service.ServiceLocator;
import ninja.egg82.service.ServiceNotFoundException;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityDamageByEntityTreason implements Consumer<EntityDamageByEntityEvent> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private EnchantAPI api = EnchantAPI.getInstance();

    public EntityDamageByEntityTreason() { }

    public void accept(EntityDamageByEntityEvent event) {
        EntityItemHandler entityItemHandler;
        try {
            entityItemHandler = ServiceLocator.get(EntityItemHandler.class);
        } catch (InstantiationException | IllegalAccessException | ServiceNotFoundException ex) {
            logger.error(ex.getMessage(), ex);
            return;
        }

        Entity from = event.getDamager();
        LivingEntity to = (LivingEntity) event.getEntity();

        Optional<EntityEquipment> equipment = Optional.ofNullable(to.getEquipment());
        if (!equipment.isPresent()) {
            return;
        }

        if (tryCurse(BukkitEnchantableItem.fromItemStack(equipment.get().getHelmet()))) {
            transferItem(from, equipment.get().getHelmet());
            equipment.get().setHelmet(null);
        }
        if (tryCurse(BukkitEnchantableItem.fromItemStack(equipment.get().getChestplate()))) {
            transferItem(from, equipment.get().getChestplate());
            equipment.get().setChestplate(null);
        }
        if (tryCurse(BukkitEnchantableItem.fromItemStack(equipment.get().getLeggings()))) {
            transferItem(from, equipment.get().getLeggings());
            equipment.get().setLeggings(null);
        }
        if (tryCurse(BukkitEnchantableItem.fromItemStack(equipment.get().getBoots()))) {
            transferItem(from, equipment.get().getBoots());
            equipment.get().setBoots(null);
        }

        Optional<ItemStack> mainHand = entityItemHandler.getItemInMainHand(to);
        if (mainHand.isPresent()) {
            if (tryCurse(BukkitEnchantableItem.fromItemStack(mainHand.get()))) {
                transferItem(from, mainHand.get());
                entityItemHandler.setItemInMainHand(to, null);
            }
        }

        Optional<ItemStack> offHand = entityItemHandler.getItemInOffHand(to);
        if (offHand.isPresent()) {
            if (tryCurse(BukkitEnchantableItem.fromItemStack(offHand.get()))) {
                transferItem(from, offHand.get());
                entityItemHandler.setItemInOffHand(to, null);
            }
        }
    }

    private boolean tryCurse(BukkitEnchantableItem item) {
        boolean hasEnchantment;
        int level;
        try {
            hasEnchantment = api.anyHasEnchantment(AdvancedEnchantment.TREASON_CURSE, item);
            level = api.getMaxLevel(AdvancedEnchantment.TREASON_CURSE, item);
        } catch (APIException ex) {
            logger.error(ex.getMessage(), ex);
            return false;
        }

        if (!hasEnchantment) {
            return false;
        }

        return Math.random() <= 0.02 * level;
    }

    private void transferItem(Entity to, ItemStack item) {
        if (to instanceof InventoryHolder) {
            Map<Integer, ItemStack> droppedItems = ((InventoryHolder) to).getInventory().addItem(item);
            for (Map.Entry<Integer, ItemStack> kvp : droppedItems.entrySet()) {
                to.getWorld().dropItemNaturally(to.getLocation(), kvp.getValue());
            }
        } else {
            to.getWorld().dropItemNaturally(to.getLocation(), item);
        }
    }
}
