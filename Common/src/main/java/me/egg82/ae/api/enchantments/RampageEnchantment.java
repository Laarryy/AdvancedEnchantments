package me.egg82.ae.api.enchantments;

import java.util.UUID;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.AdvancedEnchantmentTarget;

public class RampageEnchantment extends AdvancedEnchantment {
    public RampageEnchantment() {
        super(UUID.randomUUID(), "rampage", "Rampage", false, 1, 3);
        targets.add(AdvancedEnchantmentTarget.WEAPON);
    }
}
