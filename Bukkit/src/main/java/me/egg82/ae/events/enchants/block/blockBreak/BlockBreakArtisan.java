package me.egg82.ae.events.enchants.block.blockBreak;

import java.util.*;
import java.util.function.Consumer;
import me.egg82.ae.APIException;
import me.egg82.ae.EnchantAPI;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.BukkitEnchantableItem;
import me.egg82.ae.services.CollectionProvider;
import me.egg82.ae.services.entity.EntityItemHandler;
import me.egg82.ae.utils.BlockUtil;
import me.egg82.ae.utils.ItemDurabilityUtil;
import ninja.egg82.service.ServiceLocator;
import ninja.egg82.service.ServiceNotFoundException;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockBreakArtisan implements Consumer<BlockBreakEvent> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private EnchantAPI api = EnchantAPI.getInstance();

    public BlockBreakArtisan() { }

    public void accept(BlockBreakEvent event) {
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
            hasEnchantment = api.anyHasEnchantment(AdvancedEnchantment.ARTISAN, enchantableMainHand);
            level = api.getMaxLevel(AdvancedEnchantment.ARTISAN, enchantableMainHand);
        } catch (APIException ex) {
            logger.error(ex.getMessage(), ex);
            return;
        }

        if (!hasEnchantment || level <= 0) {
            return;
        }

        Set<Location> blockLocations = getSimilar(event.getBlock(), level + 1, new HashSet<>());

        Location originalLocation = event.getBlock().getLocation();

        for (Location location : blockLocations) {
            if (location.equals(originalLocation)) {
                continue;
            }

            Block block = location.getBlock();

            CollectionProvider.getArtisan().add(location);

            BlockBreakEvent e = new BlockBreakEvent(block, event.getPlayer());
            Bukkit.getPluginManager().callEvent(e);

            CollectionProvider.getArtisan().remove(location);

            if (!e.isCancelled()) {
                if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
                    block.breakNaturally(mainHand.get());
                } else {
                    block.setType(Material.AIR);
                }
            }
        }

        if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
            if (!ItemDurabilityUtil.removeDurability(event.getPlayer(), enchantableMainHand, blockLocations.size() - 1, event.getPlayer().getLocation())) {
                entityItemHandler.setItemInMainHand(event.getPlayer(), null);
            }
        }
    }

    private Set<Location> getSimilar(Block block, int depth, Set<Location> walked) {
        Set<Location> retVal = new HashSet<>();

        if (depth <= 0) {
            return retVal;
        }

        if (!walked.add(block.getLocation())) {
            return retVal;
        }

        retVal.add(block.getLocation());

        Material searchType = block.getType();
        byte searchData = block.getData();

        for (Block b : BlockUtil.getBlocks(block.getLocation(), 1, 1, 1)) {
            Material type = b.getType();

            if (type == Material.AIR || type == Material.BEDROCK || type == Material.BARRIER) {
                continue;
            }
            if (type != searchType || b.getData() != searchData) {
                continue;
            }

            retVal.addAll(getSimilar(b, depth - 1, walked));
        }

        return retVal;
    }
}
