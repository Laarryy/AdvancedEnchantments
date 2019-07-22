package me.egg82.ae.hooks;

import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.utils.CombatUtil;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.Plugin;

public class TownyHook implements PluginHook {
    private Towny plugin;
    private static TownyHook hook = null;

    public TownyHook(Plugin plugin) {
        this.plugin = (Towny) plugin;
        hook = this;
    }

    public void cancel() { }

    public static boolean ignoreCancelled(EntityDamageByEntityEvent event) { return hook == null || !CombatUtil.preventDamageCall(hook.plugin, event.getDamager(), event.getEntity()); }
}
