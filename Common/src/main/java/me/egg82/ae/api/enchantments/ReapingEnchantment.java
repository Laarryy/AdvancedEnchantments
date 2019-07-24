package me.egg82.ae.api.enchantments;

import java.util.UUID;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.AdvancedEnchantmentTarget;

public class ReapingEnchantment extends AdvancedEnchantment {
    public ReapingEnchantment() {
        super(UUID.randomUUID(), "reaping", "Reaping", false, 1, 1);
        targets.add(AdvancedEnchantmentTarget.WEAPON);
    }
}
