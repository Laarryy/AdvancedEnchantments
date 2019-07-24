package me.egg82.ae.events;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import me.egg82.ae.APIException;
import me.egg82.ae.EnchantAPI;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.BukkitEnchantableItem;
import me.egg82.ae.hooks.TownyHook;
import ninja.egg82.events.BukkitEventSubscriber;
import ninja.egg82.service.ServiceLocator;
import ninja.egg82.service.ServiceNotFoundException;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
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

    protected final boolean canUseEnchant(Object obj, String node) { return !(obj instanceof Player) || ((Player) obj).hasPermission(node); }

    protected final boolean townyIgnoreCancelled(EntityDamageByEntityEvent event) {
        try {
            TownyHook townyHook = ServiceLocator.get(TownyHook.class);
            return townyHook.ignoreCancelled(event);
        } catch (InstantiationException | IllegalAccessException | ServiceNotFoundException ignored) { return true; }
    }

    protected final boolean tryAddSouls(LivingEntity entity, int souls) {
        Optional<EntityEquipment> equipment = Optional.ofNullable(entity.getEquipment());
        if (!equipment.isPresent()) {
            return false;
        }

        if (tryStore(BukkitEnchantableItem.fromItemStack(equipment.get().getHelmet()), souls)) {
            return true;
        }
        if (tryStore(BukkitEnchantableItem.fromItemStack(equipment.get().getChestplate()), souls)) {
            return true;
        }
        if (tryStore(BukkitEnchantableItem.fromItemStack(equipment.get().getLeggings()), souls)) {
            return true;
        }
        if (tryStore(BukkitEnchantableItem.fromItemStack(equipment.get().getBoots()), souls)) {
            return true;
        }

        return false;
    }

    protected final boolean tryRemoveSouls(LivingEntity entity, int souls) { return tryAddSouls(entity, souls * -1); }

    private boolean tryStore(BukkitEnchantableItem item, int souls) {
        int level;
        try {
            level = api.getMaxLevel(AdvancedEnchantment.VORPAL, item);
        } catch (APIException ex) {
            logger.error(ex.getMessage(), ex);
            return false;
        }

        if (level < 0) {
            return false;
        }

        int newSouls = item.getSouls() + souls;

        if (newSouls >= 0 && newSouls <= level * 2) {
            item.setSouls(newSouls);
            return true;
        }
        return false;
    }
}
