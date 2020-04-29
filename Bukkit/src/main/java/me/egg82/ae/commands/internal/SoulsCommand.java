package me.egg82.ae.commands.internal;

import co.aikar.commands.CommandIssuer;
import java.util.Optional;
import me.egg82.ae.api.*;
import me.egg82.ae.enums.Message;
import me.egg82.ae.services.entity.EntityItemHandler;
import ninja.egg82.service.ServiceLocator;
import ninja.egg82.service.ServiceNotFoundException;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SoulsCommand implements Runnable {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private final CommandIssuer issuer;
    private final int souls;

    public SoulsCommand(CommandIssuer issuer, int souls) {
        this.issuer = issuer;
        this.souls = souls;
    }

    public void run() {
        if (souls < 0) {
            issuer.sendError(Message.SOULS__ERROR_MIN);
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
            enchantableMainHand.get().setSouls(souls);
            issuer.sendInfo(Message.SOULS__SUCCESS_MAIN_HAND, "{souls}", String.valueOf(souls));
        } else if (enchantableOffHand.isPresent()) {
            enchantableOffHand.get().setSouls(souls);
            issuer.sendInfo(Message.SOULS__SUCCESS_OFF_HAND, "{souls}", String.valueOf(souls));
        } else {
            issuer.sendError(Message.ERROR__NO_ITEM);
        }
    }
}
