package me.egg82.ae.events.enchants;

import de.slikey.effectlib.EffectManager;
import de.slikey.effectlib.effect.ArcEffect;
import de.slikey.effectlib.effect.LoveEffect;
import java.util.Optional;
import me.egg82.ae.APIException;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.BukkitEnchantableItem;
import me.egg82.ae.api.GenericEnchantableItem;
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
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class VampiricEvents extends EventHolder {
    private final EffectManager effectManager;

    public VampiricEvents(Plugin plugin, EffectManager effectManager) {
        this.effectManager = effectManager;

        events.add(
                BukkitEvents.subscribe(plugin, EntityDamageByEntityEvent.class, EventPriority.NORMAL)
                        .filter(BukkitEventFilters.ignoreCancelled())
                        .filter(this::compatIgnoreCancelled)
                        .filter(e -> e.getDamager() instanceof LivingEntity)
                        .filter(e -> PermissionUtil.canUseEnchant(e.getDamager(), "ae.enchant.vampiric"))
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

        LivingEntity to = (LivingEntity) event.getDamager();

        Optional<ItemStack> mainHand = entityItemHandler.getItemInMainHand(to);
        GenericEnchantableItem enchantableMainHand = mainHand.isPresent() ? BukkitEnchantableItem.fromItemStack(mainHand.get()) : null;

        boolean hasEnchantment;
        int level;
        try {
            hasEnchantment = api.anyHasEnchantment(AdvancedEnchantment.VAMPIRIC, enchantableMainHand);
            level = api.getMaxLevel(AdvancedEnchantment.VAMPIRIC, enchantableMainHand);
        } catch (APIException ex) {
            logger.error(ex.getMessage(), ex);
            return;
        }

        if (!hasEnchantment) {
            return;
        }

        Entity from = event.getEntity();

        if (ConfigUtil.getParticlesOrFalse()) {
            ArcEffect effect = new ArcEffect(effectManager);
            effect.particle = Particle.END_ROD;
            effect.iterations = 15;
            effect.height = 1.5f;
            effect.particles = 1;
            EffectUtil.start(effect, from, to);

            LoveEffect effect2 = new LoveEffect(effectManager);
            effect2.iterations = 7;
            EffectUtil.start(effect2, to);
        }

        double health = to.getHealth();
        double damage = Math.max(7.0d, event.getFinalDamage()) / 4.0d;
        health += damage - (damage / (level + 0.3333333333333334d));
        to.setHealth(Math.min(to.getMaxHealth(), health));
    }
}
