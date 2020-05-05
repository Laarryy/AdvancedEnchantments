package me.egg82.ae.events;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import me.egg82.ae.APIException;
import me.egg82.ae.EnchantAPI;
import me.egg82.ae.api.BukkitEnchantableItem;
import me.egg82.ae.api.GenericEnchantment;
import me.egg82.ae.services.entity.EntityItemHandler;
import me.egg82.ae.utils.PermissionUtil;
import ninja.egg82.events.BukkitEventSubscriber;
import ninja.egg82.service.ServiceLocator;
import ninja.egg82.service.ServiceNotFoundException;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class EventHolder {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected final List<BukkitEventSubscriber<?>> events = new ArrayList<>();

    protected final EnchantAPI api = EnchantAPI.getInstance();

    public final int numEvents() { return events.size(); }

    public final void cancel() {
        for (BukkitEventSubscriber<?> event : events) {
            event.cancel();
        }
    }

    protected EntityItemHandler getItemHandler() {
        EntityItemHandler retVal;
        try {
            retVal = ServiceLocator.get(EntityItemHandler.class);
        } catch (InstantiationException | IllegalAccessException | ServiceNotFoundException ex) {
            logger.error(ex.getMessage(), ex);
            return null;
        }
        return retVal;
    }

    protected int getLevelForArmor(LivingEntity entity, GenericEnchantment enchant) { return getLevelForArmor(entity, enchant, null); }

    protected int getLevelForArmor(LivingEntity entity, GenericEnchantment enchant, String[] permissions) {
        Optional<EntityEquipment> equipment = Optional.ofNullable(entity.getEquipment());
        if (!equipment.isPresent()) {
            return -1;
        }

        if (permissions != null) {
            boolean canUse = true;
            for (String p : permissions) {
                if (!PermissionUtil.canUseEnchant(entity, p)) {
                    canUse = false;
                    break;
                }
            }
            if (!canUse) {
                return -1;
            }
        }

        int level;
        try {
            level = api.getMaxLevel(enchant,
                    BukkitEnchantableItem.fromItemStack(equipment.get().getHelmet()),
                    BukkitEnchantableItem.fromItemStack(equipment.get().getChestplate()),
                    BukkitEnchantableItem.fromItemStack(equipment.get().getLeggings()),
                    BukkitEnchantableItem.fromItemStack(equipment.get().getBoots())
            );
        } catch (APIException ex) {
            logger.error(ex.getMessage(), ex);
            return -1;
        }

        return level;
    }

    protected List<LivingEntity> getEntitiesWithArmorEnchant(World world, GenericEnchantment enchant, String[] permissions) {
        List<LivingEntity> retVal = new ArrayList<>();

        for (LivingEntity entity : world.getLivingEntities()) {
            Optional<EntityEquipment> equipment = Optional.ofNullable(entity.getEquipment());
            if (!equipment.isPresent()) {
                continue;
            }

            boolean canUse = true;
            for (String p : permissions) {
                if (!PermissionUtil.canUseEnchant(entity, p)) {
                    canUse = false;
                    break;
                }
            }
            if (!canUse) {
                continue;
            }

            try {
                if (api.anyHasEnchantment(enchant,
                        BukkitEnchantableItem.fromItemStack(equipment.get().getHelmet()),
                        BukkitEnchantableItem.fromItemStack(equipment.get().getChestplate()),
                        BukkitEnchantableItem.fromItemStack(equipment.get().getLeggings()),
                        BukkitEnchantableItem.fromItemStack(equipment.get().getBoots())
                )) {
                    retVal.add(entity);
                }
            } catch (APIException ex) {
                logger.error(ex.getMessage(), ex);
            }
        }

        return retVal;
    }
}
