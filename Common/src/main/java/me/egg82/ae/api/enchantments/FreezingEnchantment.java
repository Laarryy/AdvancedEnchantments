package me.egg82.ae.api.enchantments;

import java.util.UUID;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.AdvancedEnchantmentTarget;

public class FreezingEnchantment extends AdvancedEnchantment {
    public FreezingEnchantment() {
        super(UUID.randomUUID(), "freezing", "Freezing", false, 1, 5);
        targets.add(AdvancedEnchantmentTarget.WEAPON);
    }
}
