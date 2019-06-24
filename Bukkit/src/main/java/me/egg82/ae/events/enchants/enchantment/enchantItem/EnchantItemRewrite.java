package me.egg82.ae.events.enchants.enchantment.enchantItem;

import java.util.function.Consumer;
import me.egg82.ae.api.BukkitEnchantableItem;
import org.bukkit.Bukkit;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.plugin.Plugin;

public class EnchantItemRewrite implements Consumer<EnchantItemEvent> {
    private final Plugin plugin;

    public EnchantItemRewrite(Plugin plugin) {
        this.plugin = plugin;
    }

    public void accept(EnchantItemEvent event) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            BukkitEnchantableItem item = BukkitEnchantableItem.fromItemStack(event.getItem());
            item.rewriteMeta();
        }, 1L);
    }
}
