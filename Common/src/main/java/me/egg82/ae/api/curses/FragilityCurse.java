package me.egg82.ae.api.curses;

import java.util.UUID;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.AdvancedEnchantmentTarget;

public class FragilityCurse extends AdvancedEnchantment {
    public FragilityCurse() {
        super(UUID.randomUUID(), "fragility_curse", "Curse of Fragility", true, 1, 5);
        targets.add(AdvancedEnchantmentTarget.ARMOR);
    }
}
