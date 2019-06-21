package me.egg82.ae.api.enchantments;

import java.util.UUID;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.AdvancedEnchantmentTarget;

public class ChargingEnchantment extends AdvancedEnchantment {
    public ChargingEnchantment() {
        super(UUID.randomUUID(), "charging", "Charging", false, 1, 5);
        targets.add(AdvancedEnchantmentTarget.WEAPON);
    }
}
