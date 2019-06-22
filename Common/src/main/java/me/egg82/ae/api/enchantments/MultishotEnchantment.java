package me.egg82.ae.api.enchantments;

import java.util.Arrays;
import java.util.UUID;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.AdvancedEnchantmentTarget;

public class MultishotEnchantment extends AdvancedEnchantment {
    public MultishotEnchantment() {
        super(UUID.randomUUID(), "multishot", "Multishot", false, 1, 3);
        targets.addAll(Arrays.asList(AdvancedEnchantmentTarget.BOW, AdvancedEnchantmentTarget.CROSSBOW));
    }
}
