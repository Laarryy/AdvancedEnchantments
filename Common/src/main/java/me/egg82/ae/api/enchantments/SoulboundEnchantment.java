package me.egg82.ae.api.enchantments;

import java.util.UUID;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.AdvancedEnchantmentTarget;

public class SoulboundEnchantment extends AdvancedEnchantment {
    public SoulboundEnchantment() {
        super(UUID.randomUUID(), "soulbound", "Soulbound", false, 1, 1);
        targets.add(AdvancedEnchantmentTarget.ALL);
        conflicts.add(AdvancedEnchantment.REPAIRING);
    }
}
