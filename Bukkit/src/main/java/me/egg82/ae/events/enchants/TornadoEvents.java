package me.egg82.ae.events.enchants;

import de.slikey.effectlib.EffectManager;
import de.slikey.effectlib.effect.TornadoEffect;
import java.util.Optional;
import me.egg82.ae.APIException;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.BukkitEnchantableItem;
import me.egg82.ae.api.GenericEnchantableItem;
import me.egg82.ae.events.EventHolder;
import me.egg82.ae.services.entity.EntityItemHandler;
import me.egg82.ae.utils.ConfigUtil;
import me.egg82.ae.utils.PermissionUtil;
import ninja.egg82.events.BukkitEventFilters;
import ninja.egg82.events.BukkitEvents;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

public class TornadoEvents extends EventHolder {
    private final Plugin plugin;
    private final EffectManager effectManager;

    public TornadoEvents(Plugin plugin, EffectManager effectManager) {
        this.plugin = plugin;
        this.effectManager = effectManager;

        events.add(
                BukkitEvents.subscribe(plugin, EntityDamageByEntityEvent.class, EventPriority.MONITOR)
                        .filter(BukkitEventFilters.ignoreCancelled())
                        .filter(e -> e.getDamager() instanceof LivingEntity)
                        .filter(e -> PermissionUtil.canUseEnchant(e.getDamager(), "ae.enchant.tornado"))
                        .handler(this::damage)
        );
    }

    private void damage(EntityDamageByEntityEvent event) {
        EntityItemHandler entityItemHandler = getItemHandler();
        if (entityItemHandler == null) {
            return;
        }

        LivingEntity from = (LivingEntity) event.getDamager();

        Optional<ItemStack> mainHand = entityItemHandler.getItemInMainHand(from);
        GenericEnchantableItem enchantableMainHand = mainHand.isPresent() ? BukkitEnchantableItem.fromItemStack(mainHand.get()) : null;

        boolean hasEnchantment;
        int level;
        try {
            hasEnchantment = api.anyHasEnchantment(AdvancedEnchantment.TORNADO, enchantableMainHand);
            level = api.getMaxLevel(AdvancedEnchantment.TORNADO, enchantableMainHand);
        } catch (APIException ex) {
            logger.error(ex.getMessage(), ex);
            return;
        }

        if (!hasEnchantment) {
            return;
        }

        if (ConfigUtil.getParticlesOrFalse()) {
            TornadoEffect effect = new TornadoEffect(effectManager);
            effect.setLocation(event.getEntity().getLocation());
            effect.tornadoParticle = Particle.SMOKE_NORMAL;
            effect.iterations = 2;
            effect.tornadoHeight = 1.75f;
            effect.maxTornadoRadius = 1.25f;
            effect.cloudSize = 0.75f;
            effect.circleParticles = 16;
            effect.cloudParticles = 20;
            effect.start();
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!event.getEntity().isDead()) {
                event.getEntity().setVelocity(new Vector(0.0d, 0.35d * level, 0.0d));
            }
        }, 1L);
    }
}
