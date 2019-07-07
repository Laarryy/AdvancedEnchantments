package me.egg82.ae.api.enchantments;

import java.util.UUID;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.AdvancedEnchantmentTarget;

public class AntigravityEnchantment extends AdvancedEnchantment {
    public AntigravityEnchantment() {
        super(UUID.randomUUID(), "antigravity", "Antigravity", false, 1, 3);
        targets.add(AdvancedEnchantmentTarget.ARMOR_FEET);
    }
}
