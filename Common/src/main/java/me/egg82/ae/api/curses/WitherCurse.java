package me.egg82.ae.api.curses;

import java.util.Arrays;
import java.util.UUID;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.AdvancedEnchantmentTarget;

public class WitherCurse extends AdvancedEnchantment {
    public WitherCurse() {
        super(UUID.randomUUID(), "wither_curse", "Curse of Wither", true, 1, 5);
        targets.addAll(Arrays.asList(AdvancedEnchantmentTarget.WEAPON, AdvancedEnchantmentTarget.ARMOR));
    }
}
