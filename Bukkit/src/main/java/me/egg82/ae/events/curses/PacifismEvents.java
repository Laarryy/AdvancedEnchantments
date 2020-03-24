package me.egg82.ae.events.curses;

import de.slikey.effectlib.EffectManager;
import java.util.Optional;
import me.egg82.ae.APIException;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.BukkitEnchantableItem;
import me.egg82.ae.api.GenericEnchantableItem;
import me.egg82.ae.effects.ParticleSplashEffect;
import me.egg82.ae.events.EventHolder;
import me.egg82.ae.services.entity.EntityItemHandler;
import me.egg82.ae.utils.ConfigUtil;
import me.egg82.ae.utils.EffectUtil;
import me.egg82.ae.utils.PermissionUtil;
import ninja.egg82.events.BukkitEventFilters;
import ninja.egg82.events.BukkitEvents;
import ninja.egg82.service.ServiceLocator;
import ninja.egg82.service.ServiceNotFoundException;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class PacifismEvents extends EventHolder {
    private final EffectManager effectManager;

    public PacifismEvents(Plugin plugin, EffectManager effectManager) {
        this.effectManager = effectManager;

        events.add(
                BukkitEvents.subscribe(plugin, EntityDamageByEntityEvent.class, EventPriority.LOW)
                        .filter(BukkitEventFilters.ignoreCancelled())
                        .filter(this::compatIgnoreCancelled)
                        .filter(e -> e.getDamager() instanceof LivingEntity)
                        .filter(e -> PermissionUtil.canUseEnchant(e.getDamager(), "ae.curse.pacifism"))
                        .handler(this::damage)
        );
    }

    private void damage(EntityDamageByEntityEvent event) {
        EntityItemHandler entityItemHandler;
        try {
            entityItemHandler = ServiceLocator.get(EntityItemHandler.class);
        } catch (InstantiationException | IllegalAccessException | ServiceNotFoundException ex) {
            logger.error(ex.getMessage(), ex);
            return;
        }

        LivingEntity from = (LivingEntity) event.getDamager();

        Optional<ItemStack> mainHand = entityItemHandler.getItemInMainHand(from);
        GenericEnchantableItem enchantableMainHand = mainHand.isPresent() ? BukkitEnchantableItem.fromItemStack(mainHand.get()) : null;

        boolean hasEnchantment;
        try {
            hasEnchantment = api.anyHasEnchantment(AdvancedEnchantment.PACIFISM_CURSE, enchantableMainHand);
        } catch (APIException ex) {
            logger.error(ex.getMessage(), ex);
            return;
        }

        if (!hasEnchantment) {
            return;
        }

        if (ConfigUtil.getParticlesOrFalse()) {
            ParticleSplashEffect effect = new ParticleSplashEffect(effectManager, Particle.CRIT);
            EffectUtil.start(effect, event.getEntity());
        }
        event.setCancelled(true);
    }
}
