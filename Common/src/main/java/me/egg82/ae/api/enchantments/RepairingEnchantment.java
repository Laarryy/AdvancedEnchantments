package me.egg82.ae.api.enchantments;

import java.util.UUID;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.AdvancedEnchantmentTarget;

public class RepairingEnchantment extends AdvancedEnchantment {
    public RepairingEnchantment() {
        super(UUID.randomUUID(), "repairing", "Repairing", false, 1, 3);
        targets.add(AdvancedEnchantmentTarget.BREAKABLE);
    }
}
