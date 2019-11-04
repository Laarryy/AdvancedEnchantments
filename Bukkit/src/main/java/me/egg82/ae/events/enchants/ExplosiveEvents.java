package me.egg82.ae.events.enchants;

import java.util.List;
import java.util.Optional;
import me.egg82.ae.APIException;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.BukkitEnchantableItem;
import me.egg82.ae.events.EventHolder;
import me.egg82.ae.services.CollectionProvider;
import me.egg82.ae.services.entity.EntityItemHandler;
import me.egg82.ae.utils.BlockUtil;
import me.egg82.ae.utils.ItemDurabilityUtil;
import me.egg82.ae.utils.LocationUtil;
import me.egg82.ae.utils.PermissionUtil;
import ninja.egg82.events.BukkitEventFilters;
import ninja.egg82.events.BukkitEvents;
import ninja.egg82.service.ServiceLocator;
import ninja.egg82.service.ServiceNotFoundException;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class ExplosiveEvents extends EventHolder {
    public ExplosiveEvents(Plugin plugin) {
        events.add(
                BukkitEvents.subscribe(plugin, BlockBreakEvent.class, EventPriority.NORMAL)
                        .filter(BukkitEventFilters.ignoreCancelled())
                        .filter(e -> !CollectionProvider.getExplosive().contains(e.getBlock().getLocation()))
                        .filter(e -> PermissionUtil.canUseEnchant(e.getPlayer(), "ae.enchant.explosive"))
                        .filter(e -> e.getBlock().getType().isBlock())
                        .handler(this::blockBreak)
        );
    }

    private void blockBreak(BlockBreakEvent event) {
        EntityItemHandler entityItemHandler;
        try {
            entityItemHandler = ServiceLocator.get(EntityItemHandler.class);
        } catch (InstantiationException | IllegalAccessException | ServiceNotFoundException ex) {
            logger.error(ex.getMessage(), ex);
            return;
        }

        Optional<ItemStack> mainHand = entityItemHandler.getItemInMainHand(event.getPlayer());
        BukkitEnchantableItem enchantableMainHand = mainHand.isPresent() ? BukkitEnchantableItem.fromItemStack(mainHand.get()) : null;

        boolean hasEnchantment;
        int level;
        try {
            hasEnchantment = api.anyHasEnchantment(AdvancedEnchantment.EXPLOSIVE, enchantableMainHand);
            level = api.getMaxLevel(AdvancedEnchantment.EXPLOSIVE, enchantableMainHand);
        } catch (APIException ex) {
            logger.error(ex.getMessage(), ex);
            return;
        }

        if (!hasEnchantment || level <= 0) {
            return;
        }

        BlockFace facing = LocationUtil.getFacingDirection(event.getPlayer().getLocation().getYaw(), true);
        List<Block> facingBlocks = event.getPlayer().getLastTwoTargetBlocks(null, 5);
        if (facingBlocks.size() == 2) {
            facing = facingBlocks.get(1).getFace(facingBlocks.get(0));
        }

        List<Block> blocks;
        if (facing == BlockFace.NORTH || facing == BlockFace.SOUTH) {
            blocks = BlockUtil.getBlocks(event.getBlock().getLocation(), level, level, 0);
        } else if (facing == BlockFace.UP || facing == BlockFace.DOWN) {
            blocks = BlockUtil.getBlocks(event.getBlock().getLocation(), level, 0, level);
        } else {
            blocks = BlockUtil.getBlocks(event.getBlock().getLocation(), 0, level, level);
        }

        int blockCount = 1;
        Location originalLocation = event.getBlock().getLocation();
        float originalHardness = event.getBlock().getType().getHardness();

        for (Block block : blocks) {
            Location location = block.getLocation();
            Material type = block.getType();

            if (location.equals(originalLocation)) {
                continue;
            }
            if (!type.isBlock() || type.getHardness() <= 0.0f || type.getHardness() > originalHardness) {
                continue;
            }

            if (block.getDrops(mainHand.get()).isEmpty()) {
                continue;
            }

            CollectionProvider.getExplosive().add(location);

            BlockBreakEvent e = new BlockBreakEvent(block, event.getPlayer());
            Bukkit.getPluginManager().callEvent(e);

            CollectionProvider.getExplosive().remove(location);

            if (!e.isCancelled()) {
                if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
                    block.breakNaturally(mainHand.get());
                } else {
                    block.setType(Material.AIR);
                }
                blockCount++;
            }
        }

        if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
            if (!ItemDurabilityUtil.removeDurability(event.getPlayer(), enchantableMainHand, blockCount, event.getPlayer().getLocation())) {
                entityItemHandler.setItemInMainHand(event.getPlayer(), null);
            }
        }
    }
}
