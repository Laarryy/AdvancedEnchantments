package me.egg82.aee.hooks;

import com.djrapitops.plan.capability.CapabilityService;
import com.djrapitops.plan.extension.CallEvents;
import com.djrapitops.plan.extension.DataExtension;
import com.djrapitops.plan.extension.ExtensionService;
import com.djrapitops.plan.extension.annotation.PluginInfo;
import com.djrapitops.plan.extension.icon.Color;
import com.djrapitops.plan.extension.icon.Family;
import java.util.*;
import me.egg82.ae.EnchantAPI;
import me.egg82.ae.api.*;
import me.egg82.ae.hooks.PluginHook;
import me.egg82.ae.services.entity.EntityItemHandler;
import ninja.egg82.service.ServiceLocator;
import ninja.egg82.service.ServiceNotFoundException;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerAnalyticsHook implements PluginHook {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final CapabilityService capabilities;

    public PlayerAnalyticsHook() {
        capabilities = CapabilityService.getInstance();

        if (isCapabilityAvailable("DATA_EXTENSION_VALUES")) {
            try {
                ExtensionService.getInstance().register(new Data());
            } catch (NoClassDefFoundError ex) {
                // Plan not installed
                logger.error("Plan is not installed.", ex);
            } catch (IllegalStateException ex) {
                // Plan not enabled
                logger.error("Plan is not enabled.", ex);
            } catch (IllegalArgumentException ex) {
                // DataExtension impl error
                logger.error("DataExtension implementation exception.", ex);
            }
        }
    }

    public void cancel() { }

    private boolean isCapabilityAvailable(String capability) {
        try {
            return capabilities.hasCapability(capability);
        } catch (NoClassDefFoundError ignored) {
            return false;
        }
    }

    @PluginInfo(
            name = "AdvancedEnchantments-Extras",
            iconName = "book",
            iconFamily = Family.SOLID,
            color = Color.PURPLE
    )
    class Data implements DataExtension {
        private final EnchantAPI api = EnchantAPI.getInstance();
        private final CallEvents[] events = new CallEvents[] { };
        private EntityItemHandler entityItemHandler;
        private final BukkitEnchantment durability = BukkitEnchantment.fromEnchant(Enchantment.DURABILITY);

        private Data() {
            try {
                entityItemHandler = ServiceLocator.get(EntityItemHandler.class);
            } catch (InstantiationException | IllegalAccessException | ServiceNotFoundException ex) {
                logger.error(ex.getMessage(), ex);
            }
        }

        private Set<GenericEnchantableItem> getItems(Player player) {
            Set<GenericEnchantableItem> retVal = new HashSet<>();

            Optional<ItemStack> mainHand = entityItemHandler.getItemInMainHand(player);
            mainHand.ifPresent(itemStack -> retVal.add(BukkitEnchantableItem.fromItemStack(itemStack)));
            Optional<ItemStack> offHand = entityItemHandler.getItemInOffHand(player);
            offHand.ifPresent(itemStack -> retVal.add(BukkitEnchantableItem.fromItemStack(itemStack)));

            Optional<EntityEquipment> equipment = Optional.ofNullable(player.getEquipment());
            if (!equipment.isPresent()) {
                return retVal;
            }

            retVal.add(BukkitEnchantableItem.fromItemStack(equipment.get().getHelmet()));
            retVal.add(BukkitEnchantableItem.fromItemStack(equipment.get().getChestplate()));
            retVal.add(BukkitEnchantableItem.fromItemStack(equipment.get().getLeggings()));
            retVal.add(BukkitEnchantableItem.fromItemStack(equipment.get().getBoots()));
            retVal.remove(null);
            return retVal;
        }

        public CallEvents[] callExtensionMethodsOn() { return events; }
    }
}
