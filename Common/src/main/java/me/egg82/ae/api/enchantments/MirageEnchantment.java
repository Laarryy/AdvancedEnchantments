package me.egg82.ae.api.enchantments;

import java.util.UUID;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.AdvancedEnchantmentTarget;

public class MirageEnchantment extends AdvancedEnchantment {
    public MirageEnchantment() {
        super(UUID.randomUUID(), "mirage", "Mirage", false, 1, 5);
        targets.add(AdvancedEnchantmentTarget.WEAPON);
    }
}
