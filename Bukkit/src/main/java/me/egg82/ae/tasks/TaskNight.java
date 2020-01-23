package me.egg82.ae.tasks;

import java.util.Optional;
import me.egg82.ae.APIException;
import me.egg82.ae.EnchantAPI;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.BukkitEnchantableItem;
import me.egg82.ae.api.GenericEnchantableItem;
import me.egg82.ae.services.CollectionProvider;
import me.egg82.ae.utils.PermissionUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskNight implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private EnchantAPI api = EnchantAPI.getInstance();

    public TaskNight() { }

    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!PermissionUtil.canUseEnchant(player, "ae.curse.night")) {
                continue;
            }

            Optional<EntityEquipment> equipment = Optional.ofNullable(player.getEquipment());
            if (!equipment.isPresent()) {
                continue;
            }

            GenericEnchantableItem enchantableHelmet = BukkitEnchantableItem.fromItemStack(equipment.get().getHelmet());

            boolean hasEnchantment;
            try {
                hasEnchantment = api.anyHasEnchantment(AdvancedEnchantment.NIGHT_CURSE, enchantableHelmet);
            } catch (APIException ex) {
                logger.error(ex.getMessage(), ex);
                continue;
            }

            if (!hasEnchantment) {
                if (CollectionProvider.getNight().remove(player.getUniqueId())) {
                    player.resetPlayerTime();
                }
                continue;
            }

            player.setPlayerTime(18000, false);
            CollectionProvider.getNight().add(player.getUniqueId());
        }
    }
}
