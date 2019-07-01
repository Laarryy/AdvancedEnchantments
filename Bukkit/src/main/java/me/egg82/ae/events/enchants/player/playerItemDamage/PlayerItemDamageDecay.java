package me.egg82.ae.events.enchants.player.playerItemDamage;

import java.util.function.Consumer;
import me.egg82.ae.APIException;
import me.egg82.ae.EnchantAPI;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.BukkitEnchantableItem;
import me.egg82.ae.api.GenericEnchantableItem;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerItemDamageDecay implements Consumer<PlayerItemDamageEvent> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private EnchantAPI api = EnchantAPI.getInstance();

    public PlayerItemDamageDecay() { }

    public void accept(PlayerItemDamageEvent event) {
        GenericEnchantableItem enchantableItem = BukkitEnchantableItem.fromItemStack(event.getItem());

        boolean hasEnchantment;
        int level;
        try {
            hasEnchantment = api.anyHasEnchantment(AdvancedEnchantment.DECAY_CURSE, enchantableItem);
            level = api.getMaxLevel(AdvancedEnchantment.DECAY_CURSE, enchantableItem);
        } catch (APIException ex) {
            logger.error(ex.getMessage(), ex);
            return;
        }

        if (!hasEnchantment || level <= 0) {
            return;
        }

        event.setDamage(event.getDamage() + level * 2);
    }
}
