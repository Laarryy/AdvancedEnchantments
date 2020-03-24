package me.egg82.ae.api.enchantments;

import java.util.UUID;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.AdvancedEnchantmentTarget;

public class PoisonousEnchantment extends AdvancedEnchantment {
    public PoisonousEnchantment() {
        super(UUID.randomUUID(), "poisonous", "Poisonous", false, 1, 5);
        targets.add(AdvancedEnchantmentTarget.WEAPON);
        conflicts.add(AdvancedEnchantment.BLINDING);
    }
}
