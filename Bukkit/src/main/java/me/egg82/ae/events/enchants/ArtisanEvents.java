package me.egg82.ae.events.enchants;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import me.egg82.ae.APIException;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.BukkitEnchantableItem;
import me.egg82.ae.events.EventHolder;
import me.egg82.ae.services.CollectionProvider;
import me.egg82.ae.services.EnumFilter;
import me.egg82.ae.services.entity.EntityItemHandler;
import me.egg82.ae.services.material.MaterialLookup;
import me.egg82.ae.utils.BlockUtil;
import me.egg82.ae.utils.ItemDurabilityUtil;
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
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class ArtisanEvents extends EventHolder {
    private static final Material[] hoeMaterials;
    private static final Material dirtMaterial;
    private static final Material pathMaterial;
    private static final Material coarseDirtMaterial;

    static {
        hoeMaterials = EnumFilter.builder(Material.class).whitelist("_hoe").build();

        Optional<Material> m = MaterialLookup.get("DIRT");
        if (!m.isPresent()) {
            throw new RuntimeException("Could not get dirt material.");
        }
        dirtMaterial = m.get();

        m = MaterialLookup.get("GRASS_PATH");
        pathMaterial = m.isPresent() ? m.get() : null;

        m = MaterialLookup.get("COARSE_DIRT");
        coarseDirtMaterial = m.isPresent() ? m.get() : null;
    }

    public ArtisanEvents(Plugin plugin) {
        events.add(
                BukkitEvents.subscribe(plugin, BlockBreakEvent.class, EventPriority.MONITOR)
                        .filter(BukkitEventFilters.ignoreCancelled())
                        .filter(e -> !CollectionProvider.getArtisan().contains(e.getBlock().getLocation()))
                        .filter(e -> PermissionUtil.canUseEnchant(e.getPlayer(), "ae.enchant.artisan"))
                        .handler(this::blockBreak)
        );
        events.add(
                BukkitEvents.subscribe(plugin, PlayerInteractEvent.class, EventPriority.MONITOR)
                        .filter(BukkitEventFilters.ignoreCancelled())
                        .filter(PlayerInteractEvent::hasBlock)
                        .filter(e -> !CollectionProvider.getArtisan().contains(e.getClickedBlock().getLocation()))
                        .filter(e -> PermissionUtil.canUseEnchant(e.getPlayer(), "ae.enchant.artisan"))
                        .handler(this::hoeInteract)
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
            hasEnchantment = api.anyHasEnchantment(AdvancedEnchantment.ARTISAN, enchantableMainHand);
            level = api.getMaxLevel(AdvancedEnchantment.ARTISAN, enchantableMainHand);
        } catch (APIException ex) {
            logger.error(ex.getMessage(), ex);
            return;
        }

        if (!hasEnchantment || level <= 0) {
            return;
        }

        Set<Location> blockLocations = getSimilarBlocks(event.getBlock(), level + 1, new HashSet<>());

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

    private void hoeInteract(PlayerInteractEvent event) {
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

        Material type = mainHand.get().getType();

        boolean good = false;
        for (Material m : hoeMaterials) {
            if (type == m) {
                good = true;
            }
        }
        if (!good) {
            return;
        }

        Set<Location> blockLocations = getSimilarBlocks(event.getClickedBlock(), level + 1, new HashSet<>());

        Location originalLocation = event.getClickedBlock().getLocation();

        for (Location location : blockLocations) {
            if (location.equals(originalLocation)) {
                continue;
            }

            Block block = location.getBlock();

            CollectionProvider.getArtisan().add(location);

            PlayerInteractEvent e = new PlayerInteractEvent(event.getPlayer(), event.getAction(), event.getItem(), block, event.getBlockFace(), event.getHand());
            Bukkit.getPluginManager().callEvent(e);

            CollectionProvider.getArtisan().remove(location);

            if (!e.isCancelled()) {
                if (block.getType() == dirtMaterial || block.getType() == Material.GRASS_BLOCK || block.getType() == pathMaterial) {
                    block.setType(Material.FARMLAND);
                } else if (block.getType() == coarseDirtMaterial) {
                    block.setType(dirtMaterial);
                }
            }
        }

        if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
            if (!ItemDurabilityUtil.removeDurability(event.getPlayer(), enchantableMainHand, blockLocations.size() - 1, event.getPlayer().getLocation())) {
                entityItemHandler.setItemInMainHand(event.getPlayer(), null);
            }
        }
    }

    private Set<Location> getSimilarBlocks(Block block, int depth, Set<Location> walked) {
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

            retVal.addAll(getSimilarBlocks(b, depth - 1, walked));
        }

        return retVal;
    }
}
