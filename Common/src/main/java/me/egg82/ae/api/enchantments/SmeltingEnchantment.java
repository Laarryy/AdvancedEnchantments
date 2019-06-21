package me.egg82.ae.api.enchantments;

import java.util.UUID;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.AdvancedEnchantmentTarget;

public class SmeltingEnchantment extends AdvancedEnchantment {
    public SmeltingEnchantment() {
        super(UUID.randomUUID(), "smelting", "Smelting", false, 1, 1);
        targets.add(AdvancedEnchantmentTarget.TOOL);
    }
}
