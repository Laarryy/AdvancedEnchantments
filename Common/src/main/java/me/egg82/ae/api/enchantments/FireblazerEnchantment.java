package me.egg82.ae.api.enchantments;

import java.util.UUID;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.AdvancedEnchantmentTarget;

public class FireblazerEnchantment extends AdvancedEnchantment {
    public FireblazerEnchantment() {
        super(UUID.randomUUID(), "fireblazer", "Fireblazer", false, 1, 1);
        targets.add(AdvancedEnchantmentTarget.ARMOR_FEET);
    }
}
