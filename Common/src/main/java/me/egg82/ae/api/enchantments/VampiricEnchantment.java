package me.egg82.ae.api.enchantments;

import java.util.UUID;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.AdvancedEnchantmentTarget;

public class VampiricEnchantment extends AdvancedEnchantment {
    public VampiricEnchantment() {
        super(UUID.randomUUID(), "vampiric", "Vampiric", false, 1, 5);
        targets.add(AdvancedEnchantmentTarget.WEAPON);
    }
}
