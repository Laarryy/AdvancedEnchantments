package me.egg82.ae.events.enchants.block.blockBreak;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import me.egg82.ae.APIException;
import me.egg82.ae.EnchantAPI;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.BukkitEnchantableItem;
import me.egg82.ae.services.entity.EntityItemHandler;
import me.egg82.ae.utils.ItemDurabilityUtil;
import ninja.egg82.service.ServiceLocator;
import ninja.egg82.service.ServiceNotFoundException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockBreakSmelting implements Consumer<BlockBreakEvent> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private EnchantAPI api = EnchantAPI.getInstance();

    private static final List<FurnaceRecipe> recipes = new ArrayList<>();

    static {
        for (Iterator<Recipe> i = Bukkit.recipeIterator(); i.hasNext();) {
            Recipe r = i.next();
            if (r instanceof FurnaceRecipe) {
                recipes.add((FurnaceRecipe) r);
            }
        }
    }

    public BlockBreakSmelting() { }

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
        try {
            hasEnchantment = api.anyHasEnchantment(AdvancedEnchantment.SMELTING, enchantableMainHand);
        } catch (APIException ex) {
            logger.error(ex.getMessage(), ex);
            return;
        }

        if (!hasEnchantment) {
            return;
        }

        Location dropLoc = event.getBlock().getLocation();
        dropLoc.add(0.5d, 0.5d, 0.5d);

        boolean isSmelted = false;

        for (ItemStack i : event.getBlock().getDrops(mainHand.get())) {
            boolean dropped = false;

            for (FurnaceRecipe r : recipes) {
                if (i.getType() != r.getInput().getType()) {
                    continue;
                }

                short targetDurability = r.getInput().getDurability();
                if (targetDurability != Short.MAX_VALUE && targetDurability != Short.MIN_VALUE) {
                    if (i.getDurability() != targetDurability) {
                        continue;
                    }
                }

                ItemStack result = r.getResult().clone();
                result.setAmount(i.getAmount());
                event.getPlayer().getWorld().dropItemNaturally(dropLoc, result);
                dropped = true;
                break;
            }

            if (!dropped) {
                event.getPlayer().getWorld().dropItemNaturally(dropLoc, i);
            } else {
                isSmelted = true;
            }
        }

        if (isSmelted) {
            // Don't drop exp
            event.setCancelled(true);
            event.getBlock().setType(Material.AIR, true);
        }

        if (!ItemDurabilityUtil.removeDurability(event.getPlayer(), enchantableMainHand, (isSmelted) ? 2 : 1, event.getPlayer().getLocation())) {
            entityItemHandler.setItemInMainHand(event.getPlayer(), null);
        }
    }
}
