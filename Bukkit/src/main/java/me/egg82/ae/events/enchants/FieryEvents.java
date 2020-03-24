package me.egg82.ae.events.enchants;

import de.slikey.effectlib.EffectManager;
import de.slikey.effectlib.effect.FlameEffect;
import java.util.Optional;
import me.egg82.ae.APIException;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.BukkitEnchantableItem;
import me.egg82.ae.api.GenericEnchantableItem;
import me.egg82.ae.events.EventHolder;
import me.egg82.ae.services.CollectionProvider;
import me.egg82.ae.services.entity.EntityItemHandler;
import me.egg82.ae.utils.ConfigUtil;
import me.egg82.ae.utils.EffectUtil;
import me.egg82.ae.utils.LocationUtil;
import me.egg82.ae.utils.PermissionUtil;
import ninja.egg82.events.BukkitEventFilters;
import ninja.egg82.events.BukkitEvents;
import ninja.egg82.service.ServiceLocator;
import ninja.egg82.service.ServiceNotFoundException;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class FieryEvents extends EventHolder {
    private final EffectManager effectManager;

    public FieryEvents(Plugin plugin, EffectManager effectManager) {
        this.effectManager = effectManager;

        events.add(
                BukkitEvents.subscribe(plugin, EntityShootBowEvent.class, EventPriority.NORMAL)
                        .filter(BukkitEventFilters.ignoreCancelled())
                        .filter(e -> PermissionUtil.canUseEnchant(e.getEntity(), "ae.enchant.fiery"))
                        .handler(this::shoot)
        );

        try {
            Class.forName("org.bukkit.event.entity.ProjectileHitEvent");
            events.add(
                    BukkitEvents.subscribe(plugin, ProjectileHitEvent.class, EventPriority.NORMAL)
                            .filter(e -> CollectionProvider.getFiery().remove(e.getEntity().getUniqueId()))
                            .handler(this::hit)
            );
        } catch (ClassNotFoundException ignored) {}
            events.add(
                    BukkitEvents.subscribe(plugin, EntityDamageByEntityEvent.class, EventPriority.NORMAL)
                            .filter(e -> CollectionProvider.getFiery().remove(e.getDamager().getUniqueId()))
                            .filter(BukkitEventFilters.ignoreCancelled())
                            .filter(this::compatIgnoreCancelled)
                            .handler(e -> e.getEntity().setFireTicks(50))
        );
    }

    private void shoot(EntityShootBowEvent event) {
        EntityItemHandler entityItemHandler;
        try {
            entityItemHandler = ServiceLocator.get(EntityItemHandler.class);
        } catch (InstantiationException | IllegalAccessException | ServiceNotFoundException ex) {
            logger.error(ex.getMessage(), ex);
            return;
        }

        Optional<ItemStack> mainHand = entityItemHandler.getItemInMainHand(event.getEntity());
        GenericEnchantableItem enchantableMainHand = mainHand.isPresent() ? BukkitEnchantableItem.fromItemStack(mainHand.get()) : null;

        boolean hasEnchantment;
        try {
            hasEnchantment = api.anyHasEnchantment(AdvancedEnchantment.FIERY, enchantableMainHand);
        } catch (APIException ex) {
            logger.error(ex.getMessage(), ex);
            return;
        }

        if (!hasEnchantment) {
            return;
        }

        if (ConfigUtil.getParticlesOrFalse()) {
            FlameEffect effect = new FlameEffect(effectManager);
            effect.infinite();
            EffectUtil.start(effect, event.getProjectile());
        }

        CollectionProvider.getFiery().add(event.getProjectile().getUniqueId());
    }

    private void hit(ProjectileHitEvent event) {
        Optional<Block> hitBlock = Optional.ofNullable(event.getHitBlock());
        Optional<Entity> hitEntity = Optional.ofNullable(event.getHitEntity());

        if (hitBlock.isPresent()) {
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        // Need a new loc each time so we don't screw up the math
                        Location l2 = hitBlock.get().getLocation().clone().add(x, y, z);
                        Material type = l2.getBlock().getType();
                        if (LocationUtil.canIgnite(type)) {
                            l2.getBlock().setType(Material.FIRE, true);
                        }
                    }
                }
            }
        }

        if (hitEntity.isPresent()) {
            hitEntity.get().setFireTicks(50);
        }
    }
}
