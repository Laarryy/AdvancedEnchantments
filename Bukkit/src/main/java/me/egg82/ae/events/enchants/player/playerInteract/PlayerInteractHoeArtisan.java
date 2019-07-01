package me.egg82.ae.events.enchants.player.playerInteract;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import me.egg82.ae.APIException;
import me.egg82.ae.EnchantAPI;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.BukkitEnchantableItem;
import me.egg82.ae.api.GenericEnchantableItem;
import me.egg82.ae.services.CollectionProvider;
import me.egg82.ae.services.EnumFilter;
import me.egg82.ae.services.entity.EntityItemHandler;
import me.egg82.ae.services.material.MaterialLookup;
import me.egg82.ae.utils.BlockUtil;
import me.egg82.ae.utils.ItemDurabilityUtil;
import ninja.egg82.service.ServiceLocator;
import ninja.egg82.service.ServiceNotFoundException;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerInteractHoeArtisan implements Consumer<PlayerInteractEvent> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private EnchantAPI api = EnchantAPI.getInstance();

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

    public PlayerInteractHoeArtisan() { }

    public void accept(PlayerInteractEvent event) {
        EntityItemHandler entityItemHandler;
        try {
            entityItemHandler = ServiceLocator.get(EntityItemHandler.class);
        } catch (InstantiationException | IllegalAccessException | ServiceNotFoundException ex) {
            logger.error(ex.getMessage(), ex);
            return;
        }

        Optional<ItemStack> mainHand = entityItemHandler.getItemInMainHand(event.getPlayer());
        GenericEnchantableItem enchantableMainHand = mainHand.isPresent() ? BukkitEnchantableItem.fromItemStack(mainHand.get()) : null;

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

        Set<Location> blockLocations = getSimilar(event.getClickedBlock(), level + 1, new HashSet<>());

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
            if (!ItemDurabilityUtil.removeDurability(event.getPlayer(), mainHand.get(), blockLocations.size() - 1, event.getPlayer().getLocation())) {
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
