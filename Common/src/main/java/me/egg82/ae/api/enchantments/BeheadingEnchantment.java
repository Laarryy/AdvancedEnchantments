package me.egg82.ae.api.enchantments;

import java.util.UUID;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.AdvancedEnchantmentTarget;

public class BeheadingEnchantment extends AdvancedEnchantment {
    public BeheadingEnchantment() {
        super(UUID.randomUUID(), "beheading", "Beheading", false, 1, 3);
        targets.add(AdvancedEnchantmentTarget.WEAPON);
    }
}
