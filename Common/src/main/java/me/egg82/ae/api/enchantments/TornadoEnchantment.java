package me.egg82.ae.api.enchantments;

import java.util.UUID;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.AdvancedEnchantmentTarget;

public class TornadoEnchantment extends AdvancedEnchantment {
    public TornadoEnchantment() {
        super(UUID.randomUUID(), "tornado", "Tornado", false, 1, 3);
        targets.add(AdvancedEnchantmentTarget.WEAPON);
    }
}
