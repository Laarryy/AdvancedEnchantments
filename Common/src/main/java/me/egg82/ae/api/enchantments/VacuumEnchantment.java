package me.egg82.ae.api.enchantments;

import java.util.UUID;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.AdvancedEnchantmentTarget;

public class VacuumEnchantment extends AdvancedEnchantment {
    public VacuumEnchantment() {
        super(UUID.randomUUID(), "vacuum", "Vacuum", false, 1, 3);
        targets.add(AdvancedEnchantmentTarget.ARMOR);
    }
}
