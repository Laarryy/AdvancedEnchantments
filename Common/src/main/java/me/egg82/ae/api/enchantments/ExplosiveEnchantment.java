package me.egg82.ae.api.enchantments;

import java.util.UUID;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.AdvancedEnchantmentTarget;

public class ExplosiveEnchantment extends AdvancedEnchantment {
    public ExplosiveEnchantment() {
        super(UUID.randomUUID(), "explosive", "Explosive", false, 1, 3);
        targets.add(AdvancedEnchantmentTarget.TOOL);
        conflicts.add(AdvancedEnchantment.ARTISAN);
    }
}
