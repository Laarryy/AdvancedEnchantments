package me.egg82.ae.events.enchants.enchantment.enchantItem;

import java.util.function.Consumer;
import me.egg82.ae.api.BukkitEnchantableItem;
import org.bukkit.event.enchantment.EnchantItemEvent;

public class EnchantItemReplace implements Consumer<EnchantItemEvent> {
    public EnchantItemReplace() { }

    public void accept(EnchantItemEvent event) {
        BukkitEnchantableItem item = BukkitEnchantableItem.fromItemStack(event.getItem());
        item.rewriteMeta();
    }
}
