package me.egg82.ae.commands.internal;

import co.aikar.commands.CommandIssuer;
import java.util.Optional;
import me.egg82.ae.api.*;
import me.egg82.ae.enums.Message;
import me.egg82.ae.services.entity.EntityItemHandler;
import ninja.egg82.service.ServiceLocator;
import ninja.egg82.service.ServiceNotFoundException;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoveCommand implements Runnable {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private final CommandIssuer issuer;
    private final String enchant;

    public RemoveCommand(CommandIssuer issuer, String enchant) {
        this.issuer = issuer;
        this.enchant = enchant;
    }

    public void run() {
        Optional<GenericEnchantment> en = getEnchantment(enchant);

        if (!en.isPresent()) {
            issuer.sendError(Message.ERROR__ENCHANT_NOT_FOUND);
            return;
        }

        if (!(issuer.getIssuer() instanceof LivingEntity)) {
            issuer.sendError(Message.ERROR__NO_CONSOLE);
            return;
        }

        EntityItemHandler entityItemHandler;
        try {
            entityItemHandler = ServiceLocator.get(EntityItemHandler.class);
        } catch (InstantiationException | IllegalAccessException | ServiceNotFoundException ex) {
            logger.error(ex.getMessage(), ex);
            issuer.sendError(Message.ERROR__INTERNAL);
            return;
        }

        Optional<ItemStack> mainHand = entityItemHandler.getItemInMainHand(issuer.getIssuer());
        Optional<ItemStack> offHand = entityItemHandler.getItemInOffHand(issuer.getIssuer());

        Optional<GenericEnchantableItem> enchantableMainHand = Optional.ofNullable(mainHand.isPresent() ? BukkitEnchantableItem.fromItemStack(mainHand.get()) : null);
        Optional<GenericEnchantableItem> enchantableOffHand = Optional.ofNullable(offHand.isPresent() ? BukkitEnchantableItem.fromItemStack(offHand.get()) : null);

        if (enchantableMainHand.isPresent()) {
            enchantableMainHand.get().removeEnchantment(en.get());
            issuer.sendInfo(Message.REMOVE__SUCCESS_MAIN_HAND, "{name}", en.get().getFriendlyName());
        } else if (enchantableOffHand.isPresent()) {
            enchantableOffHand.get().removeEnchantment(en.get());
            issuer.sendInfo(Message.REMOVE__SUCCESS_OFF_HAND, "{name}", en.get().getFriendlyName());
        } else {
            issuer.sendError(Message.ERROR__NO_ITEM);
        }
    }

    private Optional<GenericEnchantment> getEnchantment(String enchantment) {
        for (Enchantment e : Enchantment.values()) {
            if (e.getName().equalsIgnoreCase(enchantment)) {
                return Optional.of(BukkitEnchantment.fromEnchant(e));
            }
        }

        for (AdvancedEnchantment e : AdvancedEnchantment.values()) {
            if (e.getName().equalsIgnoreCase(enchantment)) {
                return Optional.of(e);
            }
        }

        return Optional.empty();
    }
}
