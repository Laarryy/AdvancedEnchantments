package me.egg82.ae.api.curses;

import java.util.UUID;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.AdvancedEnchantmentTarget;

public class EmpathyCurse extends AdvancedEnchantment {
    public EmpathyCurse() {
        super(UUID.randomUUID(), "empathy_curse", "Curse of Empathy", true, 1, 5);
        targets.add(AdvancedEnchantmentTarget.ARMOR_TORSO);
    }
}
