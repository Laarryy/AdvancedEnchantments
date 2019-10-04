package me.egg82.ae.api.enchantments;

import java.util.UUID;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.AdvancedEnchantmentTarget;

public class TrailblazerEnchantment extends AdvancedEnchantment {
    public TrailblazerEnchantment() {
        super(UUID.randomUUID(), "trailblazer", "Trailblazer", false, 1, 1);
        targets.add(AdvancedEnchantmentTarget.ARMOR_FEET);
    }
}
