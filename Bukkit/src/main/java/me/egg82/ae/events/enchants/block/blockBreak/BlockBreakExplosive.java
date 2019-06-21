package me.egg82.ae.events.enchants.block.blockBreak;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import me.egg82.ae.APIException;
import me.egg82.ae.EnchantAPI;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.BukkitEnchantableItem;
import me.egg82.ae.api.GenericEnchantableItem;
import me.egg82.ae.services.CollectionProvider;
import me.egg82.ae.services.entity.EntityItemHandler;
import me.egg82.ae.utils.BlockUtil;
import me.egg82.ae.utils.ItemDurabilityUtil;
import me.egg82.ae.utils.LocationUtil;
import ninja.egg82.service.ServiceLocator;
import ninja.egg82.service.ServiceNotFoundException;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockBreakExplosive implements Consumer<BlockBreakEvent> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private EnchantAPI api = EnchantAPI.getInstance();

    public BlockBreakExplosive() { }

    public void accept(BlockBreakEvent event) {
        EntityItemHandler entityItemHandler;
        try {
            entityItemHandler = ServiceLocator.get(EntityItemHandler.class);
        } catch (InstantiationException | IllegalAccessException | ServiceNotFoundException ex) {
            logger.error(ex.getMessage(), ex);
            return;
        }

        Optional<ItemStack> mainHand = entityItemHandler.getItemInMainHand(event.getPlayer());
        GenericEnchantableItem enchantableMainHand = mainHand.isPresent() ? BukkitEnchantableItem.fromItemStack(mainHand.get()) : null;

        int level;
        try {
            level = api.getMaxLevel(AdvancedEnchantment.EXPLOSIVE, enchantableMainHand);
        } catch (APIException ex) {
            logger.error(ex.getMessage(), ex);
            return;
        }

        if (level < 0) {
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

        for (Block block : blocks) {
            Location l = block.getLocation();
            Block b = l.getBlock();

            if (l.equals(event.getBlock().getLocation())) {
                continue;
            }
            if (block.getType() == null || block.getType() == Material.AIR || block.getType() == Material.BEDROCK || block.getType() == Material.BARRIER) {
                continue;
            }

            Collection<ItemStack> drops = b.getDrops(mainHand.get());
            if (drops.isEmpty()) {
                continue;
            }

            CollectionProvider.getExplosive().add(l);

            BlockBreakEvent e = new BlockBreakEvent(b, event.getPlayer());
            Bukkit.getPluginManager().callEvent(e);

            CollectionProvider.getExplosive().add(event.getBlock().getLocation());

            if (!e.isCancelled()) {
                if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
                    b.breakNaturally(mainHand.get());
                } else {
                    b.setType(Material.AIR);
                }
                blockCount++;
            }
        }

        if (event.getPlayer().getGameMode() != GameMode.CREATIVE && !ItemDurabilityUtil.removeDurability(mainHand.get(), blockCount, event.getPlayer().getLocation())) {
            entityItemHandler.setItemInMainHand(event.getPlayer(), null);
        }
    }
}
