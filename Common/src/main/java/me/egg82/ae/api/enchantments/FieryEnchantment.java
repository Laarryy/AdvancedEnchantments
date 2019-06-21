package me.egg82.ae.api.enchantments;

import java.util.Arrays;
import java.util.UUID;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.AdvancedEnchantmentTarget;

public class FieryEnchantment extends AdvancedEnchantment {
    public FieryEnchantment() {
        super(UUID.randomUUID(), "fiery", "Fiery", false, 1, 1);
        targets.addAll(Arrays.asList(AdvancedEnchantmentTarget.BOW, AdvancedEnchantmentTarget.CROSSBOW));
    }
}
