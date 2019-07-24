package me.egg82.ae.api.curses;

import java.util.UUID;
import me.egg82.ae.api.AdvancedEnchantment;
import me.egg82.ae.api.AdvancedEnchantmentTarget;

public class GrogginessCurse extends AdvancedEnchantment {
    public GrogginessCurse() {
        super(UUID.randomUUID(), "grogginess_curse", "Curse of Grogginess", true, 1, 5);
        targets.add(AdvancedEnchantmentTarget.WEARABLE);
    }
}
