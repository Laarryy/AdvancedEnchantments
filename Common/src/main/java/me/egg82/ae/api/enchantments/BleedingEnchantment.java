package me.egg82.ae.api.enchantments;

import java.util.UUID;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.AdvancedEnchantmentTarget;

public class BleedingEnchantment extends AdvancedEnchantment {
    public BleedingEnchantment() {
        super(UUID.randomUUID(), "bleeding", "Bleeding", false, 1, 5);
        targets.add(AdvancedEnchantmentTarget.WEAPON);
    }
}
