package me.egg82.ae.events.enchants.entity.entityDamageByEntity;

import java.util.Optional;
import java.util.function.Consumer;
import me.egg82.ae.APIException;
import me.egg82.ae.EnchantAPI;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.BukkitEnchantableItem;
import me.egg82.ae.api.GenericEnchantableItem;
import me.egg82.ae.services.entity.EntityItemHandler;
import ninja.egg82.service.ServiceLocator;
import ninja.egg82.service.ServiceNotFoundException;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityDamageByEntityDisarming implements Consumer<EntityDamageByEntityEvent> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private EnchantAPI api = EnchantAPI.getInstance();

    public EntityDamageByEntityDisarming() { }

    public void accept(EntityDamageByEntityEvent event) {
        EntityItemHandler entityItemHandler;
        try {
            entityItemHandler = ServiceLocator.get(EntityItemHandler.class);
        } catch (InstantiationException | IllegalAccessException | ServiceNotFoundException ex) {
            logger.error(ex.getMessage(), ex);
            return;
        }

        LivingEntity from = (LivingEntity) event.getDamager();

        Optional<ItemStack> mainHand = entityItemHandler.getItemInMainHand(from);
        GenericEnchantableItem enchantableMainHand = mainHand.isPresent() ? BukkitEnchantableItem.fromItemStack(mainHand.get()) : null;

        boolean hasEnchantment;
        int level;
        try {
            hasEnchantment = api.anyHasEnchantment(AdvancedEnchantment.DISARMING, enchantableMainHand);
            level = api.getMaxLevel(AdvancedEnchantment.DISARMING, enchantableMainHand);
        } catch (APIException ex) {
            logger.error(ex.getMessage(), ex);
            return;
        }

        if (!hasEnchantment) {
            return;
        }

        if (Math.random() > 0.02 * level) {
            return;
        }

        LivingEntity to = (LivingEntity) event.getEntity();

        Optional<ItemStack> otherMainHand = entityItemHandler.getItemInMainHand(to);
        Optional<ItemStack> otherOffHand = entityItemHandler.getItemInOffHand(to);

        if (otherMainHand.isPresent()) {
            to.getWorld().dropItemNaturally(to.getLocation(), otherMainHand.get());
            entityItemHandler.setItemInMainHand(to, null);
        } else if (otherOffHand.isPresent()) {
            to.getWorld().dropItemNaturally(to.getLocation(), otherOffHand.get());
            entityItemHandler.setItemInOffHand(to, null);
        }
    }
}
