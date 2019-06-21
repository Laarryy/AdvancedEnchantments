package me.egg82.ae.api.curses;

import java.util.UUID;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.AdvancedEnchantmentTarget;

public class LeechingCurse extends AdvancedEnchantment {
    public LeechingCurse() {
        super(UUID.randomUUID(), "leeching_curse", "Curse of Leeching", true, 1, 5);
        targets.add(AdvancedEnchantmentTarget.WEAPON);
    }
}
