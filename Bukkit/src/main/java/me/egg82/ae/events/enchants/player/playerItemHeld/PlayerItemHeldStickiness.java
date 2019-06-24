package me.egg82.ae.events.enchants.player.playerItemHeld;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import me.egg82.ae.APIException;
import me.egg82.ae.EnchantAPI;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.BukkitEnchantableItem;
import me.egg82.ae.api.GenericEnchantableItem;
import me.egg82.ae.services.CollectionProvider;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerItemHeldStickiness implements Consumer<PlayerItemHeldEvent> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private EnchantAPI api = EnchantAPI.getInstance();

    public PlayerItemHeldStickiness() { }

    public void accept(PlayerItemHeldEvent event) {
        GenericEnchantableItem enchantableItem = BukkitEnchantableItem.fromItemStack(event.getPlayer().getInventory().getItem(event.getNewSlot()));

        boolean hasEnchantment;
        int level;
        try {
            hasEnchantment = api.anyHasEnchantment(AdvancedEnchantment.STICKINESS_CURSE, enchantableItem);
            level = api.getMaxLevel(AdvancedEnchantment.STICKINESS_CURSE, enchantableItem);
        } catch (APIException ex) {
            logger.error(ex.getMessage(), ex);
            return;
        }

        if (!hasEnchantment || level <= 0) {
            return;
        }

        CollectionProvider.getStickiness().put(event.getPlayer().getUniqueId(), level, level * 750L, TimeUnit.MILLISECONDS);
    }
}
