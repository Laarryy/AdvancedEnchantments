package me.egg82.ae.api.enchantments;

import java.util.UUID;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.AdvancedEnchantmentTarget;

public class MagneticEnchantment extends AdvancedEnchantment {
    public MagneticEnchantment() {
        super(UUID.randomUUID(), "magnetic", "Magnetic", false, 1, 3);
        targets.add(AdvancedEnchantmentTarget.ARMOR);
    }
}
