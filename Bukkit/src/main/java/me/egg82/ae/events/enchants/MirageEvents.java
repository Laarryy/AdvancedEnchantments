package me.egg82.ae.events.enchants;

import de.slikey.effectlib.EffectManager;
import java.util.*;
import me.egg82.ae.APIException;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.BukkitEnchantableItem;
import me.egg82.ae.core.FakeBlockData;
import me.egg82.ae.effects.ParticleSplashEffect;
import me.egg82.ae.events.EventHolder;
import me.egg82.ae.services.block.FakeBlockHandler;
import me.egg82.ae.services.entity.EntityItemHandler;
import me.egg82.ae.utils.BlockUtil;
import me.egg82.ae.utils.ConfigUtil;
import me.egg82.ae.utils.ItemDurabilityUtil;
import me.egg82.ae.utils.PermissionUtil;
import ninja.egg82.events.BukkitEventFilters;
import ninja.egg82.events.BukkitEvents;
import ninja.egg82.service.ServiceLocator;
import ninja.egg82.service.ServiceNotFoundException;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

public class MirageEvents extends EventHolder {
    private final Plugin plugin;
    private final EffectManager effectManager;

    public MirageEvents(Plugin plugin, EffectManager effectManager) {
        this.plugin = plugin;
        this.effectManager = effectManager;

        events.add(
                BukkitEvents.subscribe(plugin, PlayerAnimationEvent.class, EventPriority.MONITOR)
                        .filter(BukkitEventFilters.ignoreCancelled())
                        .filter(e -> e.getAnimationType() == PlayerAnimationType.ARM_SWING)
                        .filter(e -> PermissionUtil.canUseEnchant(e.getPlayer(), "ae.enchant.mirage"))
                        .handler(this::swing)
        );
    }

    private void swing(PlayerAnimationEvent event) {
        EntityItemHandler entityItemHandler = getItemHandler();
        if (entityItemHandler == null) {
            return;
        }

        Optional<ItemStack> mainHand = entityItemHandler.getItemInMainHand(event.getPlayer());
        BukkitEnchantableItem enchantableMainHand = mainHand.isPresent() ? BukkitEnchantableItem.fromItemStack(mainHand.get()) : null;

        boolean hasEnchantment;
        int level;
        try {
            hasEnchantment = api.anyHasEnchantment(AdvancedEnchantment.MIRAGE, enchantableMainHand);
            level = api.getMaxLevel(AdvancedEnchantment.MIRAGE, enchantableMainHand);
        } catch (APIException ex) {
            logger.error(ex.getMessage(), ex);
            return;
        }

        if (!hasEnchantment || level <= 0) {
            return;
        }

        Set<Block> blocks = new HashSet<>();
        for (int i = 0; i < level; i++) {
            blocks.addAll(BlockUtil.getHalfCircleAround(event.getPlayer().getEyeLocation(), i + 2, level + 2, 4));
        }

        if (raise(blocks, ConfigUtil.getParticlesOrFalse()) && event.getPlayer().getGameMode() != GameMode.CREATIVE) {
            if (!ItemDurabilityUtil.removeDurability(event.getPlayer(), enchantableMainHand, 1, event.getPlayer().getLocation())) {
                entityItemHandler.setItemInMainHand(event.getPlayer(), null);
            }
        }
    }

    private boolean raise(Set<Block> blocks, boolean particles) {
        FakeBlockHandler fakeBlockHandler;
        try {
            fakeBlockHandler = ServiceLocator.get(FakeBlockHandler.class);
        } catch (InstantiationException | IllegalAccessException | ServiceNotFoundException ex) {
            logger.error(ex.getMessage(), ex);
            return false;
        }

        boolean raised = false;

        Map<Block, FakeBlockData> fakeBlocks = new HashMap<>();
        Set<Location> topBlocks = new HashSet<>();

        for (Block b : blocks) {
            Location l = b.getLocation();

            Block above = l.clone().add(0.0d, 1.0d, 0.0d).getBlock();
            if (above.getType().isSolid()) {
                continue;
            }

            raised = true;

            Block below = l.clone().add(0.0d, -1.0d, 0.0d).getBlock();
            Material t = b.getType();

            fakeBlocks.put(above, new FakeBlockData(t, b.getData()));
            fakeBlocks.put(b, new FakeBlockData(below.getType(), below.getData()));
            fakeBlocks.put(below, new FakeBlockData(Material.AIR));

            if (particles) {
                Location al = above.getLocation();
                if (t.isSolid() && topBlocks.add(al)) {
                    ParticleSplashEffect effect = new ParticleSplashEffect(effectManager, Particle.BLOCK_DUST);
                    effect.material = t;
                    effect.setLocation(al);
                    effect.start();
                }
            }

            for (Entity e : l.getWorld().getNearbyEntities(l.clone().add(0.5d, 1.0d, 0.5d), 0.5d, 1.0d, 0.5d)) {
                if (!(e instanceof LivingEntity)) {
                    continue;
                }
                e.setVelocity(new Vector(0.0d, 0.55d, 0.0d));
            }
        }

        fakeBlockHandler.sendFake(fakeBlocks);
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> fakeBlockHandler.sendReal(fakeBlocks.keySet()), 70L);

        return raised;
    }
}
