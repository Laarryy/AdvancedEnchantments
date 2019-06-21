package me.egg82.ae.api.curses;

import java.util.UUID;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.AdvancedEnchantmentTarget;

public class EnderCurse extends AdvancedEnchantment {
    public EnderCurse() {
        super(UUID.randomUUID(), "ender_curse", "Curse of Ender", true, 1, 3);
        targets.add(AdvancedEnchantmentTarget.ARMOR);
    }
}
