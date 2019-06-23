package me.egg82.ae.api.curses;

import java.util.Arrays;
import java.util.UUID;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.AdvancedEnchantmentTarget;

public class CallingCurse extends AdvancedEnchantment {
    public CallingCurse() {
        super(UUID.randomUUID(), "calling_curse", "Curse of Calling", true, 1, 5);
        targets.addAll(Arrays.asList(AdvancedEnchantmentTarget.WEAPON, AdvancedEnchantmentTarget.ARMOR));
    }
}
