package me.egg82.ae.api.enchantments;

import java.util.UUID;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.AdvancedEnchantmentTarget;

public class EnsnaringEnchantment extends AdvancedEnchantment {
    public EnsnaringEnchantment() {
        super(UUID.randomUUID(), "ensnaring", "Ensnaring", false, 1, 3);
        targets.add(AdvancedEnchantmentTarget.WEAPON);
    }
}
