package me.egg82.ae.events.enchants.entity.entityDamageByEntity;

import java.util.Random;
import java.util.function.Consumer;
import me.egg82.ae.APIException;
import me.egg82.ae.EnchantAPI;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.BukkitEnchantableItem;
import me.egg82.ae.utils.BlockUtil;
import me.egg82.ae.utils.LocationUtil;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityDamageByEntityEnder implements Consumer<EntityDamageByEntityEvent> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private EnchantAPI api = EnchantAPI.getInstance();

    public EntityDamageByEntityEnder() { }

    public void accept(EntityDamageByEntityEvent event) {
        LivingEntity to = (LivingEntity) event.getEntity();
        EntityEquipment equipment = to.getEquipment();

        int level;
        try {
            level = api.getMaxLevel(AdvancedEnchantment.ENDER_CURSE,
                    BukkitEnchantableItem.fromItemStack(equipment.getHelmet()),
                    BukkitEnchantableItem.fromItemStack(equipment.getChestplate()),
                    BukkitEnchantableItem.fromItemStack(equipment.getLeggings()),
                    BukkitEnchantableItem.fromItemStack(equipment.getBoots())
            );
        } catch (APIException ex) {
            logger.error(ex.getMessage(), ex);
            return;
        }

        if (level < 0) {
            return;
        }

        if (Math.random() > 0.08 * level) {
            return;
        }

        to.teleport(BlockUtil.getHighestSolidBlock(LocationUtil.getRandomPointAround(to.getLocation(), new Random().nextInt(150) + 150, false)).getLocation().add(0.0d, 1.0d, 0.0d));
    }
}
