package me.egg82.ae.api.curses;

import java.util.Arrays;
import java.util.UUID;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.AdvancedEnchantmentTarget;

public class TreasonCurse extends AdvancedEnchantment {
    public TreasonCurse() {
        super(UUID.randomUUID(), "treason_curse", "Curse of Treason", true, 1, 3);
        targets.addAll(Arrays.asList(AdvancedEnchantmentTarget.WEAPON, AdvancedEnchantmentTarget.ARMOR));
    }
}
