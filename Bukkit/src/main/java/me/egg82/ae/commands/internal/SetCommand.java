package me.egg82.ae.commands.internal;

import co.aikar.commands.CommandIssuer;
import java.util.Optional;
import me.egg82.ae.api.*;
import me.egg82.ae.enums.Message;
import me.egg82.ae.services.entity.EntityItemHandler;
import me.egg82.ae.utils.EnchantmentUtil;
import ninja.egg82.service.ServiceLocator;
import ninja.egg82.service.ServiceNotFoundException;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SetCommand implements Runnable {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private final CommandIssuer issuer;
    private final String enchant;
    private final String level;
    private final String force;

    public SetCommand(CommandIssuer issuer, String enchant, String level, String force) {
        this.issuer = issuer;
        this.enchant = enchant;
        this.level = level;
        this.force = force;
    }

    public void run() {
        Optional<GenericEnchantment> en = getEnchantment(enchant);

        if (!en.isPresent()) {
            issuer.sendError(Message.ERROR__ENCHANT_NOT_FOUND);
            return;
        }

        int l = level == null ? en.get().getMinLevel() : Integer.parseInt(level);
        boolean f = Boolean.parseBoolean(force);

        if (!f && l < en.get().getMinLevel()) {
            issuer.sendError(Message.SET__ERROR_LEVEL_MIN, "{level}", String.valueOf(en.get().getMinLevel()));
            return;
        }
        if (!f && l > en.get().getMaxLevel()) {
            issuer.sendError(Message.SET__ERROR_LEVEL_MAX, "{level}", String.valueOf(en.get().getMaxLevel()));
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
            if (!f && !en.get().canEnchant(enchantableMainHand.get())) {
                issuer.sendError(Message.SET__ERROR_CONFLICTS);
                return;
            }

            enchantableMainHand.get().setEnchantmentLevel(en.get(), l);
            issuer.sendInfo(Message.SET__SUCCESS_MAIN_HAND, "{name}", en.get().getFriendlyName(), "{level}", String.valueOf(l));
        } else if (enchantableOffHand.isPresent()) {
            if (!f && !en.get().canEnchant(enchantableOffHand.get())) {
                issuer.sendError(Message.SET__ERROR_CONFLICTS);
                return;
            }

            enchantableOffHand.get().setEnchantmentLevel(en.get(), l);
            issuer.sendInfo(Message.SET__SUCCESS_OFF_HAND, "{name}", en.get().getFriendlyName(), "{level}", String.valueOf(l));
        } else {
            issuer.sendError(Message.ERROR__NO_ITEM);
        }
    }

    private Optional<GenericEnchantment> getEnchantment(String enchantment) {
        for (AdvancedEnchantment e : AdvancedEnchantment.values()) {
            if (e != null && e.getName().equalsIgnoreCase(enchantment)) {
                return Optional.of(e);
            }
        }

        for (Enchantment e : Enchantment.values()) {
            if (e != null && EnchantmentUtil.getName(e).equalsIgnoreCase(enchantment)) {
                return Optional.of(BukkitEnchantment.fromEnchant(e));
            }
        }

        return Optional.empty();
    }
}
