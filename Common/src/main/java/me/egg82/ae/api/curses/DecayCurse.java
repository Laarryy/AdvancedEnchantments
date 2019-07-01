package me.egg82.ae.api.curses;

import java.util.UUID;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.AdvancedEnchantmentTarget;

public class DecayCurse extends AdvancedEnchantment {
    public DecayCurse() {
        super(UUID.randomUUID(), "decay_curse", "Curse of Decay", true, 1, 5);
        targets.add(AdvancedEnchantmentTarget.BREAKABLE);
    }
}
