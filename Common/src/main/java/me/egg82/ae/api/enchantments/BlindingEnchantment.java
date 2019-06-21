package me.egg82.ae.api.enchantments;

import java.util.UUID;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.AdvancedEnchantmentTarget;

public class BlindingEnchantment extends AdvancedEnchantment {
    public BlindingEnchantment() {
        super(UUID.randomUUID(), "blinding", "Blinding", false, 1, 5);
        targets.add(AdvancedEnchantmentTarget.WEAPON);
    }
}
