package me.egg82.ae.api.curses;

import java.util.Arrays;
import java.util.UUID;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.AdvancedEnchantmentTarget;

public class VoidCurse extends AdvancedEnchantment {
    public VoidCurse() {
        super(UUID.randomUUID(), "void_curse", "Curse of the Void", true, 1, 3);
        targets.addAll(Arrays.asList(AdvancedEnchantmentTarget.WEAPON, AdvancedEnchantmentTarget.ARMOR));
    }
}
