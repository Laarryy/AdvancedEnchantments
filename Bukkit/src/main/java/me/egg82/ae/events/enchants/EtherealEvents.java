package me.egg82.ae.events.enchants;

import de.slikey.effectlib.EffectManager;
import de.slikey.effectlib.EffectType;
import de.slikey.effectlib.effect.ShieldEffect;
import java.util.Optional;
import me.egg82.ae.APIException;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.BukkitEnchantableItem;
import me.egg82.ae.api.GenericEnchantableItem;
import me.egg82.ae.events.EventHolder;
import me.egg82.ae.utils.ConfigUtil;
import me.egg82.ae.utils.EffectUtil;
import me.egg82.ae.utils.PermissionUtil;
import me.egg82.ae.utils.SoulsUtil;
import ninja.egg82.events.BukkitEventFilters;
import ninja.egg82.events.BukkitEvents;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.plugin.Plugin;

public class EtherealEvents extends EventHolder {
    private final EffectManager effectManager;

    public EtherealEvents(Plugin plugin, EffectManager effectManager) {
        this.effectManager = effectManager;

        events.add(
                BukkitEvents.subscribe(plugin, EntityDamageByEntityEvent.class, EventPriority.LOW)
                        .filter(BukkitEventFilters.ignoreCancelled())
                        .filter(this::compatIgnoreCancelled)
                        .filter(e -> e.getEntity() instanceof LivingEntity)
                        .filter(e -> PermissionUtil.canUseEnchant(e.getEntity(), "ae.enchant.ethereal"))
                        .filter(e -> PermissionUtil.canUseEnchant(e.getEntity(), "ae.enchant.vorpal"))
                        .handler(this::damageByEntity)
        );
    }

    private void damageByEntity(EntityDamageByEntityEvent event) {
        Optional<EntityEquipment> equipment = Optional.ofNullable(((LivingEntity) event.getEntity()).getEquipment());
        if (!equipment.isPresent()) {
            return;
        }

        GenericEnchantableItem enchantableHelmet = BukkitEnchantableItem.fromItemStack(equipment.get().getHelmet());
        GenericEnchantableItem enchantableChestplate = BukkitEnchantableItem.fromItemStack(equipment.get().getChestplate());
        GenericEnchantableItem enchantableLeggings = BukkitEnchantableItem.fromItemStack(equipment.get().getLeggings());
        GenericEnchantableItem enchantableBoots = BukkitEnchantableItem.fromItemStack(equipment.get().getBoots());

        boolean hasEnchantment;
        try {
            hasEnchantment = api.anyHasEnchantment(AdvancedEnchantment.ETHEREAL,
                    enchantableHelmet,
                    enchantableChestplate,
                    enchantableLeggings,
                    enchantableBoots);
        } catch (APIException ex) {
            logger.error(ex.getMessage(), ex);
            return;
        }

        if (!hasEnchantment) {
            return;
        }

        if (!SoulsUtil.tryRemoveSouls((LivingEntity) event.getEntity(), 1)) {
            return;
        }

        if (ConfigUtil.getParticlesOrFalse()) {
            ShieldEffect effect = new ShieldEffect(effectManager);
            effect.particle = Particle.TOWN_AURA;
            effect.sphere = true;
            effect.radius = 1.0d;
            effect.particles = 35;
            effect.type = EffectType.INSTANT;
            effect.iterations = 1;
            EffectUtil.start(effect, event.getEntity());
        }

        event.setCancelled(true);
    }
}
