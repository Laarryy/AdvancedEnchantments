package me.egg82.ae.api.enchantments;

import java.util.UUID;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.AdvancedEnchantmentTarget;

public class VorpalEnchantment extends AdvancedEnchantment {
    public VorpalEnchantment() {
        super(UUID.randomUUID(), "vorpal", "Vorpal", false, 1, 10);
        targets.add(AdvancedEnchantmentTarget.WEARABLE);
    }
}
