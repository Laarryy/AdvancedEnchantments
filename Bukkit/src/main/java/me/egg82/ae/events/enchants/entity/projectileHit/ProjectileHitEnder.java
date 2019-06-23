package me.egg82.ae.events.enchants.entity.projectileHit;

import java.util.Random;
import java.util.function.Consumer;
import me.egg82.ae.APIException;
import me.egg82.ae.EnchantAPI;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.BukkitEnchantableItem;
import me.egg82.ae.utils.BlockUtil;
import me.egg82.ae.utils.LocationUtil;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.EntityEquipment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProjectileHitEnder implements Consumer<ProjectileHitEvent> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private EnchantAPI api = EnchantAPI.getInstance();

    public ProjectileHitEnder() { }

    public void accept(ProjectileHitEvent event) {
        LivingEntity to = (LivingEntity) event.getHitEntity();
        EntityEquipment equipment = to.getEquipment();

        boolean hasEnchantment;
        int level;
        try {
            hasEnchantment = api.anyHasEnchantment(AdvancedEnchantment.ENDER_CURSE,
                    BukkitEnchantableItem.fromItemStack(equipment.getHelmet()),
                    BukkitEnchantableItem.fromItemStack(equipment.getChestplate()),
                    BukkitEnchantableItem.fromItemStack(equipment.getLeggings()),
                    BukkitEnchantableItem.fromItemStack(equipment.getBoots())
                    );
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

        if (!hasEnchantment) {
            return;
        }

        if (Math.random() > 0.08 * level) {
            return;
        }

        to.teleport(BlockUtil.getHighestSolidBlock(LocationUtil.getRandomPointAround(to.getLocation(), (new Random().nextInt(5) + 5) * level, false)).getLocation().add(0.0d, 1.0d, 0.0d));
    }
}
