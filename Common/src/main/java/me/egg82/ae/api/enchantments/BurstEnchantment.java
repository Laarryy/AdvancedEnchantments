package me.egg82.ae.api.enchantments;

import java.util.Arrays;
import java.util.UUID;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.AdvancedEnchantmentTarget;

public class BurstEnchantment extends AdvancedEnchantment {
    public BurstEnchantment() {
        super(UUID.randomUUID(), "burst", "Burst", false, 1, 3);
        targets.addAll(Arrays.asList(AdvancedEnchantmentTarget.BOW, AdvancedEnchantmentTarget.CROSSBOW));
        conflicts.add(AdvancedEnchantment.MULTISHOT);
    }
}
