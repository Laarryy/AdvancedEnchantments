package me.egg82.ae.api.enchantments;

import java.util.Arrays;
import java.util.UUID;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.AdvancedEnchantmentTarget;

public class ProficiencyEnchantment extends AdvancedEnchantment {
    public ProficiencyEnchantment() {
        super(UUID.randomUUID(), "proficiency", "Proficiency", false, 1, 5);
        targets.addAll(Arrays.asList(AdvancedEnchantmentTarget.WEAPON, AdvancedEnchantmentTarget.TOOL));
    }
}
