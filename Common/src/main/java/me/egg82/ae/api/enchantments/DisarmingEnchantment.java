package me.egg82.ae.api.enchantments;

import java.util.UUID;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.AdvancedEnchantmentTarget;

public class DisarmingEnchantment extends AdvancedEnchantment {
    public DisarmingEnchantment() {
        super(UUID.randomUUID(), "disarming", "Disarming", false, 1, 5);
        targets.add(AdvancedEnchantmentTarget.WEAPON);
    }
}
